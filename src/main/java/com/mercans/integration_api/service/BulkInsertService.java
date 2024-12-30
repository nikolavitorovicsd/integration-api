package com.mercans.integration_api.service;

import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.SalaryComponentEntity;
import com.mercans.integration_api.model.BatchJobStatistics;
import java.math.BigInteger;
import java.sql.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkInsertService {

  private final JdbcTemplate jdbcTemplate;

  public void bulkInsert(List<EmployeeEntity> employees, BatchJobStatistics batchJobStatistics) {

    log.info("_______PREPARING SEQUENCE________");
    long preparingTimeStart = System.currentTimeMillis();

    // person sequence quering
    if (batchJobStatistics.getPersonSequence().get() == 0) {
      String maxEmployeeIdQuery = "SELECT MAX (p.id) FROM person p";

      Long maxEmployeeId = jdbcTemplate.queryForObject(maxEmployeeIdQuery, Long.class);

      if (maxEmployeeId == null) {
        batchJobStatistics.getPersonSequence().incrementAndGet();
      } else {
        batchJobStatistics.getPersonSequence().getAndAdd(maxEmployeeId + 1);
      }
    } else {
      batchJobStatistics.getPersonSequence().get();
    }

    // person sequence quering
    if (batchJobStatistics.getComponentSequence().get() == 0) {
      String maxComponentQuery = "SELECT MAX (sc.id) FROM salary_component sc";

      Long maxComponentId = jdbcTemplate.queryForObject(maxComponentQuery, Long.class);

      if (maxComponentId == null) {
        batchJobStatistics.getComponentSequence().incrementAndGet();
      } else {
        batchJobStatistics.getComponentSequence().getAndAdd(maxComponentId + 1);
      }
    } else {
      batchJobStatistics.getComponentSequence().get();
    }

    // we want to start from id 1 for bot tables!
    long nextEmployeeId = batchJobStatistics.getPersonSequence().get();
    long nextSalaryComponentId = batchJobStatistics.getComponentSequence().get();

    log.info(
        "_______PREPARED SEQUENCE IN '{}' ms", System.currentTimeMillis() - preparingTimeStart);

    // todo bellow is good, above makes issues

    log.info("_______PREPARING ENTITIES______");
    long entitiesTimeStart = System.currentTimeMillis();

    for (EmployeeEntity employee : employees) {
      // Set the employee ID
      var employeeId = BigInteger.valueOf(nextEmployeeId);
      employee.setId(employeeId);

      // For each salary component, set the corresponding employee ID and assign salary component ID
      for (SalaryComponentEntity salaryComponent : employee.getSalaryComponentEntities()) {
        salaryComponent.setId(BigInteger.valueOf(nextSalaryComponentId));
        salaryComponent.setEmployeeId(employeeId);
        nextSalaryComponentId++; // Increment the salary component ID for the next one
      }
      nextEmployeeId++; // Increment employee ID for the next employee
    }

    log.info(
        "_______PREPARED ENTITIES______ IN '{}' ms",
        System.currentTimeMillis() - entitiesTimeStart);

    log.info("_______PREPARING QUERY VALUES______");
    long queryValuesTimeStart = System.currentTimeMillis();

    // employees
    Long[] employeesIds =
        employees.stream().map(employee -> employee.getId().longValue()).toArray(Long[]::new);
    String[] employeesFullNames =
        employees.stream().map(EmployeeEntity::getEmployeeFullName).toArray(String[]::new);
    String[] employeesCodes =
        employees.stream().map(EmployeeEntity::getEmployeeCode).toArray(String[]::new);
    Date[] employeesHireDates =
        employees.stream()
            .map(employee -> Date.valueOf(employee.getEmployeeHireDate()))
            .toArray(Date[]::new);

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

    log.info(
        "_______PREPARed QUERY VALUES IN '{}' ms ",
        System.currentTimeMillis() - queryValuesTimeStart);

    log.info("_______PREPARING QUERIES______");
    long preparingQueryStart = System.currentTimeMillis();

    // SQL Query to insert Employee and retrieve generated IDs
    String insertPersonSql =
        """
            INSERT INTO person (id, full_name, employee_code, hire_date)
            SELECT * FROM unnest(?::numeric[], ?::text[], ?::text[], ?::date[]);

            INSERT INTO salary_component (id, person_id, amount, currency, start_date, end_date)
            SELECT *  FROM unnest(?::numeric[], ?::numeric[], ?::numeric[], ?::text[], ?::date[], ?::date[]);
            """;

    log.info(
        "_______ADDED QUERIES___IN '{}' ms. ", System.currentTimeMillis() - preparingQueryStart);

    log.info("PREPARING TO INSERT ROWS TO DB");
    long startTime = System.currentTimeMillis();

    // Execute the batch update
    var rowsInserted =
        jdbcTemplate.update(
            insertPersonSql,
            employeesIds,
            employeesFullNames,
            employeesCodes,
            employeesHireDates,
            componentsIds,
            componentEmployeeIds,
            componentsAmounts,
            componentsCurrencies,
            componentsStartDates,
            componentsEndDates);

    // need to increase sequence again
    if(employeesIds.length > 0) {
      batchJobStatistics.getPersonSequence().getAndAdd(employeesIds.length);
    }
    if(componentsIds.length > 0) {
      batchJobStatistics.getComponentSequence().getAndAdd(componentsIds.length);
    }

    log.info(
        "INSERTED {} ROWS TO DB IN '{}' ms", rowsInserted, System.currentTimeMillis() - startTime);
  }
}
