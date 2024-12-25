package com.mercans.integration_api.config;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.mercans.integration_api.model.EmployeeRecord;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

// todo nikola in progress or might even not be needed as reader is working fine
@Component
@StepScope
@RequiredArgsConstructor
public class JsonProcessor implements ItemProcessor<EmployeeRecord, EmployeeRecord> {

  private final Validator validator;

  @Override
  public EmployeeRecord process(EmployeeRecord employeeRecord) throws Exception {

    // if there are validation exceptions we skip that line
    Set<ConstraintViolation<EmployeeRecord>> violations = validator.validate(employeeRecord);

    if (isNotEmpty(violations)) {
      throw new ValidationException(violations.toString());
    }

    return employeeRecord;
  }
}
