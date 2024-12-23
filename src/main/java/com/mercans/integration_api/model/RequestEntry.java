package com.mercans.integration_api.model;

import com.opencsv.bean.CsvBindByName;

public class RequestEntry {

  @CsvBindByName(column = "ACTION")
  String action;

  @CsvBindByName(column = "worker_name")
  String name;

  @CsvBindByName(column = "contract_workerId")
  String employeeCode;
  //     @CsvBindByName(column = "contract_workerId") String payComponents;

}
