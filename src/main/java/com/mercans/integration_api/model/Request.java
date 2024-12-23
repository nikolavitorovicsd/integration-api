package com.mercans.integration_api.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record Request(
    @NotNull UUID uuid, // string in pdf which is strange
    @NotNull String fname, // should be csv file name
    List<String> errors,
    @NotEmpty List<@Valid Payload> payload) {}
