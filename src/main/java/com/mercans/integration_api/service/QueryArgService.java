package com.mercans.integration_api.service;

import static com.mercans.integration_api.constants.Queries.MAX_PERSON_ID_QUERY;
import static com.mercans.integration_api.constants.Queries.MAX_SALARY_COMPONENT_ID_QUERY;

import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.model.BatchJobStatistics;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.QueryArgHolder;
import com.mercans.integration_api.model.actions.ChangeAction;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryArgService {

  private final BatchJobCache batchJobCache;
  private final JdbcTemplate jdbcTemplate;

  protected BatchJobStatistics getBatchJobStatistics() {
    return batchJobCache.getStatistics();
  }

  /**
   * this method makes sure that 'person' table is updated properly to avoid having duplicate pk
   * keys and updates current job statistics with current sequences to allow better track of the db
   */
  public void updatePersonTablePKSequence() {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (getBatchJobStatistics().getPersonSequence().get() == 0) {
      Long maxEmployeeId = jdbcTemplate.queryForObject(MAX_PERSON_ID_QUERY, Long.class);

      // if 'person' table is empty, maxEmployeeId will be returned as null
      if (maxEmployeeId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        getBatchJobStatistics().getPersonSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        getBatchJobStatistics().updatePersonSequence(maxEmployeeId + 1);
      }
    }
  }

  /**
   * this method makes sure that 'salary_component' table is updated properly to avoid having
   * duplicate pk keys and updates current job statistics with current sequences to allow better
   * track of the db
   */
  public void updateSalaryComponentTablePKSequence() {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (getBatchJobStatistics().getComponentSequence().get() == 0) {
      Long maxComponentId = jdbcTemplate.queryForObject(MAX_SALARY_COMPONENT_ID_QUERY, Long.class);

      // if 'salary_component' table is empty, maxEmployeeId will be returned as null
      if (maxComponentId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        getBatchJobStatistics().getComponentSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        getBatchJobStatistics().updateComponentSequence(maxComponentId + 1);
      }
    }
  }

  public QueryArgHolder buildPersonAndSalaryComponentInsertQueryArgs(
      List<EmployeeEntity> employees) {
    // employees
    Long[] employeesIds = employees.stream().map(EmployeeEntity::getId).toArray(Long[]::new);

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

    Date[] employeesBirthDates =
        employees.stream()
            .map(employee -> Date.valueOf(employee.getEmployeeBirthDate()))
            .toArray(Date[]::new);

    // components
    Long[] componentsIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(SalaryComponentEntity::getId)
            .toArray(Long[]::new);

    Long[] componentEmployeeIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(SalaryComponentEntity::getEmployeeId)
            .toArray(Long[]::new);

    BigDecimal[] componentsAmounts =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponents().stream())
            .map(SalaryComponentEntity::getAmount)
            .toArray(BigDecimal[]::new);

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

    Timestamp[] employeesCreationDates =
        employees.stream()
            .map(
                employee ->
                    Timestamp.from(employee.getCreationDate().truncatedTo(ChronoUnit.SECONDS)))
            .toArray(Timestamp[]::new);

    Timestamp[] employeesModificationDates =
        employees.stream()
            .map(
                employee ->
                    Timestamp.from(employee.getModificationDate().truncatedTo(ChronoUnit.SECONDS)))
            .toArray(Timestamp[]::new);

    return QueryArgHolder.builder()
        .employeesIds(employeesIds)
        .employeesFullNames(employeesFullNames)
        .employeesCodes(employeesCodes)
        .employeesHireDates(employeesHireDates)
        .employeesGenders(employeesGenders)
        .employeesBirthDates(employeesBirthDates)
        .employeesCreationDates(employeesCreationDates)
        .employeesModificationDates(employeesModificationDates)
        .componentsIds(componentsIds)
        .componentEmployeeIds(componentEmployeeIds)
        .componentsAmounts(componentsAmounts)
        .componentsCurrencies(componentsCurrencies)
        .componentsStartDates(componentsStartDates)
        .componentsEndDates(componentsEndDates)
        .build();
  }

  public QueryArgHolder buildSalaryComponentInsertQueryArgs(
      List<ChangeAction> actionsForWhichWeUpdatePersonComponents,
      Map<String, Long> employeeCodeToIdMap) {
    Long[] newComponentIds =
        getNewSequencesForNewComponents(actionsForWhichWeUpdatePersonComponents);

    Long[] componentEmployeeIds =
        getComponentEmployeeIds(actionsForWhichWeUpdatePersonComponents, employeeCodeToIdMap);

    BigDecimal[] componentsAmounts =
        actionsForWhichWeUpdatePersonComponents.stream()
            .flatMap(action -> action.payComponents().stream())
            .map(PayComponent::amount)
            .toArray(BigDecimal[]::new);

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

    return QueryArgHolder.builder()
        .componentsIds(newComponentIds)
        .componentEmployeeIds(componentEmployeeIds)
        .componentsAmounts(componentsAmounts)
        .componentsCurrencies(componentsCurrencies)
        .componentsStartDates(componentsStartDates)
        .componentsEndDates(componentsEndDates)
        .build();
  }

  // method that calculates following sequence array for 'salary_component'
  // in order to insert new rows  without creating a gap between PKs
  private Long[] getNewSequencesForNewComponents(
      List<ChangeAction> actionsForWhichWeUpdatePersonComponents) {
    updateSalaryComponentTablePKSequence();

    var totalComponentCountToBeAdded =
        actionsForWhichWeUpdatePersonComponents.stream()
            .mapToLong(action -> action.payComponents().size())
            .sum();

    List<Long> newSequenceList = new ArrayList<>();
    for (int i = 0; i < totalComponentCountToBeAdded; i++) {
      var currentSequence = getBatchJobStatistics().getComponentSequence().get();
      newSequenceList.add(currentSequence);
      getBatchJobStatistics().getComponentSequence().incrementAndGet();
    }

    return newSequenceList.toArray(Long[]::new);
  }

  // this method maps employeeId ('person_id') to each new salary_component
  // to be inserted for person
  private Long[] getComponentEmployeeIds(
      List<ChangeAction> actionsForWhichWeUpdatePersonComponents,
      Map<String, Long> employeeCodeToIdMap) {
    List<Long> employeeIds = new ArrayList<>();
    actionsForWhichWeUpdatePersonComponents.forEach(
        action ->
            action
                .payComponents()
                .forEach(
                    component ->
                        employeeIds.add(employeeCodeToIdMap.get(action.getEmployeeCode()))));
    return employeeIds.toArray(Long[]::new);
  }
}
