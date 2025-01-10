package com.mercans.integration_api.config;

import static com.mercans.integration_api.model.EmployeeRecord.ACTION;
import static com.mercans.integration_api.model.enums.ActionType.*;

import com.mercans.integration_api.cache.BatchJobCache;
import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.mapper.UniversalMapper;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.ErrorStatistics;
import com.mercans.integration_api.model.actions.Action;
import com.mercans.integration_api.model.actions.ChangeAction;
import com.mercans.integration_api.model.actions.HireAction;
import com.mercans.integration_api.model.actions.TerminateAction;
import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@Slf4j
public class CsvLinesProcessor implements ItemProcessor<EmployeeRecord, Action> {

  private final Validator validator;
  private final UniversalMapper universalMapper;
  private final BatchJobCache batchJobCache;

  public CsvLinesProcessor(
      Validator validator, UniversalMapper universalMapper, BatchJobCache batchJobCache) {
    this.validator = validator;
    this.universalMapper = universalMapper;
    this.batchJobCache = batchJobCache;
  }

  @Override
  public Action process(EmployeeRecord employeeRecord) {
    try {
      Action action = null;

      ActionType actionType = getActionTypeFromCsvObject(employeeRecord.getAction(), ACTION);

      var batchJobStatistics = batchJobCache.getStatistics();

      switch (actionType) {
        case HIRE -> {
          action =
              universalMapper.getActionMapper(actionType).mapToAction(employeeRecord, validator);

          HireAction hireAction = ((HireAction) action);

          if (batchJobStatistics.isEmployeeInDb(hireAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with systemId '%s' and employeeCode '%s' already exists in db and can't be added",
                    employeeRecord.getSystemId(), hireAction.employeeCode()));
            return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
          if (batchJobStatistics.isHireEmployeeAlreadyProcessed(hireAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with systemId '%s' and employeeCode '%s' is duplicate in csv and will be skipped",
                    employeeRecord.getSystemId(), hireAction.employeeCode()));
            return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
        }
        case CHANGE -> {
          action =
              universalMapper.getActionMapper(actionType).mapToAction(employeeRecord, validator);

          ChangeAction changeAction = ((ChangeAction) action);

          if (batchJobStatistics.isNotPresentInDbAndNotProcessed(changeAction.employeeCode())) {
            saveException(
                String.format(
                    "Record with systemId '%s' and employeeCode '%s' doesn't exists in db neither in already processed CSV lines and can't be updated!",
                    employeeRecord.getSystemId(), changeAction.employeeCode()));
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
                    "Record with systemId '%s' and employeeCode '%s' doesn't exists in db neither in already processed CSV lines and can't be deleted!",
                    employeeRecord.getSystemId(), terminateAction.employeeCode()));
            return terminateAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
          }
        }
      }

      // we add processed valid hire action to cache
      if (ActionType.HIRE.equals(action.getAction())) {
        batchJobStatistics.addToAlreadyProcessedHireEmployees(action.getEmployeeCode());
      }

      return action;

    } catch (RuntimeException exception) {
      if (exception instanceof UnskippableCsvException) {
        String message =
            String.format(
                "Record with systemId '%s' failed validation and will be skipped, reason: %s.",
                employeeRecord.getSystemId(), exception.getLocalizedMessage());
        saveException(message);
        // we skip records that have unskippable exception
        return null;
      }
      throw new RuntimeException("Stop the application, something is unhandled properly!");
    }
  }

  private void saveException(String exceptionMessage) {
    ErrorStatistics errorStatistics = batchJobCache.getStatistics().getErrorStatistics();
    // increase error count
    errorStatistics.updateErrorCount();
    // add to error list
    errorStatistics.getErrors().add(exceptionMessage);
  }
}
