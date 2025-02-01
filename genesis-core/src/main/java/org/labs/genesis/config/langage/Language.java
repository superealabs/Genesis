package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Language {
    private int id;
    private String name;
    private String extension;
    private List<ConfigurationMetadata> configurations;
    private Map<String, String> syntax;
    private Map<String, String> types;

    @Override
    public String toString() {
        return this.name;
    }
}
