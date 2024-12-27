package com.mercans.integration_api.exception.handlers;

import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class CsvReadCustomExceptionHandler implements CsvExceptionHandler {
  @Override
  public CsvException handleException(CsvException exception) throws CsvException {

    // if row has fewer fields than header columns we skip it
    if (exception instanceof CsvRequiredFieldEmptyException) {
      return null;
    }
    // todo keep playing with csv to probe if something fails batch process
    return exception;
  }
}
