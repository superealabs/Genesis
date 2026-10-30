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
            boolean useFrontendApp,
            boolean withGroupId
    ) {
    }


    public record FrameworkSelectionResponse(
            boolean success,
            FrameworkResponse framework
    ) {
    }

    public record LanguageResponse(
            int id,
            String name,
            String extension
    ) {
    }

    public record CoreFrameworkResponse(
            int id,
            String name
    ) {
    }

    public record ViewTemplateResponse(
            int id,
            String name
    ) {
    }
}