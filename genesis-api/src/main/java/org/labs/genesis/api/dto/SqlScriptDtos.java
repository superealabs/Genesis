package org.labs.genesis.api.dto;

public final class SqlScriptDtos {

    private SqlScriptDtos() {}

    public record SqlScriptRequest(
            String script,
            Boolean confirmExecution
    ) {}

    public record SqlValidationResponse(
            boolean valid,
            String message
    ) {}

    public record SqlExecutionResponse(
            boolean success,
            String message
    ) {}
}
