package com.mercans.integration_api.exception.handlers;

import com.opencsv.bean.exceptionhandler.CsvExceptionHandler;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

// todo customize all kinds of exceptions regarding issues with csv structure like nummber of
// fields not matching header
// todo add aditional handling for all possible issues with CSV (investigate)
public class CsvReadCustomExceptionHandler implements CsvExceptionHandler {
  @Override
  public CsvException handleException(CsvException exception) throws CsvException {

    // if row has fewer fields than header columns we skip it
    if (exception instanceof CsvRequiredFieldEmptyException) {
      return null;
    }
    // todo nikola try with custom created exceptions  from CsvException
    //    if(exception instanceof CsvConstraintViolationException) {
    //      return null;
    //    }
    return exception;
  }
}
