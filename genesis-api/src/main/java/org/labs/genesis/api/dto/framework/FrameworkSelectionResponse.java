package org.labs.genesis.api.dto.framework;

public record FrameworkSelectionResponse(
        boolean success,
        FrameworkResponse framework
) {
}