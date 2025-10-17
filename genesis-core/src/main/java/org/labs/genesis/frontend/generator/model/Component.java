package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.FilesEdit;

import java.util.List;

@Getter
@Setter
public class Component
{
    private String componentType;
    private String importFile;
    private String selector;
    private ComponentRoute router;
    private Boolean standalone;
    private String importComponent;
    private String template;
    private String style;
    private String export;
    private String destinationPath;
    private String componentName;
    private List<FilesEdit> componentAdditionalFiles;
    private String entityName;
    private Boolean generateForView;
}
