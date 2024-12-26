package com.mercans.integration_api.model.converters;

import com.mercans.integration_api.model.enums.Currency;
import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvException;
import lombok.SneakyThrows;

public class CSVCurrencyConverter extends AbstractBeanField<Currency, String> {

  @SneakyThrows
  @Override
  protected Currency convert(String value) {
    try {
      var enumName = value.toUpperCase();
      return Currency.valueOf(enumName);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new CsvException(String.format("CONVERTER Invalid currency format '%s'", value));
    }
  }
}
