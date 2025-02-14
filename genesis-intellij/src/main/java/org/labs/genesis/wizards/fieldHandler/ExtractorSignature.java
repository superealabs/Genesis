package org.labs.genesis.wizards.fieldHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ExtractorSignature {
    public abstract DatabaseFields extractArgs(String jdbcUrl);
    public String extractParameter(String params, String key) {
        Pattern pattern = Pattern.compile(key + "=([^&]+)");
        Matcher matcher = pattern.matcher(params);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
