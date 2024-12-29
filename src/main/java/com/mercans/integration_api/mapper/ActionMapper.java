package com.mercans.integration_api.mapper;

import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.actions.Action;
import jakarta.validation.Validator;

public interface ActionMapper {

  Action mapToAction(EmployeeRecord employeeRecord, Validator validator);
}
