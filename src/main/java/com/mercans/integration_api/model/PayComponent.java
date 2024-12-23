package com.mercans.integration_api.model;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PayComponent(
        @NotNull Double amount,
        @NotNull String currency, // todo nikola thinkg about using enum
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
