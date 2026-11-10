package org.labs.genesis.config.form;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class FormConfiguration {

    private Map<String, Type> types;
    private Map<String, Configuration> components;

    @Getter
    @NoArgsConstructor
    public static class Type {

        private List<String> components;
    }

    @Getter
    @NoArgsConstructor
    public static class Configuration {

        private ComponentConfiguration configuration;
    }

    @Getter
    @NoArgsConstructor
    public static class ComponentConfiguration {

        private List<Field> fields;
    }

    @Getter
    @NoArgsConstructor
    public static class Field {

        private String name;
        private String type;
        private List<String> options;
    }
}