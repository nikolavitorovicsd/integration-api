package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.EmployeeEntity;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {

  @Query(" SELECT em.employeeCode FROM EmployeeEntity em ")
  Set<String> getAllEmployeeCodes();

  // todo refactor to use only fields necessary and not whole object
  @Query(" SELECT em FROM EmployeeEntity em where em.employeeCode in :employeeCodes ")
  List<EmployeeEntity> getEmployeesByEmployeeCodes(Iterable<String> employeeCodes);
}
