package org.labs.genesis.api.dto.project;

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