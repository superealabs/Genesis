package org.labs.genesis.forms.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de configuration pour les visualisations.
 * Stocke les paramètres configurés par l'utilisateur.
 */
public class VisualizationConfig {

    private final Map<String, Object> values = new HashMap<>();

    public void setValue(String key, Object value) {
        values.put(key, value);
    }

    public Object getValue(String key) {
        return values.get(key);
    }

    public String getString(String key) {
        Object value = values.get(key);
        return value != null ? value.toString() : null;
    }

    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    public boolean hasValue(String key) {
        return values.containsKey(key) && values.get(key) != null;
    }

    public boolean isNotEmpty(String key) {
        Object value = values.get(key);
        return value != null && !value.toString().trim().isEmpty();
    }

    public void clear() {
        values.clear();
    }

    public Map<String, Object> getAllValues() {
        return new HashMap<>(values);
    }
}