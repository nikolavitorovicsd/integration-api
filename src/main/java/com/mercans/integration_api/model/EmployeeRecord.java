package com.mercans.integration_api.model;

import com.mercans.integration_api.model.converters.CSVActionEnumConverter;
import com.mercans.integration_api.model.converters.CSVCurrencyConverter;
import com.mercans.integration_api.model.converters.CSVDateConverter;
import com.mercans.integration_api.model.converters.CSVGenderEnumConverter;
import com.mercans.integration_api.model.enums.Action;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.model.enums.Gender;
import com.opencsv.bean.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Pojo class used to map CSV lines directly by using opencsv library
// required fields which absence will lead to skipping such employee are: 'ACTION','worker_name',
// 'contract_workStartDate'
// everything else will be handled in batch processor according to action type
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRecord {

  @CsvCustomBindByName(column = "ACTION", converter = CSVActionEnumConverter.class, required = true)
  Action action;

  @CsvBindByName(column = "worker_name", required = true)
  String employeeName;

  @CsvCustomBindByName(column = "worker_gender", converter = CSVGenderEnumConverter.class)
  Gender employeeGender;

  @CsvBindByName(column = "contract_workerId")
  String employeeCode; // no need to handle here, handle during processing

  @CsvCustomBindByName(
      column = "contract_workStartDate",
      converter = CSVDateConverter.class,
      required = true)
  LocalDate employeeContractStartDate;

  @CsvBindByName(column = "contract_endDate")
  String employeeContractEndDate; // no need to handle here, handle during processing

  // pay
  @CsvBindByName(column = "pay_amount")
  @CsvNumber("#")
  Double payAmount;

  @CsvCustomBindByName(column = "pay_currency", converter = CSVCurrencyConverter.class)
  Currency payCurrency;

  @CsvCustomBindByName(column = "pay_effectiveFrom", converter = CSVDateConverter.class)
  LocalDate payStartDate;

  @CsvCustomBindByName(column = "pay_effectiveTo", converter = CSVDateConverter.class)
  LocalDate payEndDate;

  // compensation
  @CsvBindByName(column = "compensation_amount")
  @CsvNumber("#")
  Double compensationAmount;

  @CsvCustomBindByName(column = "compensation_currency", converter = CSVCurrencyConverter.class)
  Currency compensationCurrency;

  @CsvCustomBindByName(column = "compensation_effectiveFrom", converter = CSVDateConverter.class)
  LocalDate compensationStartDate;

  @CsvCustomBindByName(column = "compensation_effectiveTo", converter = CSVDateConverter.class)
  LocalDate compensationEndDate;

  // for removal
  // todo remove: added to check what are other headers
  //  @CsvBindAndJoinByName(column = ".*", elementType = String.class)
  //  private MultiValuedMap<String, String> theRest;
}
