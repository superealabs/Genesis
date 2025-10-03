package org.labs.genesis.frontend.generator.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class FrontendLayout {
    public String navbar;
    public String primaryColor;
    public String secondaryColor;
    public String additionalCss;
    public List<InterfaceLang> langs;

    public void isValid() throws Exception {
//        if (langs == null || langs.size() == 0) {
//            throw new Exception("Please select at least one language");
//        }
        if (primaryColor == null || primaryColor.length() == 0) {
            throw new Exception("Please select a primary color");
        }
        if (secondaryColor == null || secondaryColor.length() == 0) {
            throw new Exception("Please select a secondary color");
        }
        if (navbar == null || navbar.length() == 0) {
            throw new Exception("Please select your prefered navbar");
        }
    }
}
