package org.labs.genesis.config.langage.generator.ruleToCode;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Properties;



@Getter
@Setter
public class LlmApiClientRule {
    private static final String CONFIG_FILE = "genesis-core.properties";
    private String apiUrl;
    private String apiKey;
    private String defaultModel;
    private Boolean useCustomApiKey;

    public LlmApiClientRule() {
        this.useCustomApiKey=false;
    }
    public void setApiKeyFromFile() {
        String apiKey;
        try (InputStream input = LlmApiClientRule.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("File contains llm configuration not found.");
            }
            Properties prop = new Properties();
            prop.load(input);
            apiKey = prop.getProperty("api.key").trim();
        } catch (IOException ex) {
            throw new RuntimeException("Error in loading file configuration.", ex);
        }
        this.apiKey = apiKey;
    }
    private HttpRequest buildHttpRequest(String jsonPayload) throws Exception {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
    }

    public String generateFunction(String yaml , String prompt , String under_prompt , String aiModel) throws Exception {
            String jsonPayload = buildRequestPayload( yaml , prompt , under_prompt , aiModel);
            HttpRequest request = buildHttpRequest(jsonPayload);
            HttpResponse<String> response = sendHttpRequest(request);
            return parseResponse(response);
    }

    public String buildRequestPayload(String yaml , String prompt , String under_prompt , String aiModel) throws Exception {

        HashMap<String, Object> payload = new HashMap<>();
        HashMap<String, String> message = new HashMap<>();
        message.put("role", "user");

        message.put("content", String.format( """
            Description : 
            %s   
            %s

            This is YAML content : 
            %s
            """ , prompt , under_prompt , yaml ));

        payload.put("messages", new HashMap[]{message});
        payload.put("model", aiModel);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(payload);
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
            throw new Exception("Invalid response format: choices array is empty or malformed");
        } else {
            throw new Exception("API call failed with status code: " + response.statusCode() + "\nError message : " + response.body());
        }
    }
}
