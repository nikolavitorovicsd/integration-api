package com.mercans.integration_api.model.enums;

import com.mercans.integration_api.exception.UnskippableCsvException;

public enum Currency {
  USD,
  EUR;

  public static Currency getCurrencyFromCsvObject(Object csvValue, boolean skippable) {
    try {
      var enumName = csvValue.toString().toUpperCase();
      return valueOf(enumName);
    } catch (IllegalArgumentException | NullPointerException exception) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format("Csv value '%s' couldn't be parsed to Currency", csvValue));
    }
  }
}
