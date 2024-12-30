package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;

@Builder(toBuilder = true)
public record HireAction(
    @NotNull(message = "employeeCode must not be null") String employeeCode,
    @NotNull(message = "employeeHireDate must not be null") LocalDate employeeHireDate,
    @NotNull(message = "employeeFullName must not be null") String employeeFullName,
    Gender employeGender,
    LocalDate employeeBirthDate,
    Set<@Valid PayComponent> payComponents,
    @JsonIgnore boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.HIRE;
  }
}
