package org.labs.genesis.config.langage.generator.project;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LlmApiConfig {
    private int id;
    private String name;
    private String model;
    private String apiUrl;

    @Override
    public String toString() {
        return this.name;
    }
}
