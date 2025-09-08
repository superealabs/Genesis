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
    public  String faviconFilename = "favicon.ico";
    public String logoFilename = "logo";
    public String logoExtension = "png";

    public boolean hasFavicon() {
        return  faviconFile != null || (useFaviconLink());
    }

    public boolean hasLogo(){
        return logoFile != null || (useLogoLink());
    }

    public String getLogoType(){
        if (logoExtension.equals("svg")){
            return "vector";
        }
        return "image";
    }

    public boolean useLogoLink(){
        return logoLink != null && !logoLink.isEmpty();
    }

    public boolean useFaviconLink(){
        return faviconLink != null && !faviconLink.isEmpty();
    }
}