package com.mercans.integration_api.model;

import com.mercans.integration_api.actions.Action;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

// todo finish creation of JsonResponse
public record JsonResponse(
    @NotNull UUID uuid,
    @NotNull String fname,
    List<String> errors,
    @NotEmpty List<@Valid Action> payload) {}
