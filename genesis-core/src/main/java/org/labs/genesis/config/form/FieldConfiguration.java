package org.labs.genesis.config.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}