package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.SqlScriptDtos.*;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.connexion.SQLRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Connection;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SqlScriptService {

    private static final int MAX_SCRIPT_LENGTH = 65536;

    private final GenerationContextStore contextStore;
    private final boolean executionEnabled;

    private final AtomicReference<ScriptConfigRequest> savedScript = new AtomicReference<>();

    public SqlScriptService(GenerationContextStore contextStore, @Value("${genesis.sql.execution-enabled:false}") boolean executionEnabled) {
        this.contextStore = contextStore;
        this.executionEnabled = executionEnabled;
    }

    public SqlValidationResponse validate(SqlScriptRequest request) {
        if (request == null
                || request.script() == null
                || request.script().isBlank()) {
            return new SqlValidationResponse(false, "Le script SQL est vide");
        }

        if (request.script().length() > MAX_SCRIPT_LENGTH) {
            return new SqlValidationResponse(false, "Le script SQL est trop volumineux");
        }

        if (SQLRunner.removeComments(request.script()).isBlank()) {
            return new SqlValidationResponse(false, "Le script ne contient aucune instruction");
        }

        return new SqlValidationResponse(true, "Format de base accepté. Syntaxe SQL non vérifiée.");
    }

    public SqlExecutionResponse execute(SqlScriptRequest request) throws Exception {

        if (!executionEnabled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exécution SQL désactivée");
        }

        SqlValidationResponse validation = validate(request);

        if (!validation.valid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.message());
        }

        if (!Boolean.TRUE.equals(request.confirmExecution())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmation d'exécution obligatoire");
        }

        ProjectGenerationContext context =
                contextStore.getContext();

        if (context.getDatabase() == null || context.getCredentials() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configurez d'abord la base de données");
        }

        try (Connection connection = context.getDatabase().getConnection(context.getCredentials())) {
            connection.setAutoCommit(false);
            try {
                SQLRunner.execute(connection, request.script());
            } catch (Exception exception) {
                if (!connection.isClosed()) {
                    try {
                        connection.rollback();
                    } catch (Exception rollbackError) {
                        exception.addSuppressed(rollbackError);
                    }
                }
                throw exception;
            }
        }

        return new SqlExecutionResponse(true, "Script SQL exécuté");
    }

    public ScriptConfigResponse saveScriptConfig(ScriptConfigRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Configuration SQL manquante");
        }

        String path = request.path() == null
                ? ""
                : request.path().trim();

        String content = request.content() == null
                ? ""
                : request.content();

        if (path.length() > 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chemin de script trop long");
        }

        if (content.length() > MAX_SCRIPT_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Script SQL trop volumineux");
        }

        if (!content.isBlank()) {
            SqlValidationResponse validation = validate(new SqlScriptRequest(content, false));

            if (!validation.valid()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.message());
            }
        }

        if (path.isBlank() && content.isBlank()) {
            savedScript.set(null);
            return new ScriptConfigResponse(true, "Configuration SQL effacée");
        }

        savedScript.set(new ScriptConfigRequest(path, content));

        return new ScriptConfigResponse(true, "Configuration du script SQL enregistrée");
    }
}
