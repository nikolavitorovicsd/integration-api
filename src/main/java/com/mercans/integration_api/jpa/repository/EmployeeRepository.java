package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.EmployeeEntity;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, BigInteger> {

  @Query(" SELECT em.employeeCode FROM EmployeeEntity em ")
  Set<String> getAllEmployeeCodes();

  @Query(" SELECT em FROM EmployeeEntity em where em.employeeCode in :employeeCodes ")
  List<EmployeeEntity> getEmployeesIdsByCodes(Iterable<String> employeeCodes);
}
