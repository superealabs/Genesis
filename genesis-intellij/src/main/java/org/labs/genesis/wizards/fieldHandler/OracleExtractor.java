package org.labs.genesis.wizards.fieldHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleExtractor extends ExtractorSignature{
    @Override
    public DatabaseFields extractArgs(String jdbcUrl) {
        DatabaseFields fields = new DatabaseFields();
        Pattern pattern = Pattern.compile("jdbc:oracle:thin:@//([^:]+):([^/]+)/([^?]+)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            fields.setHost(matcher.group(1));
            fields.setPort(matcher.group(2));
            fields.setSid(matcher.group(3));
            fields.setDriverType("oracle");
            fields.setDriverName("oracle.jdbc.OracleDriver");
        }
        return fields;
    }
}
