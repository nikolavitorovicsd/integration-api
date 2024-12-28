package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;

@Builder
public record HireAction(
    @NotNull String employeeCode,
    @NotNull LocalDate employeeHireDate,
    @NotNull String employeeFullName,
    Gender employeGender,
    LocalDate employeeBirthDate, // todo add in writter
    Set<@Valid PayComponent> payComponents)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.HIRE;
  }
}
