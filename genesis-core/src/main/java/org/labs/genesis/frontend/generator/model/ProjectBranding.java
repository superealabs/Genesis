package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class ProjectBranding {
    public File logoFile;
    public String logoLink;
    public File faviconFile;
    public String faviconLink;
}