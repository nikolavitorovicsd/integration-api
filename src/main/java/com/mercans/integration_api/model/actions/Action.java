package com.mercans.integration_api.model.actions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mercans.integration_api.model.enums.ActionType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "action")
@JsonSubTypes({
  @JsonSubTypes.Type(value = HireAction.class, name = "HIRE"),
  @JsonSubTypes.Type(value = ChangeAction.class, name = "CHANGE"),
  @JsonSubTypes.Type(value = TerminateAction.class, name = "TERMINATE"),
})
public interface Action {

  @JsonIgnore
  ActionType getAction();

  public String getEmployeeCode();

  @JsonIgnore
  public boolean shouldBeSkippedDuringWrite();
}
