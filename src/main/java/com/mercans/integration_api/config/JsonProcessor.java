package com.mercans.integration_api.config;

import static com.mercans.integration_api.constants.GlobalConstants.BATCH_JOB_STATISTICS;
import static com.mercans.integration_api.model.enums.ActionType.*;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.actions.ChangeAction;
import com.mercans.integration_api.actions.HireAction;
import com.mercans.integration_api.actions.TerminateAction;
import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.BatchJobStatistics;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.ErrorStatistics;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.ActionType;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.model.enums.Gender;
import com.mercans.integration_api.utils.DateUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Slf4j
public class JsonProcessor implements ItemProcessor<EmployeeRecord, Action> {

  private final BatchJobStatistics batchJobStatistics;
  private final Validator validator;

  public JsonProcessor(
      @Value("#{jobExecutionContext['" + BATCH_JOB_STATISTICS + "']}")
          BatchJobStatistics batchJobStatistics,
      Validator validator) {
    this.batchJobStatistics = batchJobStatistics;
    this.validator = validator;
  }

  @Override
  public Action process(EmployeeRecord employeeRecord) {
    long startTime = System.currentTimeMillis();

    Action action = null;
    try {
      // if action throws exception
      ActionType actionType = getActionTypeFromCsvObject(employeeRecord.getAction(), false);

      switch (actionType) {
        case HIRE -> action = buildHireAction(employeeRecord);
        case CHANGE -> action = buildChangeAction(employeeRecord);
        case TERMINATE -> action = buildTerminateAction(employeeRecord);
      }
    } catch (RuntimeException exception) {
      saveException(exception.getMessage());

      if (exception instanceof UnskippableCsvException) {
        // we skip records that have unskippable exception
        return null;
      }
      log.warn("WASNT HANDLED!!!");
      throw new RuntimeException("BIG FAILURE!");
    }

    // this validates created action fields (e.g if hire action has null employeeCode)
    Set<ConstraintViolation<Action>> violations = validator.validate(action);
    if (isNotEmpty(violations)) {
      saveException(violations.toString());
      return null;
    }

    if (HIRE == action.getAction()) {

      HireAction hireAction = ((HireAction) action);

      if (batchJobStatistics.getEmployeeCodesThatExistInDb().contains(hireAction.employeeCode())) {

        saveException(
            String.format(
                "Record with employeeCode '%s' already exists in db and can't be added",
                employeeRecord.getEmployeeCode()));
        return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
      }
      if (batchJobStatistics
          .getHireEmployeesThatWereAlreadyProcessed()
          .contains(hireAction.employeeCode())) {
        saveException(
            String.format(
                "Record with employeeCode '%s' is duplicate in csv and will be skipped",
                employeeRecord.getEmployeeCode()));
        return hireAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
      } else {
        batchJobStatistics
            .getHireEmployeesThatWereAlreadyProcessed()
            .add(hireAction.employeeCode());
      }
    } else if (CHANGE == action.getAction()) {

      ChangeAction changeAction = ((ChangeAction) action);

      if (!batchJobStatistics.getEmployeeCodesThatExistInDb().contains(changeAction.employeeCode())
          && !batchJobStatistics
              .getHireEmployeesThatWereAlreadyProcessed()
              .contains(changeAction.employeeCode())) {

        saveException(
            String.format(
                "Record with employeeCode '%s' doesn't exists in db neither in already processed CSV lines and can't be updated!",
                employeeRecord.getEmployeeCode()));
        return changeAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
      }
    } else if (TERMINATE == action.getAction()) {

      TerminateAction terminateAction = ((TerminateAction) action);

      if (!batchJobStatistics
              .getEmployeeCodesThatExistInDb()
              .contains(terminateAction.employeeCode())
          && !batchJobStatistics
              .getHireEmployeesThatWereAlreadyProcessed()
              .contains(terminateAction.employeeCode())) {

        saveException(
            String.format(
                "Record with employeeCode '%s' doesn't exists in db neither in already processed CSV lines and can't be deleted!",
                employeeRecord.getEmployeeCode()));
        return terminateAction.toBuilder().shouldBeSkippedDuringWrite(true).build();
      }
    }

    log.warn(
        " _________________TIME OF PROCESS EXECUTION____________________: {} ms",
        System.currentTimeMillis() - startTime);


    return action;
  }

  private void saveException(String exceptionMessage) {
    ErrorStatistics errorStatistics = batchJobStatistics.getErrorStatistics();

    // increase error count
    errorStatistics.updateErrorCount();
    // add to error list
    errorStatistics.getErrors().add(exceptionMessage);
  }

  private Action buildHireAction(EmployeeRecord employeeRecord) throws UnskippableCsvException {
    if (isNull(employeeRecord.getEmployeeContractStartDate())
        && isNull(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException(
          "Missing 'employeeCode' and 'hireDate' for 'CHANGE' action");
    }

    // we will try to convert date and if conversion fails it will skip whole row, otherwise
    // it will set it as null which will also skip the row
    LocalDate hireDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getEmployeeContractStartDate(), false);
    String employeeCode =
        Optional.ofNullable((String) employeeRecord.getEmployeeCode())
            .orElseGet(() -> getEmployeeCodeFromStartDate(hireDate));
    String employeeFullName = (String) employeeRecord.getEmployeeName();

    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getEmployeeGender(), true);

    var components = buildPayComponents(employeeRecord);

    return HireAction.builder()
        .employeeCode(employeeCode)
        .employeeHireDate(hireDate)
        .employeeFullName(employeeFullName)
        .employeGender(employeeGender)
        .payComponents(components)
        .build();
  }

  private Action buildChangeAction(EmployeeRecord employeeRecord) throws UnskippableCsvException {
    if (isNull(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'CHANGE' action");
    }
    String employeeCode =
        Optional.ofNullable((String) employeeRecord.getEmployeeCode()).orElse(null);
    String employeeFullName = (String) employeeRecord.getEmployeeName();
    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getEmployeeGender(), true);

    var components = buildPayComponents(employeeRecord);

    return ChangeAction.builder()
        .employeeCode(employeeCode)
        .employeeFullName(employeeFullName)
        .employeGender(employeeGender)
        .payComponents(components)
        .build();
  }

  private Action buildTerminateAction(EmployeeRecord employeeRecord)
      throws UnskippableCsvException {
    if (isNull(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'TERMINATE' action");
    }
    LocalDate terminationDate =
        Optional.ofNullable(employeeRecord.getEmployeeContractEndDate())
            // if termination date fails to convert, we throw unskippable exception
            .map(date -> DateUtils.getLocalDateFromCsvObject(date, false))
            .orElse(LocalDate.now());

    return TerminateAction.builder()
        .employeeCode((String) employeeRecord.getEmployeeCode())
        .terminationDate(terminationDate)
        .build();
  }

  private Set<PayComponent> buildPayComponents(EmployeeRecord employeeRecord) {
    // pay
    Long payAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), true);
    Currency payCurrency = Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), true);
    LocalDate payStartDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayStartDate(), true);
    LocalDate payEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), true);

    // todo can be extracted
    var payComponent = buildPayComponent(payAmount, payCurrency, payStartDate, payEndDate);

    // compensation
    Long compensationAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), true);
    Currency compensationCurrency =
        Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), true);
    LocalDate compensationStartDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayStartDate(), true);
    LocalDate compensationEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), true);

    var compensationComponent =
        buildPayComponent(
            compensationAmount, compensationCurrency, compensationStartDate, compensationEndDate);

    var components = List.of(payComponent, compensationComponent);

    // we validate payComponents and if any have violations we filter them out
    return components.stream()
        .filter(component -> isEmpty(validator.validate(component)))
        .collect(Collectors.toSet());
  }

  private static PayComponent buildPayComponent(
      Long payAmount, Currency payCurrency, LocalDate payStartDate, LocalDate payEndDate) {
    return PayComponent.builder()
        .amount(payAmount)
        .currency(payCurrency)
        .startDate(payStartDate)
        .endDate(payEndDate)
        .build();
  }

  private Long getLongFromCsvObject(Object payAmount, boolean skippable) {
    try {
      return Long.parseLong(payAmount.toString());
    } catch (NumberFormatException | NullPointerException exception) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format("Csv value '%s' couldn't be parsed to Long", payAmount));
    }
  }

  private String getEmployeeCodeFromStartDate(LocalDate hireDate) {
    // todo nikola hardcoded change it now it looks like "employeeCode": "2022-01-012B",
    // date comes as "2022-01-12", after replace it looks like "20220112", after substring "220112"
    return hireDate.toString().replace("-", "").substring(2, 8) + "2B";
  }
}
