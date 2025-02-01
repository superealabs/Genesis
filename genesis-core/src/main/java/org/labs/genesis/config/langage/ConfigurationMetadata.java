package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ConfigurationMetadata {
    private String name;
    private String variableName;
    private List<String> options;
    private String defaultOption;
}
