package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceComponent {
    private String imports;
    private String methods;
    private String destinationPath;
    private String name;
}
