package com.mercans.integration_api.model.enums;

import static java.util.stream.Collectors.toMap;

import jakarta.validation.ValidationException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Gender {
  M("male"),
  F("female");

  private static final Map<String, Gender> MAP =
      Stream.of(Gender.values()).collect(toMap(value -> value.clientValue, Function.identity()));

  private final String clientValue;

  public static Gender fromCsvValue(String value) throws ValidationException {
    var lowerCaseValue = value.toLowerCase();
    return Optional.ofNullable(MAP.get(lowerCaseValue))
        .orElseThrow(
            () -> new ValidationException(String.format("Unsupported Gender type '%s'", value)));
  }

  public static Gender getGenderFromCsvObject(Object csvValue) {
    try {
      var enumName = csvValue.toString();
      return fromCsvValue(enumName);
    } catch (IllegalArgumentException | ValidationException | NullPointerException exception) {
      return null;
    }
  }
}
