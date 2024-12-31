package com.mercans.integration_api.config;

import static com.mercans.integration_api.model.enums.ActionType.*;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.mercans.integration_api.config.listeners.BatchJobCache;
import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.mapper.UniversalMapper;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.ErrorStatistics;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.actions.HireAction;
import com.mercans.integration_api.model.actions.TerminateAction;
import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Slf4j
public class JsonProcessor implements ItemProcessor<EmployeeRecord, Action> {

  private final Validator validator;
  private final UniversalMapper universalMapper;
  private final BatchJobCache batchJobCache;

  public JsonProcessor(
      Validator validator, UniversalMapper universalMapper, BatchJobCache batchJobCache) {
    this.validator = validator;
    this.universalMapper = universalMapper;
    this.batchJobCache = batchJobCache;
  }

  @Override
  public Action process(EmployeeRecord employeeRecord) {
    Action action = null;
    try {
      ActionType actionType = getActionTypeFromCsvObject(employeeRecord.getAction(), false);

      var batchJobStatistics = batchJobCache.getStatistics();

      switch (actionType) {
        case HIRE -> {
          action =
              universalMapper.getActionMapper(actionType).mapToAction(employeeRecord, validator);

          HireAction hireAction = ((HireAction) action);

          if (batchJobStatistics.isEmployeeInDb(hireAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with employeeCode '%s' already exists in db and can't be added",
                    employeeRecord.getEmployeeCode()));
            return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
          if (batchJobStatistics.isHireEmployeeAlreadyProcessed(hireAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with employeeCode '%s' is duplicate in csv and will be skipped",
                    employeeRecord.getEmployeeCode()));
            return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          } else {
            batchJobStatistics.addToAlreadyProcessedHireEmployees(hireAction.employeeCode());
          }
        }
        case CHANGE -> {
          action =
              universalMapper.getActionMapper(actionType).mapToAction(employeeRecord, validator);

          ChangeAction changeAction = ((ChangeAction) action);

          if (batchJobStatistics.isNotPresentInDbAndNotProcessed(changeAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with employeeCode '%s' doesn't exists in db neither in already processed CSV lines and can't be updated!",
                    employeeRecord.getEmployeeCode()));
            return changeAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
        }
        case TERMINATE -> {
          action =
              universalMapper.getActionMapper(actionType).mapToAction(employeeRecord, validator);

          TerminateAction terminateAction = ((TerminateAction) action);

          if (batchJobStatistics.isNotPresentInDbAndNotProcessed(terminateAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with employeeCode '%s' and name '%s' doesn't exists in db neither in already processed CSV lines and can't be deleted!",
                    employeeRecord.getEmployeeCode()));
            return terminateAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
        }
      }
    } catch (RuntimeException exception) {

      if (exception instanceof UnskippableCsvException) {
        String message =
            String.format(
                "Record with employeeCode '%s' and name '%s' failed validation and will be skipped, reason: %s.",
                employeeRecord.getEmployeeCode(),
                employeeRecord.getEmployeeName(),
                exception.getLocalizedMessage());
        saveException(message);
        // we skip records that have unskippable exception
        return null;
      }
      throw new RuntimeException("Stop the application, something is unhandled properly!");
    }

    // this validates created action fields (e.g if hire action has null employeeCode)
    Set<ConstraintViolation<Action>> violations = validator.validate(action);
    if (isNotEmpty(violations)) {
      var validationMessages = violations.stream().map(ConstraintViolation::getMessage).toList();
      String message =
          String.format(
              "Record with employeeCode '%s' and name '%s' failed validation and will be skipped, reason: %s .",
              employeeRecord.getEmployeeCode(),
              employeeRecord.getEmployeeName(),
              validationMessages);
      saveException(message);
      return null;
    }

    return action;
  }

  private void saveException(String exceptionMessage) {
    ErrorStatistics errorStatistics = batchJobCache.getStatistics().getErrorStatistics();
    // increase error count
    errorStatistics.updateErrorCount();
    // add to error list
    errorStatistics.getErrors().add(exceptionMessage);
  }
}
