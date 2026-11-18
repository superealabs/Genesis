package org.labs.genesis.api.dto;

import java.util.List;

public final class ScriptAiDtos {

    private ScriptAiDtos() {
    }

    public record LlmModelResponse(int id, String name, String model) {
    }

    public record GenerateSqlRequest(
            Integer databaseId,
            Integer llmModelId,
            String prompt,
            String currentScript,
            Boolean includeCurrentScript,
            String apiKey
    ) {
    }

    public record GenerateSqlResponse(
            boolean success,
            String database,
            String model,
            String script,
            List<String> tables,
            String warningMessage
    ) {
    }
}