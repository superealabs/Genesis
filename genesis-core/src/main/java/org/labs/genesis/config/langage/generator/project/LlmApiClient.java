package org.labs.genesis.config.langage.generator.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;
import org.labs.genesis.apj.component.ApjField;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Getter
@Setter
public class LlmApiClient {

    private static final String CONFIG_FILE = "genesis-core.properties";
    private static final String WORKER_FILE = "worker.properties";
    private String apiUrl;
    private String apiKey;
    private String defaultModel;
    private Boolean useCustomApiKey;
    private String workerUrl;
    private String workerSecret;

    public LlmApiClient() {
        this.useCustomApiKey=false;
    }

    public void setApiKeyFromFile() {
        String apiKey;
        try (InputStream input = LlmApiClient.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Le fichier de configuration n'a pas été trouvé.");
            }

            Properties prop = new Properties();
            prop.load(input);

            apiKey = prop.getProperty("api.key").trim();

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors du chargement du fichier de configuration.", ex);
        }
        this.apiKey = apiKey;
    }

    public String generateSQL(ProjectGenerationContext projectGenerationContext, String description, boolean addDatabase) {
        try {
            String jsonPayload = buildRequestPayload(projectGenerationContext, description, addDatabase);
            HttpRequest request = buildHttpRequest(jsonPayload);
            HttpResponse<String> response = sendHttpRequest(request);
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("ERROR when generating the SQL script :\n" + e.getMessage());
            return "-- Failed to generate SQL script. Error: " + e.getMessage();
        }
    }

    private String buildRequestPayload(ProjectGenerationContext projectGenerationContext, String description, boolean addDatabase) throws Exception {
        Database database = projectGenerationContext.getDatabase();
        String databaseSchema = "";

        if(addDatabase){
            databaseSchema = getDatabaseSchemaToString(projectGenerationContext);
        }

        HashMap<String, Object> payload = new HashMap<>();
        HashMap<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", String.format("""
                **Role**:
                You are a senior SQL developer with 15+ years of experience, specializing in optimized database schema design and mission-critical systems.
        
                **Task**:
                Generate a complete SQL script implementing the following client description:
                %s
                
                **Context**:
                - Database system: %s
                - Available data types: %s
                - Production environment requiring maximum robustness
                - All database objects must be uniquely identifiable
                %s
        
                **Constraints**:
                1. Output format: PLAIN TEXT ONLY containing EXCLUSIVELY executable SQL statements
                2. ABSOLUTELY NO:
                   - Markdown formatting
                   - Code blocks
                   - Comments
                   - Explanations
                   - Introductory/concluding text
                   - Non-SQL characters
                3. Script MUST contain:
                   - UNIQUE primary key for every table
                   - "IF NOT EXISTS" clause for all CREATE statements
                   - Professionally formatted SQL (proper indentation, capitalization)
                4. Strictly validate all integrity constraints
                5. Use ONLY the specified database types and features
                6. Ensure script is immediately executable in target DBMS
                7. Maintain full compatibility with existing schema
                8. Preserve all existing data relationships
                9. CRITICAL: ANY non-SQL output will cause system failure 
                (like Here is the SQL script implementing the client description,...)
                10. I repeat : ANY non-SQL output will cause system failure 
                11. I repeat again : ANY non-SQL output will cause system failure 
                
                """, description, database.getName(), database.getTypes().entrySet(), databaseSchema));

        payload.put("messages", new HashMap[]{message});
        payload.put("model", defaultModel);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(payload);
    }

    private HttpRequest buildHttpRequest(String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    private static HttpResponse<String> sendHttpRequest(HttpRequest request) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static String parseResponse(HttpResponse<String> response) throws Exception {
        if (response.statusCode() == 200) {
            ObjectMapper mapper = new ObjectMapper();
            String responseBody = response.body();

            HashMap<?, ?> jsonResponse = mapper.readValue(responseBody, HashMap.class);
            Object choicesObject = jsonResponse.get("choices");

            if (choicesObject instanceof java.util.ArrayList<?> choices) {

                if (!choices.isEmpty()) {
                    HashMap<?, ?> firstChoice = (HashMap<?, ?>) choices.getFirst();
                    HashMap<?, ?> message = (HashMap<?, ?>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }
            throw new RuntimeException("Invalid response format: choices array is empty or malformed");
        } else {
            throw new RuntimeException("API call failed with status code: " + response.statusCode() + "\nError message : " + response.body());
        }
    }

    private String getDatabaseSchemaToString(ProjectGenerationContext projectGenerationContext) throws SQLException, ClassNotFoundException {
        StringBuilder schemaBuilder = new StringBuilder();
        Database database = projectGenerationContext.getDatabase();
        Credentials credentials = projectGenerationContext.getCredentials();
        Connection connex = projectGenerationContext.getConnection();
        Language language = projectGenerationContext.getLanguage();
        Framework framework = projectGenerationContext.getFramework();
        List<TableMetadata> entities = database.getEntitiesByNames(new ArrayList<>(), connex, credentials, language, framework);
        List<TableMetadata> views = database.getViewsByNames(new ArrayList<>(), connex, credentials, language, framework);

        List<TableMetadata> allEntities = new ArrayList<>();
        allEntities.addAll(entities);
        allEntities.addAll(views);

        // Build schema description
        schemaBuilder.append("\n- Existing schema structure:");
        for (TableMetadata table : allEntities) {
            schemaBuilder.append(String.format("\n  %s: %s",
                    table.getIsView() ? "VIEW" : "TABLE",
                    table.getTableName()));

            for (ColumnMetadata column : table.getColumns()) {
                schemaBuilder.append(String.format("\n    ├─ %s: %s%s%s",
                        column.getReferencedColumn(),
                        column.getColumnType(),
                        column.isPrimary() ? " (PK)" : "",
                        column.isForeign() ? String.format(" → %s(%s)",
                                column.getReferencedTable(),
                                column.getReferencedColumn()) : ""));
            }
        }
        return schemaBuilder.toString();
    }

    public void setWorkerConfig(){
        try (InputStream input = LlmApiClient.class.getClassLoader().getResourceAsStream(WORKER_FILE)) {
            if (input == null) {
                throw new RuntimeException("Le fichier de configuration n'a pas été trouvé.");
            }
            Properties prop = new Properties();
            prop.load(input);
            this.setWorkerUrl(prop.getProperty("worker.url").trim());
            this.setWorkerSecret(prop.getProperty("worker.secret").trim());
        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors du chargement du fichier de configuration.", ex);
        }
    }

    public void setConfigFile() {
        this.setWorkerConfig();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(this.getWorkerUrl()))
                    .header("x-api-key", this.getWorkerSecret())
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur lors de l'appel au Worker : " + response.statusCode() + " -> " + response.body());
            }

            ObjectMapper mapper = new ObjectMapper();
            var node = mapper.readTree(response.body());

            this.apiUrl = node.path("apiUrl").asText();
            this.apiKey = node.path("apiKey").asText();
            this.defaultModel = node.path("apiModel").asText();

        } catch (Exception e) {
            throw new RuntimeException("Impossible de récupérer les credentials via le Worker", e);
        }
    }

    public String[] askForLabel(String mapping, ApjField[] apjFields) throws Exception {
        setConfigFile();
        String apiToken = this.getApiKey();
        String model = this.getDefaultModel();
        HttpClient client = HttpClient.newHttpClient();
        String[] fields = new String[apjFields.length];
        for (int i = 0; i < apjFields.length; i++) {
            fields[i] = apjFields[i].toString();
        }

        String prompt = """
        Pour la classe Java "%s", génère uniquement des libellés lisibles en français pour ces attributs : %s.
        
        - Chaque libellé doit correspondre à un attribut.
        - Transforme les noms camelCase en mots séparés.
        - Mets la première lettre de chaque libellé en majuscule.
        - Adapte automatiquement les mots pour qu'ils soient compréhensibles par un utilisateur final en français.
        - Ne crée pas de synonymes complexes ou inventés.
        - Conserve l'ordre des attributs.
        - Le libellé doit correspondre au sens du mot:
        - Voici plusieurs exemple :
            (String)idOrigine -> Id Origine,
            (String)idClient -> Id Client,
            (String)idPompisteLib -> Pompiste
            (Date)dateNaissance -> Date de naissance,
            (double)pv -> Prix de vente,
            (double)pvAncien -> Ancien prix de vente,
            (String)etatLib -> État,
            (String)totalEncaissement -> Total des encaissements,
            (double)depense -> Dépense,
        - **Répond uniquement par un tableau JSON de chaînes**, rien d'autre. Exemple de format attendu :
        
        ["Prix de vente", "Unité", "Ancien prix de vente", "Date de demande", "Date"]
        Aucune explication, aucun objet, juste le tableau JSON.
        """.formatted(mapping, String.join(", ", fields));

        String jsonPrompt = JSONObject.quote(prompt);

        String requestBody = """
        {
            "messages": [
                {"role": "user", "content": %s}
            ],
            "model": "%s"
        }
        """.formatted(jsonPrompt, model);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(this.getApiUrl()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        System.out.println("Réponse brute de l'API : " + json);

        ObjectMapper mapper = new ObjectMapper();
        var rootNode = mapper.readTree(json);
        String content = rootNode
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();

        content = content.replaceAll("(?s)```json", "").replaceAll("```", "").trim();
        String[] libelles = mapper.readValue(content, String[].class);
        if (libelles.length != fields.length) {
            String[] corrected = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                if (i < libelles.length) {
                    corrected[i] = libelles[i];
                } else {
                    corrected[i] = Character.toUpperCase(fields[i].charAt(0)) + fields[i].substring(1);
                }
            }
            libelles = corrected;
        }

        return libelles;
    }


}
