package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class ChangeAction implements Action {

  private final Gender employeGender;
  private final List<@Valid PayComponent> payComponents;

  @Override
  public ActionType getAction() {
    return ActionType.TERMINATE;
  }
}
