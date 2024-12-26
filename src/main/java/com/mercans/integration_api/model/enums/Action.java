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
public enum Action {
  HIRE("add"),
  CHANGE("update"),
  TERMINATE("delete");

  private static final Map<String, Action> MAP =
      Stream.of(Action.values()).collect(toMap(value -> value.clientValue, Function.identity()));

  private final String clientValue;

  public static Action fromClientString(String value) {
    var lowerCaseValue = value.toLowerCase();
    return Optional.ofNullable(MAP.get(lowerCaseValue))
        .orElseThrow(
            () -> new ValidationException(String.format("Unsupported ACTION type '%s'", value)));
  }
}
