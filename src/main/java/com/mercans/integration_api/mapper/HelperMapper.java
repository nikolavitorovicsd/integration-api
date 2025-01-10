package com.mercans.integration_api.mapper;

import static com.mercans.integration_api.model.EmployeeRecord.*;

import com.mercans.integration_api.exception.UnskippableCsvException;
import com.mercans.integration_api.model.EmployeeRecord;
import com.mercans.integration_api.model.PayComponent;
import com.mercans.integration_api.model.enums.Currency;
import com.mercans.integration_api.utils.DateUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class HelperMapper {

  public List<PayComponent> buildPayComponents(EmployeeRecord employeeRecord) {
    // pay
    BigDecimal payAmount = getBigDecimalFromCsvObject(employeeRecord.getPayAmount());
    Currency payCurrency = Currency.getCurrencyFromCsvObject(employeeRecord.getPayCurrency());
    LocalDate payStartDate =
        DateUtils.getLocalDateForSalaryFromCsvObject(employeeRecord.getPayStartDate());
    LocalDate payEndDate =
        DateUtils.getLocalDateForSalaryFromCsvObject(employeeRecord.getPayEndDate());

    var payComponent = buildPayComponent(payAmount, payCurrency, payStartDate, payEndDate);

    // compensation
    BigDecimal compensationAmount =
        getBigDecimalFromCsvObject(employeeRecord.getCompensationAmount());
    Currency compensationCurrency =
        Currency.getCurrencyFromCsvObject(employeeRecord.getCompensationCurrency());
    LocalDate compensationStartDate =
        DateUtils.getLocalDateForSalaryFromCsvObject(employeeRecord.getCompensationStartDate());
    LocalDate compensationEndDate =
        DateUtils.getLocalDateForSalaryFromCsvObject(employeeRecord.getCompensationEndDate());

    var compensationComponent =
        buildPayComponent(
            compensationAmount, compensationCurrency, compensationStartDate, compensationEndDate);

    return List.of(payComponent, compensationComponent);
  }

  private PayComponent buildPayComponent(
      BigDecimal payAmount, Currency payCurrency, LocalDate payStartDate, LocalDate payEndDate) {
    return PayComponent.builder()
        .amount(payAmount)
        .currency(payCurrency)
        .startDate(payStartDate)
        .endDate(payEndDate)
        .build();
  }

  private BigDecimal getBigDecimalFromCsvObject(Object csvValue) {
    try {
      return new BigDecimal((String) csvValue).setScale(0, RoundingMode.HALF_UP);
    } catch (NumberFormatException | NullPointerException exception) {
      return null;
    }
  }

  protected String getEmployeeFullName(EmployeeRecord employeeRecord) {
    String employeeFullName = (String) employeeRecord.getEmployeeName();

    if (StringUtils.isEmpty(employeeFullName)) {
      throw new UnskippableCsvException(
          String.format(
              "Csv value '%s' for field '%s' can't be empty",
              employeeRecord.getEmployeeName(), WORKER_NAME));
    }
    return employeeFullName;
  }
}
