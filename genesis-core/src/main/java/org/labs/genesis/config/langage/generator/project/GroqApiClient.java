package org.labs.genesis.config.langage.generator.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.labs.genesis.connexion.Database;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Properties;

public class GroqApiClient {

    private static final String CONFIG_FILE = "genesis-core.properties";
    private static final String API_URL;
    private static final String API_KEY;
    private static final String DEFAULT_MODEL;

    static {
        String apiUrl;
        String apiKey;
        String defaultModel;

        try (InputStream input = GroqApiClient.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Le fichier de configuration n'a pas été trouvé.");
            }

            Properties prop = new Properties();
            prop.load(input);

            apiUrl = prop.getProperty("api.url");
            apiKey = prop.getProperty("api.key");
            defaultModel = prop.getProperty("api.model");

        } catch (IOException ex) {
            throw new RuntimeException("Erreur lors du chargement du fichier de configuration.", ex);
        }

        API_URL = apiUrl;
        API_KEY = apiKey;
        DEFAULT_MODEL = defaultModel;
    }


    public static String generateSQL(Database database, String description) {
        try {
            String jsonPayload = buildRequestPayload(database, description);
            HttpRequest request = buildHttpRequest(jsonPayload);
            HttpResponse<String> response = sendHttpRequest(request);
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("ERROR when generating the SQL script :\n" + e.getMessage());
            return "-- Failed to generate SQL script. Error: " + e.getMessage();
        }
    }


    private static String buildRequestPayload(Database database, String description) throws Exception {
        HashMap<String, Object> payload = new HashMap<>();
        HashMap<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", String.format("""
                Instructions:
                - Provide the output in plain text format (not markdown).
                - Do not include any comments or explanations in the output.
                - We only need the SQL and it must be well formatted.
                - All tables must have an unique primary key.
                - Use the : "if not exists" when creating database objects.
                
                Database Details:
                - The database being used is: %s
                - You can only use these database types: %s
                
                Task Description:
                """, database.getName(), database.getTypes().entrySet()) + description);

        payload.put("messages", new HashMap[]{message});
        payload.put("model", DEFAULT_MODEL);

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(payload);
    }

    private static HttpRequest buildHttpRequest(String jsonPayload) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
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


}
