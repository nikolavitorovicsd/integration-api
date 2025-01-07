package com.mercans.integration_api.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import lombok.Builder;

@Builder
public record QueryArgHolder(
    Long[] employeesIds,
    String[] employeesFullNames,
    String[] employeesCodes,
    Date[] employeesHireDates,
    String[] employeesGenders,
    Date[] employeesBirthDates,
    Timestamp[] employeesCreationDates,
    Timestamp[] employeesModificationDates,
    Long[] componentsIds,
    Long[] componentEmployeeIds,
    BigDecimal[] componentsAmounts,
    String[] componentsCurrencies,
    Date[] componentsStartDates,
    Date[] componentsEndDates) {}
