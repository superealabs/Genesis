package org.labs.genesis.api.service;

import java.util.Comparator;
import java.util.List;

import org.labs.genesis.api.dto.ScriptAiDtos.GenerateSqlRequest;
import org.labs.genesis.api.dto.ScriptAiDtos.GenerateSqlResponse;
import org.labs.genesis.api.dto.ScriptAiDtos.LlmModelResponse;
import org.labs.genesis.api.exception.DatabaseConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.LlmApiClient;
import org.labs.genesis.config.langage.generator.project.LlmApiConfig;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.SQLRunner;
import org.labs.genesis.connexion.ScriptDatabaseChecker;
// import org.labs.genesis.connexion.ScriptDatabaseChecker;
import org.springframework.stereotype.Service;

/**
 * Génère un script SQL avec l'IA, comme generateSQLFromLLM() de ScriptDatabaseConfigurationForm :
 * même client (LlmApiClient), mêmes modèles (llm-api-config.json), même clé (genesis-core.properties).
 */
@Service
public class ScriptAiService {

    private static final String FAILED_PREFIX = "-- Failed to generate SQL script";

    public List<LlmModelResponse> models() {
        return ProjectGenerator.llmApiConfigs.values().stream()
                .sorted(Comparator.comparingInt(LlmApiConfig::getId))
                .map(c -> new LlmModelResponse(c.getId(), c.getName(), c.getModel()))
                .toList();
    }

    public GenerateSqlResponse generate(GenerateSqlRequest request) {
        if (request == null || request.prompt() == null || request.prompt().isBlank()) {
            throw new DatabaseConfigException("Décrivez ce que l'IA doit générer.");
        }
        Database database = request.databaseId() == null ? null : ProjectGenerator.databases.get(request.databaseId());
        if (database == null) {
            throw new DatabaseConfigException("Choisissez d'abord une base de données.");
        }
        LlmApiConfig config = request.llmModelId() != null
                ? ProjectGenerator.llmApiConfigs.get(request.llmModelId())
                : ProjectGenerator.llmApiConfigs.values().stream().min(Comparator.comparingInt(LlmApiConfig::getId)).orElse(null);
        if (config == null) {
            throw new DatabaseConfigException("Modèle IA introuvable : " + request.llmModelId());
        }

        LlmApiClient client = new LlmApiClient();
        client.setDefaultModel(config.getModel());
        client.setApiUrl(config.getApiUrl());
        if (request.apiKey() != null && !request.apiKey().isBlank()) {
            client.setUseCustomApiKey(true);
            client.setApiKey(request.apiKey().trim());
        } else {
            try {
                client.setApiKeyFromFile();
            } catch (RuntimeException e) {
                throw new DatabaseConfigException("Aucune clé API : renseignez api.key dans genesis-core.properties "
                        + "ou utilisez un token personnel.");
            }
        }

        // Contexte minimal : LlmApiClient lit seulement la base (nom + types autorisés).
        ProjectGenerationContext context = new ProjectGenerationContext();
        context.setDatabase(database);

        String description = request.prompt().trim();
        if (Boolean.TRUE.equals(request.includeCurrentScript()) && request.currentScript() != null && !request.currentScript().isBlank()) {
            description += "\n\nExisting SQL script to keep and extend (return the COMPLETE updated script):\n"
                    + request.currentScript().trim();
        }

        String result = client.generateSQL(context, description, false);
        if (result == null || result.isBlank()) {
            throw new DatabaseConfigException("L'IA n'a renvoyé aucun script.");
        }
        if (result.startsWith(FAILED_PREFIX)) {
            throw new DatabaseConfigException("Échec de l'appel IA (" + config.getName() + ") : "
                    + result.substring(result.indexOf("Error:") + 6).trim());
        }

        String script = clean(result);
        List<String> tables = SQLRunner.extractTableNames(script);
        String warning = ScriptDatabaseChecker.mismatchMessage(database, script, ProjectGenerator.databases.values());
        return new GenerateSqlResponse(true, database.getName(), config.getName(), script, tables, warning);
    }

    static String clean(String raw) {
        String s = raw.trim();
        int fence = s.indexOf("```");
        if (fence >= 0) {
            int start = s.indexOf('\n', fence);
            int end = s.indexOf("```", start < 0 ? fence + 3 : start);
            if (start >= 0) {
                s = (end > start ? s.substring(start + 1, end) : s.substring(start + 1)).trim();
            }
        }
        return s;
    }
}