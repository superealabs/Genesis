package org.labs.genesis.config.langage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FrameworkCaching {
    private int id;
    private String name;
    private int frameworkId;
    private List<FilesEdit> configFiles;
    private List<Framework.Dependency> additionalDependencies;
    private List<String> metadataBooleanTrueKeys;
}
