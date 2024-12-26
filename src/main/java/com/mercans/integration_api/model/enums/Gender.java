package com.mercans.integration_api.model.enums;

import static io.micrometer.common.util.StringUtils.isEmpty;
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

  public static Gender fromClientString(String value) {
    if (isEmpty(value)) {
      return null; // its nullable in db
    }
    var lowerCaseValue = value.toLowerCase();
    return Optional.ofNullable(MAP.get(lowerCaseValue))
        .orElseThrow(
            () -> new ValidationException(String.format("Unsupported Gender type '%s'", value)));
  }
}
