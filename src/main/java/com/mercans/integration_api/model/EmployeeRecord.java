package com.mercans.integration_api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.mercans.integration_api.model.converters.CSVCurrencyConverter;
import com.mercans.integration_api.model.converters.CSVDateConverter;
import com.opencsv.bean.CsvBindAndJoinByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvNumber;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

// Pojo class used to map CSV lines directly by using opencsv library
@Getter
// todo IMPORTANT getter must be present or nothing is showed in response -_-
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRecord {

  // todo check different csv formats, they might pass integer or double or null
  @CsvBindByName(column = "ACTION", required = true)
  String action;

  @CsvBindByName(column = "worker_name", required = true)
  String employeeName;

  // todo remove: added to check what are other headers
  //  @CsvBindAndJoinByName(column = ".*", elementType = String.class)
  //  private MultiValuedMap<String, String> theRest;

  @CsvBindAndJoinByName(
      column = "contract_work.*", // this will map both contract_workStartDate abd contract_workerId
      elementType = String.class
      // converter = converter = EmployeeCodeConverter.class
      )
  // TODO there is issue with header contract_workStartDate ,its not being read from csv
  @JsonDeserialize(as = HashSetValuedHashMap.class)
  MultiValuedMap<String, String> employeeCode;

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
  Object compensationStartDate;

  @CsvCustomBindByName(column = "compensation_effectiveTo", converter = CSVDateConverter.class)
  Object compensationEndDate;
}
