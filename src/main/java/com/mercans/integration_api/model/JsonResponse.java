package com.mercans.integration_api.model;

import com.mercans.integration_api.actions.Action;
import com.mercans.integration_api.model.enums.ActionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// todo finish creation of JsonResponse
public record JsonResponse(
    @NotNull UUID uuid,
    @NotNull String fname,
    Map<ActionType, Map<String, Action>>
        errors, // todo not finished yet, should have more information on why was something rejected
    @NotEmpty List<@Valid Action> payload) {}
