package com.mercans.integration_api.model;

import com.opencsv.bean.*;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

// NOTE: in order to do validation in 1 place, i will skipp all converting in this bean and do it in
// batch process step
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EmployeeRecord implements Serializable {

  public static final EmployeeRecord EMPTY_RECORD = new EmployeeRecord();

  public static final String SYSTEM_ID = "SystemId";

  public static final String ACTION = "ACTION";
  public static final String WORKER_NAME = "worker_name";
  public static final String CONTRACT_WORK_START_DATE = "contract_workStartDate";
  public static final String WORKER_GENDER = "worker_gender";
  public static final String CONTRACT_WORKER_ID = "contract_workerId";
  public static final String CONTRACT_END_DATE = "contract_endDate";
  public static final String WORKER_PERSONAL_CODE = "worker_personalCode";

  // pay
  public static final String PAY_AMOUNT = "pay_amount";
  public static final String PAY_CURRENCY = "pay_currency";
  public static final String PAY_EFFECTIVE_FROM = "pay_effectiveFrom";
  public static final String PAY_EFFECTIVE_TO = "pay_effectiveTo";
  // compensation
  public static final String COMPENSATION_AMOUNT = "compensation_amount";
  public static final String COMPENSATION_CURRENCY = "compensation_currency";
  public static final String COMPENSATION_EFFECTIVE_FROM = "compensation_effectiveFrom";
  public static final String COMPENSATION_EFFECTIVE_TO = "compensation_effectiveTo";

  @CsvBindByName(column = SYSTEM_ID)
  Object systemId;

  @CsvBindByName(column = ACTION)
  Object action;

  @CsvBindByName(column = WORKER_NAME)
  Object employeeName;

  @CsvBindByName(column = CONTRACT_WORK_START_DATE)
  Object employeeContractStartDate;

  @CsvBindByName(column = WORKER_GENDER)
  Object employeeGender;

  @CsvBindByName(column = CONTRACT_WORKER_ID)
  Object employeeCode;

  @CsvBindByName(column = CONTRACT_END_DATE)
  Object employeeContractEndDate;

  @CsvBindByName(column = WORKER_PERSONAL_CODE)
  Object employeeBirthDate;

  // pay
  @CsvBindByName(column = PAY_AMOUNT)
  Object payAmount;

  @CsvBindByName(column = PAY_CURRENCY)
  Object payCurrency;

  @CsvBindByName(column = PAY_EFFECTIVE_FROM)
  Object payStartDate;

  @CsvBindByName(column = PAY_EFFECTIVE_TO)
  Object payEndDate;

  // compensation
  @CsvBindByName(column = COMPENSATION_AMOUNT)
  Object compensationAmount;

  @CsvBindByName(column = COMPENSATION_CURRENCY)
  Object compensationCurrency;

  @CsvBindByName(column = COMPENSATION_EFFECTIVE_FROM)
  Object compensationStartDate;

  @CsvBindByName(column = COMPENSATION_EFFECTIVE_TO)
  Object compensationEndDate;
}
