package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.Queries.*;
import static java.util.stream.Collectors.toMap;

import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.QueryArgHolder;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.actions.TerminateAction;
import com.mercans.integration_api.model.enums.ActionType;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkInsertService {

  private final JdbcTemplate jdbcTemplate;
  private final EmployeeRepository employeeRepository;
  private final QueryArgService queryArgService;

  public void bulkInsert(List<EmployeeEntity> employees) {
    if (employees.isEmpty()) {
      // nothing to update
      return;
    }
    // todo remove
    var xx = System.currentTimeMillis();

    // 'person' table sequence
    queryArgService.updatePersonTablePKSequence();
    long nextEmployeeId = queryArgService.getBatchJobStatistics().getPersonSequence().get();
    // 'salary_component' table sequence
    queryArgService.updateSalaryComponentTablePKSequence();
    long nextSalaryComponentId =
        queryArgService.getBatchJobStatistics().getComponentSequence().get();

    for (EmployeeEntity employee : employees) {
      // set current sequence number as person id
      employee.setId(nextEmployeeId);

      var date = Instant.now();
      employee.setCreationDate(date);
      employee.setModificationDate(date);

      // set the corresponding employee id for each salary component and assign salary component id
      for (SalaryComponentEntity salaryComponent : employee.getSalaryComponents()) {
        salaryComponent.setId(nextSalaryComponentId);
        salaryComponent.setEmployeeId(nextEmployeeId);
        // increment the salary component ID for the next one
        nextSalaryComponentId++;
      }
      // increment employee id sequence for the next employee
      nextEmployeeId++;
    }

    log.info("PREPARED ENTITIES AND SEQUENCE IN '{}' ms", System.currentTimeMillis() - xx);

    var startTime = System.currentTimeMillis();
    // execute query
    QueryArgHolder queryArgHolder =
        queryArgService.buildPersonAndSalaryComponentInsertQueryArgs(employees);

    var rowsInserted = executeInsert(queryArgHolder);

    // after successful insert, increase sequences
    if (queryArgHolder.employeesIds().length > 0) {
      queryArgService
          .getBatchJobStatistics()
          .updatePersonSequence(queryArgHolder.employeesIds().length);
    }
    if (queryArgHolder.componentsIds().length > 0) {
      queryArgService
          .getBatchJobStatistics()
          .updateComponentSequence(queryArgHolder.componentsIds().length);
    }

    log.info(
        "INSERTED {} ROWS TO DB IN '{}' ms", rowsInserted, System.currentTimeMillis() - startTime);

    // updating the list employeeCodesThatExistInDb to have track in next chunk what was added
    queryArgService
        .getBatchJobStatistics()
        .getEmployeeCodesThatExistInDb()
        .addAll(List.of(queryArgHolder.employeesCodes()));
  }

  public void bulkUpdate(List<Action> changeActions) {
    if (changeActions.isEmpty()) {
      // nothing to update
      return;
    }

    var updateEmployees =
        changeActions.stream()
            .filter(action -> action.getAction().equals(ActionType.CHANGE))
            .map(ChangeAction.class::cast)
            .map(this::buildChangePersonEntity)
            .toList();

    // this method only updates person table and update of 'salary_component' happens in next method
    updatePersonTableOnly(updateEmployees);

    // remove components from employees for which new change action has more than 0 components,
    // after removal, we add new components from actions
    removeOldComponentsAndAddNew(changeActions);
  }

  public void bulkTerminate(List<Action> terminateActions) {
    if (terminateActions.isEmpty()) {
      // nothing to update
      return;
    }

    var startTime = System.currentTimeMillis();

    String[] employeesCodes =
        terminateActions.stream().map(Action::getEmployeeCode).toArray(String[]::new);

    Date[] employeesTerminationDates =
        terminateActions.stream()
            .map(TerminateAction.class::cast)
            .map(terminateAction -> Date.valueOf(terminateAction.terminationDate()))
            .toArray(Date[]::new);

    Timestamp[] employeesModificationDates =
        terminateActions.stream()
            .map(action -> Timestamp.from(Instant.now()))
            .toArray(Timestamp[]::new);

    var rowsUpdated =
        jdbcTemplate.update(
            UNNEST_TERMINATE_PERSON_QUERY,
            employeesCodes,
            employeesTerminationDates,
            employeesModificationDates);

    log.info(
        "TERMINATED {} ROWS TO DB IN '{}' ms", rowsUpdated, System.currentTimeMillis() - startTime);
  }

  private void updatePersonTableOnly(List<EmployeeEntity> updateEmployees) {

    var startTime = System.currentTimeMillis();

    String[] employeesCodes =
        updateEmployees.stream().map(EmployeeEntity::getEmployeeCode).toArray(String[]::new);

    String[] employeesFullNames =
        updateEmployees.stream().map(EmployeeEntity::getEmployeeFullName).toArray(String[]::new);

    String[] employeesGenders =
        updateEmployees.stream()
            .map(
                employee ->
                    Optional.ofNullable(employee.getEmployeGender()).map(Enum::name).orElse(null))
            .toArray(String[]::new);

    Date[] employeesBirthDates =
        updateEmployees.stream()
            .map(employee -> Date.valueOf(employee.getEmployeeBirthDate()))
            .toArray(Date[]::new);

    Date[] employeesHireDates =
        updateEmployees.stream()
            .map(employee -> Date.valueOf(employee.getEmployeeHireDate()))
            .toArray(Date[]::new);

    Timestamp modificationDate = Timestamp.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    Timestamp[] employeesModificationDates =
        updateEmployees.stream().map(employee -> modificationDate).toArray(Timestamp[]::new);

    var rowsUpdated =
        jdbcTemplate.update(
            UNNEST_UPDATE_PERSON_QUERY,
            employeesCodes,
            employeesFullNames,
            employeesGenders,
            employeesBirthDates,
            employeesHireDates,
            employeesModificationDates);

    log.info(
        "UPDATEDDD {} ROWS TO DB IN '{}' ms", rowsUpdated, System.currentTimeMillis() - startTime);
  }

  // this method should remove (set delete_date) all components for specific employee if new action
  // has list of components that is not empty
  private void removeOldComponentsAndAddNew(List<Action> changeActions) {
    var removeComponentsStartTime = System.currentTimeMillis();

    List<String> employeesCodesForWhichWeUpdatePersonComponents = new ArrayList<>();

    // actions for which there exist valid pay components and for which we should remove previous
    // components in db first and then add new ones provided from csv
    List<ChangeAction> actionsForWhichWeUpdatePersonComponents =
        changeActions.stream()
            .map(ChangeAction.class::cast)
            .map(
                changeAction -> {
                  if (CollectionUtils.isNotEmpty(changeAction.payComponents())) {
                    employeesCodesForWhichWeUpdatePersonComponents.add(changeAction.employeeCode());
                    return changeAction;
                  } else {
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .toList();

    // fetch all employeeIds by employee codes from above for actions that have more than 0
    // components
    List<EmployeeEntity> employees =
        employeeRepository.getEmployeesByEmployeeCodes(
            employeesCodesForWhichWeUpdatePersonComponents);

    Map<String, Long> employeeCodeToIdMap =
        employees.stream().collect(toMap(EmployeeEntity::getEmployeeCode, EmployeeEntity::getId));

    Long[] employeeIdsForWhichWeNeedToRemoveSalaryComponents =
        employees.stream().map(EmployeeEntity::getId).toArray(Long[]::new);

    Date deleteDate = Date.valueOf(LocalDate.now());

    var removedSalaryComponentsCount =
        jdbcTemplate.update(
            UNNEST_DELETE_FROM_SALARY_COMPONENT_QUERY,
            deleteDate,
            employeeIdsForWhichWeNeedToRemoveSalaryComponents);

    log.info(
        "REMOVED COMPONENT SALARIES '{}' ROWS TO DB IN '{}' ms",
        removedSalaryComponentsCount,
        System.currentTimeMillis() - removeComponentsStartTime);

    var addComponentsStartTime = System.currentTimeMillis();

    QueryArgHolder queryArgHolder =
        queryArgService.buildSalaryComponentInsertQueryArgs(
            actionsForWhichWeUpdatePersonComponents, employeeCodeToIdMap);

    var insertedComponentsCount =
        jdbcTemplate.update(
            UNNEST_INSERT_INTO_SALARY_COMPONENT_QUERY,
            queryArgHolder.componentsIds(),
            queryArgHolder.componentEmployeeIds(),
            queryArgHolder.componentsAmounts(),
            queryArgHolder.componentsCurrencies(),
            queryArgHolder.componentsStartDates(),
            queryArgHolder.componentsEndDates());

    log.info(
        "ADDED COMPONENT SALARIES '{}' ROWS TO DB IN '{}' ms",
        insertedComponentsCount,
        System.currentTimeMillis() - addComponentsStartTime);
  }

  private int executeInsert(QueryArgHolder queryArgHolder) {
    return jdbcTemplate.update(
        UNNEST_INSERT_INTO_PERSON_AND_SALARY_COMPONENT_QUERY,
        queryArgHolder.employeesIds(),
        queryArgHolder.employeesFullNames(),
        queryArgHolder.employeesCodes(),
        queryArgHolder.employeesHireDates(),
        queryArgHolder.employeesGenders(),
        queryArgHolder.employeesBirthDates(),
        queryArgHolder.employeesCreationDates(),
        queryArgHolder.employeesModificationDates(),
        queryArgHolder.componentsIds(),
        queryArgHolder.componentEmployeeIds(),
        queryArgHolder.componentsAmounts(),
        queryArgHolder.componentsCurrencies(),
        queryArgHolder.componentsStartDates(),
        queryArgHolder.componentsEndDates());
  }

  private EmployeeEntity buildChangePersonEntity(ChangeAction changeAction) {
    return EmployeeEntity.builder()
        .employeeCode(changeAction.getEmployeeCode())
        .employeeHireDate(changeAction.employeeHireDate())
        .employeeFullName(changeAction.employeeFullName())
        .employeGender(changeAction.employeGender())
        .employeeBirthDate(changeAction.employeeBirthDate())
        .build();
  }
}
