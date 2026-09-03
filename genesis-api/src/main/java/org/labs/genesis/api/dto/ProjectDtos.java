package org.labs.genesis.api.dto;

import java.util.List;

public final class ProjectDtos {
    private ProjectDtos() {
    }

    public record ProjectConfigOptionsResponse(
            List<String> languageVersions,
            List<String> buildTools,
            List<String> frameworkVersions,
            boolean withGroupId
    ) {
    }

    public record ProjectConfigRequest(
            String projectName,
            String projectLocation,
            String languageVersion,
            String buildTool,
            String groupId,
            String frameworkVersion
    ) {
    }

    public record ProjectConfigResponse(
            boolean success,
            String projectName,
            String projectLocation,
            String languageVersion,
            String buildTool,
            String groupId,
            String frameworkVersion
    ) {
    }
}
