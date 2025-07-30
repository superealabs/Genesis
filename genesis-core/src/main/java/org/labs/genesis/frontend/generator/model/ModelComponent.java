package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelComponent
{
    private String exports;
    private String destinationPath;
    private String name;
}
