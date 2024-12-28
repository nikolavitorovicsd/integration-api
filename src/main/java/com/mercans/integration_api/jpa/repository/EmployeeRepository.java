package com.mercans.integration_api.jpa.repository;

import com.mercans.integration_api.jpa.EmployeeEntity;
import java.math.BigInteger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, BigInteger> {}
