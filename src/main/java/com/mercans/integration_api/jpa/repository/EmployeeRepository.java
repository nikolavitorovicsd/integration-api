package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.EmployeeEntity;
import com.mercans.integration_api.jpa.EmployeeIdAndCodeView;
import com.mercans.integration_api.jpa.EmployeeSalaryView;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

  @Query(" SELECT em.employeeCode FROM EmployeeEntity em ")
  Set<String> getAllEmployeeCodes();

  @Query(" SELECT em FROM EmployeeEntity em LEFT JOIN FETCH em.salaryComponents ORDER BY em.id ")
  List<EmployeeEntity> getAllEmployees();

  @Query(
      value =
          """
          SELECT view.person_id AS id,
          view.full_name AS employeeFullName,
          view.gender AS employeeGender,
          view.birth_date AS employeeBirthDate,
          view.employee_code AS employeeCode,
          view.hire_date AS employeeHireDate,
          view.current_salary_amount AS amount,
          view.salary_currency AS currency,
          view.salary_start_date AS startDate,
          view.salary_end_date AS endDate
          FROM employees_current_salaries as view
          """,
      nativeQuery = true)
  List<EmployeeSalaryView> getAllEmployeeViews();

  @Query(
      value =
          """
          SELECT p.id AS id, p.employee_code AS employeeCode
          FROM person p
          WHERE p.employee_code IN :employeeCodes
          """,
      nativeQuery = true)
  List<EmployeeIdAndCodeView> getEmployeeIdAndCodeViewsByCodes(Iterable<String> employeeCodes);
}
