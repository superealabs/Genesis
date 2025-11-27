package org.labs.genesis.apj.filetype;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ApjFile implements ApjMetadataProvider{
    private int id;
    private String name;
    private String imports;
    private String template;
    private String fileName;
    private String extension;

    @Override
    public String toString() {
        return name;
    }
}
