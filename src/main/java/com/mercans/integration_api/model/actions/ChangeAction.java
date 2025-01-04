package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

@Builder(toBuilder = true)
public record ChangeAction(
    @NotNull(message = "employeeCode must not be null") String employeeCode,
    @NotNull(message = "employeeFullName must not be null") String employeeFullName,
    Gender employeGender,
    LocalDate employeeBirthDate,
    @UniqueElements List<@Valid PayComponent> payComponents,
    @JsonIgnore boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.CHANGE;
  }

  @Override
  public String getEmployeeCode() {
    return employeeCode;
  }
}
