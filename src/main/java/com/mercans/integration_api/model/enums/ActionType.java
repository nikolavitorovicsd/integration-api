package com.mercans.integration_api.model.enums;

import static java.util.stream.Collectors.toMap;

import com.mercans.integration_api.exception.UnskippableCsvException;
import jakarta.validation.ValidationException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ActionType {
  HIRE("add"),
  CHANGE("update"),
  TERMINATE("delete");

  private static final Map<String, ActionType> MAP =
      Stream.of(ActionType.values())
          .collect(toMap(value -> value.clientValue, Function.identity()));

  private final String clientValue;

  public static ActionType fromClientString(String value) {
    var lowerCaseValue = value.toLowerCase();
    return Optional.ofNullable(MAP.get(lowerCaseValue))
        .orElseThrow(
            () -> new ValidationException(String.format("Unsupported ACTION type '%s'", value)));
  }

  public static ActionType getActionTypeFromCsvObject(Object csvValue, String fieldName) {
    try {
      String actionStringValue = csvValue.toString();
      return fromClientString(actionStringValue);
    } catch (NullPointerException | ValidationException exception) {
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' couldn't be parsed to ActionType",
              csvValue, fieldName));
    }
  }
}
