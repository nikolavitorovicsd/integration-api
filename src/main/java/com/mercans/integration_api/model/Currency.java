package com.mercans.integration_api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public enum Currency {
  USD,
  EUR;

  @JsonCreator
  public static Currency fromClientString(@NotNull @NotEmpty Object value) {
    try {
      var enumName = value.toString().toUpperCase();
      return valueOf(enumName);
    } catch (IllegalArgumentException | NullPointerException exception) {
      throw new ValidationException(String.format("ENUM Invalid currency format '%s'", value));
    }
  }
}
