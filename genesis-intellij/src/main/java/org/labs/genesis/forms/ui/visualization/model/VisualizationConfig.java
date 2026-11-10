package org.labs.genesis.forms.ui.visualization.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    public VisualizationConfig snapshot() {
        VisualizationConfig snapshot = new VisualizationConfig();
        values.forEach((key, value) -> snapshot.setValue(key, copyValue(value)));
        return snapshot;
    }

    private static Object copyValue(Object value) {
        if (value instanceof VisualizationFilterCondition condition) {
            VisualizationFilterCondition copy = new VisualizationFilterCondition(
                    condition.getColumn(), condition.getOperator(), condition.getValue());
            copy.setRelationToPrevious(condition.getRelationToPrevious());
            return copy;
        }
        if (value instanceof VisualizationFilterGroup group) {
            VisualizationFilterGroup copy = new VisualizationFilterGroup();
            copy.setRelation(group.getRelation());
            for (VisualizationFilterNode child : group.getChildren()) {
                copy.addChild((VisualizationFilterNode) copyValue(child));
            }
            return copy;
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>();
            list.forEach(item -> copy.add(copyValue(item)));
            return copy;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new HashMap<>();
            map.forEach((key, item) -> copy.put(key, copyValue(item)));
            return copy;
        }
        return value;
    }

    public Map<String, Object> getAllValues() {
        return new HashMap<>(values);
    }
}