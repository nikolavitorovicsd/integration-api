package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;

@Builder(toBuilder = true)
public record HireAction(
    String employeeCode,
    LocalDate employeeHireDate,
    String employeeFullName,
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
