package com.mercans.integration_api.config;

import static com.mercans.integration_api.model.enums.ActionType.*;
import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.actions.ChangeAction;
import com.mercans.integration_api.actions.HireAction;
import com.mercans.integration_api.actions.TerminateAction;
import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
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
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class JsonProcessor implements ItemProcessor<EmployeeRecord, Action> {

  private final Validator validator;

  @Override
  public Action process(EmployeeRecord employeeRecord) {
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
      if (exception instanceof UnskippableCsvException) {
        return null;
      }
    }

    // todo handle action null case
    // this validates created action fields (e.g if hire action has null employeeCode)
    Set<ConstraintViolation<Action>> violations = validator.validate(action);
    if (isNotEmpty(violations)) {
      return null;
    }
    return action;
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

    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getPayCurrency(), true);

    var components = buildPayComponents(employeeRecord);

    return HireAction.builder()
        .employeeCode(employeeCode)
        .employeeHireDate(hireDate)
        .employeGender(employeeGender)
        .payComponents(components)
        .build();
  }

  private Action buildChangeAction(EmployeeRecord employeeRecord) throws UnskippableCsvException {
    if (isNull(employeeRecord.getEmployeeCode())) {
      throw new UnskippableCsvException("Missing 'employeeCode' for 'CHANGE' action");
    }
    Gender employeeGender = Gender.getGenderFromCsvObject(employeeRecord.getPayCurrency(), true);

    var components = buildPayComponents(employeeRecord);

    return ChangeAction.builder().employeGender(employeeGender).payComponents(components).build();
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

  private List<PayComponent> buildPayComponents(EmployeeRecord employeeRecord) {
    Long payAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), true);
    Currency payCurrency = Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), true);
    LocalDate payStartDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayStartDate(), true);
    LocalDate payEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), true);

    var payComponent =
        PayComponent.builder()
            .amount(payAmount)
            .currency(payCurrency)
            .startDate(payStartDate)
            .endDate(payEndDate)
            .build();

    Long compensationAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), true);
    Currency compensationCurrency =
        Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), true);
    LocalDate compensationStartDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayStartDate(), true);
    LocalDate compensationEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), true);

    var compensationComponent =
        PayComponent.builder()
            .amount(compensationAmount)
            .currency(compensationCurrency)
            .startDate(compensationStartDate)
            .endDate(compensationEndDate)
            .build();

    var components = List.of(payComponent, compensationComponent);

    // we validate payComponents and if any have violations we filter them out
    return components.stream().filter(component -> isEmpty(validator.validate(component))).toList();
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
    // todo nikola hardcoded change it
    return hireDate.toString() + "2B";
  }
}
