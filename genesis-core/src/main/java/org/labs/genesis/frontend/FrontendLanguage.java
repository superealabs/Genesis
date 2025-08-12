package org.labs.genesis.frontend;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FrontendLanguage {
    private int id;
    private String name;
    private String extension;
    private Map<String, String> types;
    private Map<String, String> inputTypes;

    @Override
    public String toString() {
        return this.name;
    }

    public static FrontendLanguage findById(FrontendLanguage[] languages, int id) {
        for (FrontendLanguage language : languages) {
            if (language.getId() == id) {
                return language;
            }
        }
        return null;
    }
}
