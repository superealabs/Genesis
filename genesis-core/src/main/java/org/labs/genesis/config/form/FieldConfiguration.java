package org.labs.genesis.config.form;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class FieldConfiguration {

    private String component;
    private Map<String, Configuration> configuration = new LinkedHashMap<>();

    public void addConfig(String confName, String value, String type) {
        if (value == null || value.isBlank()) {
            return;
        }

        Configuration config = new Configuration();
        config.setType(type);
        config.setValue(value);
        configuration.put(confName, config);
    }

    public void removeConf(String confName) {
        configuration.remove(confName);
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
}