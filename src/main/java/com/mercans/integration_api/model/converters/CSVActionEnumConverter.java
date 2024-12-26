package com.mercans.integration_api.model.converters;

import com.mercans.integration_api.model.enums.Action;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvException;
import jakarta.validation.ValidationException;
import lombok.SneakyThrows;

public class CSVActionEnumConverter extends AbstractBeanField<Action, String> {

  @SneakyThrows
  @Override
  protected Action convert(String value) {
    try {
      return Action.fromClientString(value);
    } catch (IllegalArgumentException | NullPointerException | ValidationException exception) {
      throw new CsvException(String.format("CONVERTER Invalid action format '%s'", value));
    }
  }
}
