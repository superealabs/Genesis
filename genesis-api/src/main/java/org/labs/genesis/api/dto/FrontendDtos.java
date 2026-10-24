package org.labs.genesis.api.dto;

import java.util.List;

public final class FrontendDtos {
    private FrontendDtos() {}

    public record FrameworkOption(int id, String name, String coreFramework, int languageId, String defaultPort) {}

    public record FrontendConfigRequest(Integer frameworkId, Integer languageId, String port) {}

    public record FrontendConfigResponse(boolean success, int frameworkId, int languageId, String port) {}

    public record ProgrammingLanguageOption(
            int id,
            String name,
            String extension
    ) {}

    public record InterfaceLanguageOption(
            int id,
            String code,
            String name
    ) {}

    public record FrontendLayoutRequest(
            List<String> selectedInterfaceLanguages,
            String navbarType,
            String primaryColor,
            String secondaryColor,
            String logoPath,
            String faviconPath,
            String port,
            String logoAssetId,
            String faviconAssetId
    ) {}
}
