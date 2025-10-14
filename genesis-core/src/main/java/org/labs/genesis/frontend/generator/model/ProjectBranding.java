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

    public boolean useLogoLink(){
        return logoLink != null && !logoLink.isEmpty();
    }

    public boolean useFaviconLink(){
        return faviconLink != null && !faviconLink.isEmpty();
    }

    public String getLogoUrl(){
        if (useLogoLink()){
            return  "/"+this.getLogoLink();
        }
        else if (hasLogo()){
            return  "/"+getFileFullName();
        }
        return  null;
    }

    public String getFileFullName(){
        return  this.getLogoFilename() + "." + this.getLogoExtension();
    }

    public String getFaviconUrl(){
        if (useFaviconLink()){
            return  this.getFaviconLink();
        }
        else if (hasFavicon()){
            return  this.getFaviconFilename();
        }
        return null;
    }

    public void setLogoFile(File logoFile) {
        this.logoFile = logoFile;
        if (logoFile == null){
            return;
        }

        String fileName = logoFile.getName();
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1); // e.g. "png", "svg"
            setLogoExtension(extension);
        }
    }
}