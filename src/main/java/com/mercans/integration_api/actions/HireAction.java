package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class HireAction implements Action {

  @NotNull private final String employeeCode;
  @NotNull private final LocalDate employeeHireDate;
  private final Gender employeGender;
  private final List<@Valid PayComponent> payComponents;

  @Override
  public ActionType getAction() {
    return ActionType.HIRE;
  }
}
