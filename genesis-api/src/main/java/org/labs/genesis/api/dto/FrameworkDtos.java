package org.labs.genesis.api.dto;

public final class FrameworkDtos {

    private FrameworkDtos() {
    }

    public record FrameworkResponse(
            int id,
            int languageId,
            String name,
            String coreFramework,
            String type,
            boolean isProd,
            boolean useDB,
            boolean useCloud,
            boolean useEurekaServer,
            boolean isGateway,
            boolean useFrontendApp
    ) {
    }

    public record FrameworkSelectionResponse(
            boolean success,
            FrameworkResponse framework
    ) {
    }
}