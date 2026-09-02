package org.labs.genesis.api.dto.project;

import java.util.List;

public record ProjectConfigOptionsResponse(
        List<String> languageVersions,
        List<String> buildTools,
        List<String> frameworkVersions,
        boolean withGroupId
) {
}