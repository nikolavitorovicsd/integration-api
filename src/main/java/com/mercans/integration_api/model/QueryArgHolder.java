package com.mercans.integration_api.model;

import java.math.BigDecimal;
import java.sql.Date;
import lombok.Builder;

@Builder
public record QueryArgHolder(
    Long[] employeesIds,
    String[] employeesFullNames,
    String[] employeesCodes,
    Date[] employeesHireDates,
    String[] employeesGenders,
    Date[] employeesBirthDates,
    Long[] componentsIds,
    Long[] componentEmployeeIds,
    BigDecimal[] componentsAmounts,
    String[] componentsCurrencies,
    Date[] componentsStartDates,
    Date[] componentsEndDates) {}
