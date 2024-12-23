package com.mercans.integration_api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestEntry(
        @JsonProperty(value = "ACTION", required = true) String action,
        @JsonProperty(value = "worker_name", required = true) String name
) {
}
