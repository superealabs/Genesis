package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class FrontendLayout {
    public String navbar;
    public String primaryColor;
    public String secondaryColor;
    public String additionalCss;
    public List<InterfaceLang> langs;

    public  FrontendLayout(){
        this.navbar = "Sidebar";
        this.primaryColor = "#537cc2";
        this.secondaryColor = new Color(0x53,0x7c,0xc2,0x26).toString();
        this.additionalCss = "";
        this.langs = new ArrayList<>();
        langs.add(ProjectGenerator.langs.get(1));
    }

    public void isValid() throws Exception {
        if (langs == null || langs.size() == 0) {
            throw new Exception("Please select at least one language");
        }
        if (primaryColor == null || primaryColor.length() == 0) {
            throw new Exception("Please select a primary color");
        }
        if (secondaryColor == null || secondaryColor.length() == 0) {
            throw new Exception("Please select a secondary color");
        }
        if (navbar == null || navbar.length() == 0) {
            throw new Exception("Please select your default navbar preference");
        }
    }
}
