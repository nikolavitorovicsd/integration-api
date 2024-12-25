package com.mercans.integration_api.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Builder;

@Builder
public record PayComponent(
    @NotNull Double amount,
    @NotNull Currency currency,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate) {}
