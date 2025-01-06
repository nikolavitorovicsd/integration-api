package com.mercans.integration_api.mapper;

import static com.mercans.integration_api.model.EmployeeRecord.*;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.utils.DateUtils;
import java.time.LocalDate;
import java.util.List;

public class PayComponentMapper {

  public List<PayComponent> buildPayComponents(EmployeeRecord employeeRecord) {
    // pay
    Long payAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), PAY_AMOUNT, true);
    Currency payCurrency =
        Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), PAY_CURRENCY, true);
    LocalDate payStartDate =
        DateUtils.getLocalDateFromCsvObject(
            employeeRecord.getPayStartDate(), PAY_EFFECTIVE_FROM, true);
    LocalDate payEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), PAY_EFFECTIVE_TO, true);

    var payComponent = buildPayComponent(payAmount, payCurrency, payStartDate, payEndDate);

    // compensation
    Long compensationAmount =
        getLongFromCsvObject(employeeRecord.getCompensationAmount(), COMPENSATION_AMOUNT, true);
    Currency compensationCurrency =
        Currency.getCurrencyFromCsvObject(
            employeeRecord.getCompensationCurrency(), COMPENSATION_CURRENCY, true);
    LocalDate compensationStartDate =
        DateUtils.getLocalDateFromCsvObject(
            employeeRecord.getCompensationStartDate(), COMPENSATION_EFFECTIVE_FROM, true);
    LocalDate compensationEndDate =
        DateUtils.getLocalDateFromCsvObject(
            employeeRecord.getCompensationEndDate(), COMPENSATION_EFFECTIVE_TO, true);

    var compensationComponent =
        buildPayComponent(
            compensationAmount, compensationCurrency, compensationStartDate, compensationEndDate);

    return List.of(payComponent, compensationComponent);
  }

  private PayComponent buildPayComponent(
      Long payAmount, Currency payCurrency, LocalDate payStartDate, LocalDate payEndDate) {
    return PayComponent.builder()
        .amount(payAmount)
        .currency(payCurrency)
        .startDate(payStartDate)
        .endDate(payEndDate)
        .build();
  }

  private Long getLongFromCsvObject(Object csvValue, String fieldName, boolean skippable) {
    try {
      return Long.parseLong(csvValue.toString());
    } catch (NumberFormatException | NullPointerException exception) {
      if (skippable) {
        return null;
      }
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' couldn't be parsed to Long", csvValue, fieldName));
    }
  }
}
