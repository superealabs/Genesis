package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FrontendDestinationPaths {
    public String faviconPath;
    public String logoPath;
    public String langsPath;

    public String getFaviconPath() {
        return normalizePath(faviconPath);
    }

    public void setFaviconPath(String faviconPath) {
        this.faviconPath = faviconPath;
    }

    public String getLogoPath() {
        return normalizePath(logoPath);
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getLangsPath() {
        return normalizePath(langsPath);
    }

    public void setLangsPath(String langsPath) {
        this.langsPath = langsPath;
    }

    public static String normalizePath(String path) {
        if (path == null) return null;
        return path.replace("\\", "/");
    }
}

