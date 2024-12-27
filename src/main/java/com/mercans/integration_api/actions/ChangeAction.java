package com.mercans.integration_api.actions;

import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Gender;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.Builder;

@Builder
public record ChangeAction(Gender employeGender, Set<@Valid PayComponent> payComponents)
    implements Action {

  @Override
  public ActionType getAction() {
    return ActionType.CHANGE;
  }
}
