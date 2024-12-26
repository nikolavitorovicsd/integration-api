package com.mercans.integration_api.model;

import com.mercans.integration_api.model.enums.Action;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public record Payload(
    @NotNull String employeeCode,
    @NotNull Action action, // todo nikola should be String per requirement but i would use enum
    @NotNull Map<String, Object> data,
    @Valid List<PayComponent> payComponents) {}
