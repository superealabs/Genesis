package org.labs.genesis.config;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.affichage.gen.PageRechercheGen;

@Getter
@Setter
public class ApjGenerationContext {
    private String libDir;
    private String projectJarDir;
    private String locationDir;
    private String apjType;

    private PageRechercheGen pr;
}
