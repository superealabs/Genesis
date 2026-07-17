package org.labs.genesis.config.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NativeDatabaseTypeRegistry {
    private static final String CONFIG_FILE = "/data_genesis/json/native-database-types.json";
    private static final Map<String, List<String>> NATIVE_TYPES = loadNativeTypes();
    private NativeDatabaseTypeRegistry() {}
    public static boolean isNative(int databaseId,String columnType) {
        if (columnType == null) {
            return false;
        }
        List<String> configuredTypes = NATIVE_TYPES.getOrDefault(String.valueOf(databaseId), Collections.emptyList());
        String normalizedColumnType = columnType.trim().toLowerCase(Locale.ROOT);
        return configuredTypes.stream()
                .anyMatch(typePattern ->
                        matches(typePattern, normalizedColumnType)
                );
    }

    private static boolean matches(String typePattern,String columnType) {
        String normalizedPattern = typePattern.trim().toLowerCase(Locale.ROOT);
        if (normalizedPattern.endsWith("*")) {
            String prefix = normalizedPattern.substring(0,normalizedPattern.length() - 1);
            return columnType.startsWith(prefix);
        }
        return columnType.equals(normalizedPattern);
    }

    private static Map<String, List<String>> loadNativeTypes() {
        try (
            InputStream inputStream = NativeDatabaseTypeRegistry.class.getResourceAsStream(CONFIG_FILE)
        ) {
            if (inputStream == null) {
                throw new IllegalStateException("Configuration file not found: "+ CONFIG_FILE);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream,new TypeReference<Map<String, List<String>>>() {});
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load native database types",exception);
        }
    }
}