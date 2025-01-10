package com.mercans.integration_api.jpa;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EmployeeView {

  Long getId();

  String getEmployeeFullName();

  String getEmployeeGender();

  LocalDate getEmployeeBirthDate();

  String getEmployeeCode();

  LocalDate getEmployeeHireDate();

  BigDecimal getAmount();

  String getCurrency();

  LocalDate getStartDate();

  LocalDate getEndDate();
}
