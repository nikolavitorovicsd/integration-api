package com.mercans.integration_api.model.converters;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class EmployeeCodeConverter extends AbstractCsvConverter {

  @Override
  public Object convertToRead(String value)
      throws CsvDataTypeMismatchException, CsvConstraintViolationException {
    return value;
  }
}
