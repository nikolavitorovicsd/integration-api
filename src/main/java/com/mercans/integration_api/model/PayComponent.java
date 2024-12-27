package com.mercans.integration_api.model;

import com.mercans.integration_api.model.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PayComponent(
    @NotNull @Positive Long amount,
    @NotNull Currency currency,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate) {}
