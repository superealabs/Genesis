package org.labs.genesis.api.dto.project;

public record ProjectConfigRequest(
        String projectName,
        String projectLocation,
        String languageVersion,
        String buildTool,
        String groupId,
        String frameworkVersion
) {
}