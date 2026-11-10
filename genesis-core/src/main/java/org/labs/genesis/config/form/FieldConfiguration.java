package org.labs.genesis.config.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FieldConfiguration {

    private String component;

    private Map<String, Configuration> configuration =
            new LinkedHashMap<>();

    public void addConfig(
            String confName,
            String value,
            String type
    ) {
        if (confName == null
                || confName.isBlank()
                || value == null
                || value.isBlank()) {
            return;
        }

        if (configuration == null) {
            configuration = new LinkedHashMap<>();
        }

        Configuration config =
                new Configuration();

        config.setType(type);
        config.setValue(value);

        configuration.put(
                confName,
                config
        );
    }

    public void removeConf(
            String confName
    ) {
        if (configuration == null
                || confName == null) {
            return;
        }

        configuration.remove(confName);
    }

    public void clearConfiguration() {
        if (configuration != null) {
            configuration.clear();
        }
    }

    @Override
    public String toString() {
        return "FieldConfiguration{" +
                "component='" + component + '\'' +
                ", configuration=" + configuration +
                '}';
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Configuration {

        private String value;
        private String type;

        @Override
        public String toString() {
            return "Configuration{" +
                    "value='" + value + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    public String getInputConfiguration() {
        if (configuration == null
                || configuration.isEmpty()) {
            return "";
        }

        String[] keys = {
                "min",
                "max",
                "step"
        };

        StringBuilder sb =
                new StringBuilder();

        for (String key : keys) {

            Configuration conf =
                    configuration.get(key);

            if (conf != null
                    && conf.getValue() != null
                    && !conf.getValue().isBlank()) {

                if (sb.length() > 0) {
                    sb.append(' ');
                }

                sb.append(key)
                        .append("=\"")
                        .append(conf.getValue())
                        .append('"');
            }
        }

        return sb.toString();
    }

    public String getFileConfiguration() {
        if (configuration == null
                || configuration.isEmpty()) {
            return "";
        }

        Configuration conf =
                configuration.get("accept");

        if (conf == null
                || conf.getValue() == null
                || conf.getValue().isBlank()) {
            return "";
        }

        return "accept=\""
                + conf.getValue()
                + "\"";
    }

    public String getCurrency() {
        if (configuration == null
                || configuration.isEmpty()) {
            return "";
        }

        Configuration conf =
                configuration.get("currency");

        if (conf == null
                || conf.getValue() == null
                || conf.getValue().isBlank()) {
            return "";
        }

        return "currency=\""
                + conf.getValue()
                + "\"";
    }

    public String getLatitude() {
        if (configuration == null
                || configuration.isEmpty()) {
            return null;
        }

        Configuration conf =
                configuration.get("latitude");

        if (conf == null
                || conf.getValue() == null
                || conf.getValue().isBlank()) {
            return null;
        }

        return conf.getValue();
    }

    public String getLongitude() {
        if (configuration == null
                || configuration.isEmpty()) {
            return null;
        }

        Configuration conf =
                configuration.get("longitude");

        if (conf == null
                || conf.getValue() == null
                || conf.getValue().isBlank()) {
            return null;
        }

        return conf.getValue();
    }

    public static FieldConfiguration findConfiguration(
            Map<String, Map<String, FieldConfiguration>> formConfiguration,
            String tableName,
            ColumnMetadata field
    ) {
        if (formConfiguration == null || tableName == null || field == null) {
            return null;
        }

        Map<String, FieldConfiguration> tableConfig = formConfiguration.get(tableName);
        if (tableConfig == null) {
            for (Map.Entry<String, Map<String, FieldConfiguration>> entry : formConfiguration.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(tableName)) {
                    tableConfig = entry.getValue();
                    break;
                }
            }
        }
        if (tableConfig == null) {
            return null;
        }

        // 1. Priorité conventionnelle : referencedColumn (nom physique SQL)
        String refCol = field.getReferencedColumn();
        if (refCol != null && !refCol.isBlank()) {
            FieldConfiguration config = tableConfig.get(refCol);
            if (config != null) return config;
        }

        // 2. Fallback : name (nom d'attribut modèle / camelCase)
        String name = field.getName();
        if (name != null && !name.isBlank()) {
            FieldConfiguration config = tableConfig.get(name);
            if (config != null) return config;
        }

        // 3. Fallback insensible à la casse
        for (Map.Entry<String, FieldConfiguration> entry : tableConfig.entrySet()) {
            if ((refCol != null && entry.getKey().equalsIgnoreCase(refCol))
                    || (name != null && entry.getKey().equalsIgnoreCase(name))) {
                return entry.getValue();
            }
        }

        return null;
    }
}
