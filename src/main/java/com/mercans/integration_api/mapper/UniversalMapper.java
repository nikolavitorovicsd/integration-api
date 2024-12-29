package com.mercans.integration_api.mapper;

import com.mercans.integration_api.model.enums.ActionType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UniversalMapper {

  private static final Map<ActionType, ? extends ActionMapper> actionMappersByType =
      Map.of(
          ActionType.HIRE, new HireActionMapper(),
          ActionType.CHANGE, new ChangeActionMapper(),
          ActionType.TERMINATE, new TerminateActionMapper());

  public ActionMapper getActionMapper(ActionType actionType) {
    return actionMappersByType.get(actionType);
  }
}
