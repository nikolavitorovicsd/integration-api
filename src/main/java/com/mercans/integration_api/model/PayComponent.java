package com.mercans.integration_api.model;

import com.mercans.integration_api.model.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PayComponent(
    @NotNull(message = "payComponent amount must not be null")
        @Positive(message = "payComponent amount must be positive number")
        BigDecimal amount,
    @NotNull(message = "payComponent currency must not be null") Currency currency,
    @NotNull(message = "payComponent startDate must not be null") LocalDate startDate,
    @NotNull(message = "payComponent terminationDate must not be null") LocalDate endDate) {}
