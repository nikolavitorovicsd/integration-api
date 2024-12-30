package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Builder(toBuilder = true)
public record TerminateAction(
    @NotNull(message = "employeeCode must not be null") String employeeCode,
    @NotNull(message = "terminationDate must not be null") LocalDate terminationDate,
    @JsonIgnore boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.TERMINATE;
  }
}
