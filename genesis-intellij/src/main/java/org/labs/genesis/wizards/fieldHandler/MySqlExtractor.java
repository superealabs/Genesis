package org.labs.genesis.wizards.fieldHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySqlExtractor extends ExtractorSignature {
    @Override
    public DatabaseFields extractArgs(String jdbcUrl) {
        DatabaseFields fields = new DatabaseFields();
        Pattern pattern = Pattern.compile("jdbc:mysql://([^:]+):([^/]+)/(?:([^?]+))?\\?(.*)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            fields.setHost(matcher.group(1));
            fields.setPort(matcher.group(2));
            fields.setDatabaseName(matcher.group(3));
            fields.setDriverName("com.mysql.cj.jdbc.Driver");
            fields.setDriverType("mysql");
            String params = matcher.group(4);
            fields.setUser(extractParameter(params, "user"));
            fields.setPassword(extractParameter(params, "password"));
//            fields.setUseSSL(extractBooleanParameter(params, "useSSL"));
//            fields.setAllowPublicKeyRetrieval(extractBooleanParameter(params, "allowPublicKeyRetrieval"));
        }
        return fields;
    }
}
