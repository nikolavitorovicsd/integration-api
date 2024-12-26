package com.mercans.integration_api.model.converters;

import com.mercans.integration_api.model.enums.Gender;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvException;
import jakarta.validation.ValidationException;
import lombok.SneakyThrows;

public class CSVGenderEnumConverter extends AbstractBeanField<Gender, String> {

  @SneakyThrows
  @Override
  protected Gender convert(String value) {
    try {
      return Gender.fromClientString(value);
    } catch (IllegalArgumentException | NullPointerException | ValidationException exception) {
      throw new CsvException(String.format("CONVERTER Invalid gender format '%s'", value));
    }
  }
}
