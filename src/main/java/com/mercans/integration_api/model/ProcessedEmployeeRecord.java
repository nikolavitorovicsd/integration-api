package com.mercans.integration_api.model;

import com.mercans.integration_api.model.enums.Action;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.model.enums.Gender;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ProcessedEmployeeRecord {

  Action action;

  String employeeName;

  Gender employeeGender;

  String employeeCode;

  LocalDate employeeContractStartDate;

  LocalDate employeeContractEndDate;

  // pay
  Double payAmount;

  Currency payCurrency;

  LocalDate payStartDate;

  LocalDate payEndDate;

  // compensation
  Double compensationAmount;

  Currency compensationCurrency;

  LocalDate compensationStartDate;

  LocalDate compensationEndDate;
}
