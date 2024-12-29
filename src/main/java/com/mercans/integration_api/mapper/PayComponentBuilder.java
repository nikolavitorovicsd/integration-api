package com.mercans.integration_api.mapper;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.utils.DateUtils;
import java.time.LocalDate;
import java.util.List;

public class PayComponentBuilder {

  public List<PayComponent> buildPayComponents(EmployeeRecord employeeRecord) {
    // pay
    Long payAmount = getLongFromCsvObject(employeeRecord.getPayAmount(), true);
    Currency payCurrency = Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency(), true);
    LocalDate payStartDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayStartDate(), true);
    LocalDate payEndDate =
        DateUtils.getLocalDateFromCsvObject(employeeRecord.getPayEndDate(), true);

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
}
