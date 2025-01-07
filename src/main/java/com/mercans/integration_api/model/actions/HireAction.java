package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder(toBuilder = true)
public record HireAction(
    @NotNull(message = "employeeCode must not be null")
        @Size(max = 8, message = "employeeCode must not exceed 8 characters.")
        String employeeCode,
    @NotNull(message = "employeeHireDate must not be null") LocalDate employeeHireDate,
    @NotNull(message = "employeeFullName must not be null") String employeeFullName,
    Gender employeGender,
    LocalDate employeeBirthDate,
    Map<String, Object> data,
    List<@Valid PayComponent> payComponents,
    @JsonIgnore boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.HIRE;
  }

  @Override
  public String getEmployeeCode() {
    return employeeCode;
  }
}
