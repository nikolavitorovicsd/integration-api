package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Builder
public record TerminateAction(@NotNull String employeeCode, @NotNull LocalDate terminationDate)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.TERMINATE;
  }
}