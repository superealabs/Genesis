package org.labs.genesis.wizards.fieldHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlServerExtractor extends ExtractorSignature {
    @Override
    public DatabaseFields extractArgs(String jdbcUrl) {
        DatabaseFields fields = new DatabaseFields();
        Pattern pattern = Pattern.compile("jdbc:sqlserver://([^:]+):([^;]+);(.*)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            fields.setHost(matcher.group(1));
            fields.setPort(matcher.group(2));
            fields.setDriverName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            fields.setDriverType("sqlserver");
            String params = matcher.group(3);
            fields.setDatabaseName(this.extractParameter(params, "databaseName"));
            fields.setUser(this.extractParameter(params, "user"));
            fields.setPassword(this.extractParameter(params, "password"));
//            fields.setEncrypt(extractBooleanParameter(params, "encrypt"));
//            fields.setTrustServerCertificate(extractBooleanParameter(params, "trustServerCertificate"));
        }
        return fields;
    }
    @Override
    public String extractParameter(String params, String key) {
        Pattern pattern = Pattern.compile(key + "=([^;]+)");
        Matcher matcher = pattern.matcher(params);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
