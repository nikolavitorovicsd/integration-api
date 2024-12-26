package com.mercans.integration_api.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.ValidationException;

public enum Currency {
  USD,
  EUR;

  @JsonCreator
  public static Currency fromClientString(
      String value) { // todo think about receiveing an object instead of String
    try {
      var enumName = value.toUpperCase();
      return valueOf(enumName);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new ValidationException(String.format("ENUM Invalid currency format '%s'", value));
    }
  }
}
