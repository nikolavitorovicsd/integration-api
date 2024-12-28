package com.mercans.integration_api.model;

import com.opencsv.bean.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


// NOTE: in order to do validation in 1 place, i will skipp all converting in this bean and do it in batch process step
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRecord {


  @CsvBindByName(column = "ACTION")
  Object action;

  @CsvBindByName(column = "worker_name")
  Object employeeName;

  @CsvBindByName(column = "contract_workStartDate")
  Object employeeContractStartDate;

  @CsvBindByName(column = "worker_gender")
  Object employeeGender;

  @CsvBindByName(column = "contract_workerId")
  Object employeeCode;

  @CsvBindByName(column = "contract_endDate")
  Object employeeContractEndDate;

  // pay
  @CsvBindByName(column = "pay_amount")
  Object payAmount;

  @CsvBindByName(column = "pay_currency")
  Object payCurrency;

  @CsvBindByName(column = "pay_effectiveFrom")
  Object payStartDate;

  @CsvBindByName(column = "pay_effectiveTo")
  Object payEndDate;

  // compensation
  @CsvBindByName(column = "compensation_amount")
  Object compensationAmount;

  @CsvBindByName(column = "compensation_currency")
  Object compensationCurrency;

  @CsvBindByName(column = "compensation_effectiveFrom")
  Object compensationStartDate;

  @CsvBindByName(column = "compensation_effectiveTo")
  Object compensationEndDate;
}
