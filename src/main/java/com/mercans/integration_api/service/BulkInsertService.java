package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.Queries.*;
import static java.util.stream.Collectors.toMap;

import com.mercans.integration_api.config.listeners.BatchJobCache;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.jpa.repository.EmployeeRepository;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.QueryArgHolder;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.actions.TerminateAction;
import com.mercans.integration_api.model.enums.ActionType;
import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
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
  private final BatchJobCache batchJobCache;
  private final EmployeeRepository employeeRepository;

  public List<String> bulkInsert(List<EmployeeEntity> employees) {
    var xx = System.currentTimeMillis();
    // 'person' table sequence
    updatePersonTablePKSequence();
    // 'salary_component' table sequence
    updateSalaryComponentTablePKSequence();

    long nextEmployeeId = batchJobCache.getStatistics().getPersonSequence().get();
    long nextSalaryComponentId = batchJobCache.getStatistics().getComponentSequence().get();

    for (EmployeeEntity employee : employees) {
      // set current sequence number as person id
      var employeeId = BigInteger.valueOf(nextEmployeeId);
      employee.setId(employeeId);

      // set the corresponding employee id for each salary component and assign salary component id
      for (SalaryComponentEntity salaryComponent : employee.getSalaryComponents()) {
        salaryComponent.setId(BigInteger.valueOf(nextSalaryComponentId));
        salaryComponent.setEmployeeId(employeeId);
        // increment the salary component ID for the next one
        nextSalaryComponentId++;
      }
      // increment employee id sequence for the next employee
      nextEmployeeId++;
    }

    log.info("PREPARED ENTITIES AND SEQUENCE IN '{}' ms", System.currentTimeMillis() - xx);

    var startTime = System.currentTimeMillis();
    // execute query
    QueryArgHolder queryArgHolder = buildQueryArgs(employees);
    var rowsInserted =
        jdbcTemplate.update(
            UNNEST_INSERT_INTO_PERSON_AND_SALARY_COMPONENT_QUERY,
            queryArgHolder.employeesIds(),
            queryArgHolder.employeesFullNames(),
            queryArgHolder.employeesCodes(),
            queryArgHolder.employeesHireDates(),
            queryArgHolder.employeesGenders(),
            queryArgHolder.employeesBirthDates(),
            queryArgHolder.componentsIds(),
            queryArgHolder.componentEmployeeIds(),
            queryArgHolder.componentsAmounts(),
            queryArgHolder.componentsCurrencies(),
            queryArgHolder.componentsStartDates(),
            queryArgHolder.componentsEndDates());

    // after successful insert, increase sequences
    if (queryArgHolder.employeesIds().length > 0) {
      batchJobCache.getStatistics().updatePersonSequence(queryArgHolder.employeesIds().length);
    }
    if (queryArgHolder.componentsIds().length > 0) {
      batchJobCache.getStatistics().updateComponentSequence(queryArgHolder.componentsIds().length);
    }

    log.info(
        "INSERTED {} ROWS TO DB IN '{}' ms", rowsInserted, System.currentTimeMillis() - startTime);

    return List.of(queryArgHolder.employeesCodes());
  }

  /**
   * this method makes sure that 'person' table is updated properly to avoid having duplicate pk
   * keys and updates current job statistics with current sequences to allow better track of the db
   */
  private void updatePersonTablePKSequence() {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (batchJobCache.getStatistics().getPersonSequence().get() == 0) {
      Long maxEmployeeId = jdbcTemplate.queryForObject(MAX_PERSON_ID_QUERY, Long.class);

      // if 'person' table is empty, maxEmployeeId will be returned as null
      if (maxEmployeeId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        batchJobCache.getStatistics().getPersonSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        batchJobCache.getStatistics().updatePersonSequence(maxEmployeeId + 1);
      }
    } else {
      // if sequence count is present in statistics, we will increase counter before unnest insert
      batchJobCache.getStatistics().getPersonSequence().get();
    }
  }

  /**
   * this method makes sure that 'salary_component' table is updated properly to avoid having
   * duplicate pk keys and updates current job statistics with current sequences to allow better
   * track of the db
   */
  private void updateSalaryComponentTablePKSequence() {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (batchJobCache.getStatistics().getComponentSequence().get() == 0) {
      Long maxComponentId = jdbcTemplate.queryForObject(MAX_SALARY_COMPONENT_ID_QUERY, Long.class);

      // if 'salary_component' table is empty, maxEmployeeId will be returned as null
      if (maxComponentId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        batchJobCache.getStatistics().getComponentSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        batchJobCache.getStatistics().updateComponentSequence(maxComponentId + 1);
      }
    } else {
      // if sequence count is present in statistics, we will increase counter before unnest insert
      batchJobCache.getStatistics().getComponentSequence().get();
    }
  }

  // todo maybe move it to new service later
  private QueryArgHolder buildQueryArgs(List<EmployeeEntity> employees) {
    // employees
    Long[] employeesIds =
        employees.stream().map(employee -> employee.getId().longValue()).toArray(Long[]::new);
    String[] employeesCodes =
        employees.stream().map(EmployeeEntity::getEmployeeCode).toArray(String[]::new);
    Date[] employeesHireDates =
        employees.stream()
            .map(employee -> Date.valueOf(employee.getEmployeeHireDate()))
            .toArray(Date[]::new);
    String[] employeesFullNames =
        employees.stream().map(EmployeeEntity::getEmployeeFullName).toArray(String[]::new);
    String[] employeesGenders =
        employees.stream()
            .map(
                employee ->
                    Optional.ofNullable(employee.getEmployeGender()).map(Enum::name).orElse(null))
            .toArray(String[]::new);
    Date[] employeesBirthDates = null;
    // todo finish implementation
    //        employees.stream()
    //            .map(employee -> Date.valueOf(employee.getEmployeeBirthDate()))
    //            .toArray(Date[]::new);

    // components
    Long[] componentsIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(component -> component.getId().longValue())
            .toArray(Long[]::new);
    Long[] componentEmployeeIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(component -> component.getEmployeeId().longValue())
            .toArray(Long[]::new);

    Long[] componentsAmounts =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(SalaryComponentEntity::getAmount)
            .toArray(Long[]::new);

    String[] componentsCurrencies =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(component -> component.getCurrency().name())
            .toArray(String[]::new);

    Date[] componentsStartDates =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(component -> Date.valueOf(component.getStartDate()))
            .toArray(Date[]::new);

    Date[] componentsEndDates =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(component -> Date.valueOf(component.getEndDate()))
            .toArray(Date[]::new);

    return QueryArgHolder.builder()
        .employeesIds(employeesIds)
        .employeesFullNames(employeesFullNames)
        .employeesCodes(employeesCodes)
        .employeesHireDates(employeesHireDates)
        .employeesGenders(employeesGenders)
        .employeesBirthDates(employeesBirthDates)
        .componentsIds(componentsIds)
        .componentEmployeeIds(componentEmployeeIds)
        .componentsAmounts(componentsAmounts)
        .componentsCurrencies(componentsCurrencies)
        .componentsStartDates(componentsStartDates)
        .componentsEndDates(componentsEndDates)
        .build();
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

    Date[] employeesBirthDates = null;
    // todo handle later
    //        employees.stream()
    //            .map(employee -> Date.valueOf(employee.getEmployeeBirthDate()))
    //            .toArray(Date[]::new);

    var rowsUpdated =
        jdbcTemplate.update(
            UNNEST_UPDATE_PERSON_QUERY,
            employeesCodes,
            employeesFullNames,
            employeesGenders,
            employeesBirthDates);

    log.info(
        "UPDATEDDD {} ROWS TO DB IN '{}' ms", rowsUpdated, System.currentTimeMillis() - startTime);
  }

  // this method should remove all components for specific employee if new action has list of
  // components that is not empty
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
        employeeRepository.getEmployeesIdsByCodes(employeesCodesForWhichWeUpdatePersonComponents);

    var employeeCodeToIdMap =
        employees.stream().collect(toMap(EmployeeEntity::getEmployeeCode, EmployeeEntity::getId));

    Long[] employeeIdsForWhichWeNeedToRemoveSalaryComponents =
        employees.stream().map(employee -> employee.getId().longValue()).toArray(Long[]::new);

    var removedSalaryComponentsCount =
        jdbcTemplate.update(
            UNNEST_DELETE_FROM_SALARY_COMPONENT_QUERY,
            LocalDate.now(),
            employeeIdsForWhichWeNeedToRemoveSalaryComponents);

    log.info(
        "REMOVED COMPONENT SALARIES '{}' ROWS TO DB IN '{}' ms",
        removedSalaryComponentsCount,
        System.currentTimeMillis() - removeComponentsStartTime);

    var addComponentsStartTime = System.currentTimeMillis();

    Long[] componentIdsNew =
        getNewSequencesForNewComponents(actionsForWhichWeUpdatePersonComponents);

    Long[] componentEmployeeIds =
        getComponentEmployeeIds(actionsForWhichWeUpdatePersonComponents, employeeCodeToIdMap);

    Long[] componentsAmounts =
        actionsForWhichWeUpdatePersonComponents.stream()
            .flatMap(action -> action.payComponents().stream())
            .map(PayComponent::amount)
            .toArray(Long[]::new);

    String[] componentsCurrencies =
        actionsForWhichWeUpdatePersonComponents.stream()
            .flatMap(action -> action.payComponents().stream())
            .map(component -> component.currency().name())
            .toArray(String[]::new);

    Date[] componentsStartDates =
        actionsForWhichWeUpdatePersonComponents.stream()
            .flatMap(action -> action.payComponents().stream())
            .map(component -> Date.valueOf(component.startDate()))
            .toArray(Date[]::new);

    Date[] componentsEndDates =
        actionsForWhichWeUpdatePersonComponents.stream()
            .flatMap(action -> action.payComponents().stream())
            .map(component -> Date.valueOf(component.endDate()))
            .toArray(Date[]::new);

    var insertedComponentsCount =
        jdbcTemplate.update(
            UNNEST_INSERT_INTO_SALARY_COMPONENT_QUERY,
            componentIdsNew,
            componentEmployeeIds,
            componentsAmounts,
            componentsCurrencies,
            componentsStartDates,
            componentsEndDates);

    log.info(
        "ADDED COMPONENT SALARIES '{}' ROWS TO DB IN '{}' ms",
        insertedComponentsCount,
        System.currentTimeMillis() - addComponentsStartTime);
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

    var rowsUpdated =
        jdbcTemplate.update(
            UNNEST_TERMINATE_PERSON_QUERY, employeesCodes, employeesTerminationDates);

    log.info(
        "TERMINATED {} ROWS TO DB IN '{}' ms", rowsUpdated, System.currentTimeMillis() - startTime);
  }

  // method that calculates following sequence array for 'salary_component' in order to insert new
  // rows
  // without creating a gap between PKs
  private Long[] getNewSequencesForNewComponents(
      List<ChangeAction> actionsForWhichWeUpdatePersonComponents) {
    updateSalaryComponentTablePKSequence();

    var totalComponentCountToBeAdded =
        actionsForWhichWeUpdatePersonComponents.stream()
            .mapToLong(action -> action.payComponents().size())
            .sum();

    List<Long> newSequenceList = new ArrayList<>();
    for (int i = 0; i < totalComponentCountToBeAdded; i++) {
      var currentSequence = batchJobCache.getStatistics().getComponentSequence().get();
      newSequenceList.add(currentSequence);
      batchJobCache.getStatistics().getComponentSequence().incrementAndGet();
    }

    return newSequenceList.toArray(Long[]::new);
  }

  // this method maps employeeId ('person_id') to each new salary_component to be inserted for
  // person
  private Long[] getComponentEmployeeIds(
      List<ChangeAction> actionsForWhichWeUpdatePersonComponents,
      Map<String, BigInteger> employeeCodeToIdMap) {
    List<Long> employeeIds = new ArrayList<>();
    actionsForWhichWeUpdatePersonComponents.forEach(
        action ->
            action
                .payComponents()
                .forEach(
                    component ->
                        employeeIds.add(
                            employeeCodeToIdMap.get(action.getEmployeeCode()).longValue())));
    return employeeIds.toArray(Long[]::new);
  }

  private EmployeeEntity buildChangePersonEntity(ChangeAction changeAction) {
    return EmployeeEntity.builder()
        .employeeCode(changeAction.getEmployeeCode())
        .employeeFullName(changeAction.employeeFullName())
        .employeGender(changeAction.employeGender())
        .employeeBirthDate(changeAction.employeeBirthDate())
        .build();
  }
}
