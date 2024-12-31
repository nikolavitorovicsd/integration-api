package com.mercans.integration_api.model;

import com.mercans.integration_api.model.actions.Action;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
public record JsonResponse(
    @NotNull UUID uuid,
    @NotNull String fname,
    ErrorStatistics errors,
    // todo IMPORANT finish data populating
    //    Map<String, Object> data,
    @NotEmpty List<@Valid Action> payload) {}
