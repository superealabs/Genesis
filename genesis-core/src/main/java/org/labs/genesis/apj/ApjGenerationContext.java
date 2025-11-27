package org.labs.genesis.apj;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.filetype.ApjFile;

@Getter
@Setter
public class ApjGenerationContext {
    private String libDir;
    private String projectJarDir;
    private String locationDir;
    private ApjFile apjfile;
    private String[] tables;
    private String[] vues;
}
