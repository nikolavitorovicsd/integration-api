package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mercans.integration_api.model.enums.ActionType;
import java.time.LocalDate;
import java.util.Map;
import lombok.*;

@Builder(toBuilder = true)
public record TerminateAction(
    String employeeCode,
    LocalDate terminationDate,
    Map<String, Object> data,
    @JsonIgnore boolean shouldBeSkippedDuringWrite)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.TERMINATE;
  }

  @Override
  public String getEmployeeCode() {
    return employeeCode;
  }
}
