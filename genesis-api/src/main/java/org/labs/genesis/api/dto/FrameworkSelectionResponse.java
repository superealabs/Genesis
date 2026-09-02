package org.labs.genesis.api.dto;

public record FrameworkSelectionResponse(
        boolean success,
        FrameworkResponse framework
) {
}