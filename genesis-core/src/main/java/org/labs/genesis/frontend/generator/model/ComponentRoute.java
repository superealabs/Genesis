package org.labs.genesis.frontend.generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComponentRoute {
    private String componentName;
    private String componentSelector;
    private String link;
    private String componentImport;
    private String componentImportWithoutExtension;
    private String label;

    public ComponentRoute(ComponentRoute source){
        this.componentName = source.getComponentName();
        this.componentSelector = source.getComponentSelector();
        this.link = source.getLink();
        this.componentImport = source.getComponentImport();
        this.componentImportWithoutExtension = source.getComponentImportWithoutExtension();
        this.label = source.getLabel();
    }
}
