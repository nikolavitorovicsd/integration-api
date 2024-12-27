package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

@Getter
@Valid
@RequiredArgsConstructor
@Builder
public class TerminateAction implements Action {

  @NotNull private final String employeeCode;
  @NotNull private final LocalDate terminationDate;

  @Override
  public ActionType getAction() {
    return ActionType.TERMINATE;
  }
}
