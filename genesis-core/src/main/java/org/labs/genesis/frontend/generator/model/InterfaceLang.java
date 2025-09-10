package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterfaceLang {
    public int id;
    public String name;
    public String locale;
    public String filename;
    public String extension;
    public String content;

    @Override
    public String toString() {
        return this.name+" ("+this.locale+")";
    }
}
