package com.mercans.integration_api.model.enums;

public enum Currency {
  USD,
  EUR;

  public static Currency getCurrencyFromCsvObject(Object csvValue) {
    try {
      var enumName = csvValue.toString().toUpperCase();
      return valueOf(enumName);
    } catch (IllegalArgumentException | NullPointerException exception) {
      return null;
    }
  }
}
