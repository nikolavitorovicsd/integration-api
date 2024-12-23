package com.mercans.integration_api.model;

import com.opencsv.bean.CsvBindByName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

// Pojo class used to map CSV lines directly by using opencsv library
@Getter
@Valid
public class RequestEntry {

  @CsvBindByName(column = "ACTION")
  @NotEmpty
  String action;

  @CsvBindByName(column = "worker_name")
  @NotEmpty
  String name;

  @CsvBindByName(column = "contract_workerId")
  @NotEmpty
  String employeeCode;

  @CsvBindByName(column = "pay_amount")
  @NotEmpty
  String amount;

  @CsvBindByName(column = "pay_currency")
  @NotEmpty
  String currency;

  @CsvBindByName(column = "pay_effectiveFrom")
  @NotEmpty
  String startDate;

  @CsvBindByName(column = "pay_effectiveTo")
  @NotEmpty
  String endDate;
}
