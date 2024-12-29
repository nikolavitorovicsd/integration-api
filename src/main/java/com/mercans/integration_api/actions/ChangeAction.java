package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record ChangeAction(
    @NotNull String employeeCode,
    @NotNull String employeeFullName,
    Gender employeGender,
    LocalDate employeeBirthDate, // todo add in writter
    Set<@Valid PayComponent> payComponents,
    boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.CHANGE;
  }
}
