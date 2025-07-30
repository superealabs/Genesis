package org.labs.genesis.frontend;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class FrontendLanguage {
    private int id;
    private String name;
    private Map<String, String> types;

    @Override
    public String toString() {
        return this.name;
    }
}
