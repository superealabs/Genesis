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
    private String label;
    private Component component;
}
