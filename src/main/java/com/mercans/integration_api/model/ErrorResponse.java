package com.mercans.integration_api.model;

import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record ErrorResponse(int errorCount, List<String> errors) {}
