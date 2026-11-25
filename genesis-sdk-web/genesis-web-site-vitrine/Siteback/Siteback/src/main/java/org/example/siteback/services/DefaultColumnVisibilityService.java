package org.example.siteback.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.siteback.services.interfaces.ColumnVisibilityService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DefaultColumnVisibilityService implements ColumnVisibilityService {

    @Value("${app.config.column-visibility.file}")
    private String configFilePath;

    private final ObjectMapper objectMapper;
    private final Map<String, Map<String, List<String>>> visibilityConfig = new ConcurrentHashMap<>();
    
    private long lastModifiedTime = 0L;

    public DefaultColumnVisibilityService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @PostConstruct
    public void loadConfig() {
        reloadIfChanged();
    }

    @Scheduled(fixedDelay = 2000) 
    public void checkForChanges() {
        reloadIfChanged();
    }

    private synchronized void reloadIfChanged() {
        File file = new File(configFilePath);
        if (!file.exists()) {
            return;
        }

        long currentLastModified = file.lastModified();
        
        if (currentLastModified > this.lastModifiedTime) {
            try {
                Map<String, Map<String, List<String>>> loadedConfig = objectMapper.readValue(
                    file, 
                    new TypeReference<Map<String, Map<String, List<String>>>>() {}
                );
                
                visibilityConfig.clear();
                visibilityConfig.putAll(loadedConfig);
                
                this.lastModifiedTime = currentLastModified;
                System.out.println("Configuration des colonnes rechargée (modification du fichier détectée)");
                
            } catch (IOException e) {
                System.err.println("Erreur lors du rechargement du fichier de configuration : " + e.getMessage());
    }
        }
    }

    @Override
    public List<String> getVisibleFields(String entityName, String componentType) {
        Map<String, List<String>> entityConfig = visibilityConfig.get(entityName.toLowerCase());
        if (entityConfig == null) {
            return Collections.emptyList();
        }
        return entityConfig.getOrDefault(componentType.toLowerCase(), Collections.emptyList());
    }

    @Override
    public synchronized void updateVisibleFields(String entityName, String componentType, List<String> fields) {
        visibilityConfig
            .computeIfAbsent(entityName.toLowerCase(), k -> new ConcurrentHashMap<>())
            .put(componentType.toLowerCase(), fields);
        saveConfig();
    }

    private synchronized void saveConfig() {
        try {
            File file = new File(configFilePath);
            file.getParentFile().mkdirs();
            objectMapper.writeValue(file, visibilityConfig);
            
            this.lastModifiedTime = file.lastModified();
            
            System.out.println("Configuration sauvegardée avec succès dans : " + configFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier de configuration", e);
        }
    }
}
