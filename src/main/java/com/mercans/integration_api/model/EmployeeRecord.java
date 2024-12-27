package com.mercans.integration_api.model;

import com.opencsv.bean.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Pojo class used to map CSV lines directly by using opencsv library
// required fields which absence will lead to skipping such employee are: 'ACTION','worker_name',
// 'contract_workStartDate'. If any of these are missing or have improper format, we skip the row
// during batch read
// everything else will be handled in batch processor according to action type.
// converter + required field of @Csv annotation handles skipping process
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRecord {

  // todo NOTE in order to do validation in 1 place, i will skipp all converting in this bean and do

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
  //  @CsvNumber("#")
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
