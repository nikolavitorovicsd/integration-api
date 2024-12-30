package com.mercans.integration_api.service;

import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.model.BatchJobStatistics;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkInsertService {

  private final JdbcTemplate jdbcTemplate;

  public List<String> bulkInsert(
      List<EmployeeEntity> employees, BatchJobStatistics batchJobStatistics) {
    // hibernate is terribly slow in bulk inserts compared to this implementation
    // 'person' table sequence
    updatePersonTablePKSequence(batchJobStatistics);
    // 'salary_component' table sequence
    updateSalaryComponentTablePKSequence(batchJobStatistics);

    // we want to start from id 1 for bot tables!
    long nextEmployeeId = batchJobStatistics.getPersonSequence().get();
    long nextSalaryComponentId = batchJobStatistics.getComponentSequence().get();

    for (EmployeeEntity employee : employees) {
      // set current sequence number as person id
      var employeeId = BigInteger.valueOf(nextEmployeeId);
      employee.setId(employeeId);

      // set the corresponding employee id for each salary component and assign salary component id
      for (SalaryComponentEntity salaryComponent : employee.getSalaryComponentEntities()) {
        salaryComponent.setId(BigInteger.valueOf(nextSalaryComponentId));
        salaryComponent.setEmployeeId(employeeId);
        // increment the salary component ID for the next one
        nextSalaryComponentId++;
      }
      // increment employee id sequence for the next employee
      nextEmployeeId++;
    }

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
    // todo handle later
    //        employees.stream()
    //            .map(employee -> Date.valueOf(employee.getEmployeeBirthDate()))
    //            .toArray(Date[]::new);

    // components
    Long[] componentsIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(component -> component.getId().longValue())
            .toArray(Long[]::new);
    Long[] componentEmployeeIds =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(component -> component.getEmployeeId().longValue())
            .toArray(Long[]::new);

    Long[] componentsAmounts =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(SalaryComponentEntity::getAmount)
            .toArray(Long[]::new);
    String[] componentsCurrencies =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(component -> component.getCurrency().name())
            .toArray(String[]::new);

    Date[] componentsStartDates =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(component -> Date.valueOf(component.getStartDate()))
            .toArray(Date[]::new);

    Date[] componentsEndDates =
        employees.stream()
            .flatMap(employee -> employee.getSalaryComponentEntities().stream())
            .map(component -> Date.valueOf(component.getEndDate()))
            .toArray(Date[]::new);

    // query that inserts in both 'person' and 'salary_component' table applying PG unnest ability
    String insertPersonAndSalaryComponentsSql =
        """
            INSERT INTO person (id, full_name, employee_code, hire_date, gender, birth_date)
            SELECT * FROM unnest(?::numeric[], ?::text[], ?::text[], ?::date[], ?::text[],?::date[]);

            INSERT INTO salary_component (id, person_id, amount, currency, start_date, end_date)
            SELECT *  FROM unnest(?::numeric[], ?::numeric[], ?::numeric[], ?::text[], ?::date[], ?::date[]);
            """;

    var startTime = System.currentTimeMillis();
    // execute query
    var rowsInserted =
        jdbcTemplate.update(
            insertPersonAndSalaryComponentsSql,
            employeesIds,
            employeesFullNames,
            employeesCodes,
            employeesHireDates,
            employeesGenders,
            employeesBirthDates,
            componentsIds,
            componentEmployeeIds,
            componentsAmounts,
            componentsCurrencies,
            componentsStartDates,
            componentsEndDates);

    // after successful insert, increase sequences
    if (employeesIds.length > 0) {
      batchJobStatistics.getPersonSequence().getAndAdd(employeesIds.length);
    }
    if (componentsIds.length > 0) {
      batchJobStatistics.getComponentSequence().getAndAdd(componentsIds.length);
    }

    log.info(
        "INSERTED {} ROWS TO DB IN '{}' ms", rowsInserted, System.currentTimeMillis() - startTime);

    return List.of(employeesCodes);
  }

  /**
   * this method makes sure that 'person' table is updated properly to avoid having duplicate pk
   * keys and updates current job statistics with current sequences to allow better track of the db
   *
   * @param batchJobStatistics
   */
  private void updatePersonTablePKSequence(BatchJobStatistics batchJobStatistics) {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (batchJobStatistics.getPersonSequence().get() == 0) {
      String maxEmployeeIdQuery = "SELECT MAX (p.id) FROM person p";

      Long maxEmployeeId = jdbcTemplate.queryForObject(maxEmployeeIdQuery, Long.class);

      // if 'person' table is empty, maxEmployeeId will be returned as null
      if (maxEmployeeId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        batchJobStatistics.getPersonSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        batchJobStatistics.getPersonSequence().getAndAdd(maxEmployeeId + 1);
      }
    } else {
      // if sequence count is present in statistics, we will increase counter before unnest insert
      // start
      batchJobStatistics.getPersonSequence().get();
    }
  }

  /**
   * this method makes sure that 'salary_component' table is updated properly to avoid having
   * duplicate pk keys and updates current job statistics with current sequences to allow better
   * track of the db
   *
   * @param batchJobStatistics
   */
  private void updateSalaryComponentTablePKSequence(BatchJobStatistics batchJobStatistics) {
    // if sequence count is not present in statistics, we will fetch it and update accordingly
    if (batchJobStatistics.getComponentSequence().get() == 0) {
      String maxComponentQuery = "SELECT MAX (sc.id) FROM salary_component sc";

      Long maxComponentId = jdbcTemplate.queryForObject(maxComponentQuery, Long.class);

      // if 'salary_component' table is empty, maxEmployeeId will be returned as null
      if (maxComponentId == null) {
        // table is empty, counter is currently 0 and needs to be increased to 1
        batchJobStatistics.getComponentSequence().incrementAndGet();
      } else {
        // table is not empty, counter has some value and we increase it by 1
        batchJobStatistics.getComponentSequence().getAndAdd(maxComponentId + 1);
      }
    } else {
      // if sequence count is present in statistics, we will increase counter before unnest insert
      // start
      batchJobStatistics.getComponentSequence().get();
    }
  }
}
