package org.labs.genesis.connexion.providers;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLServerDatabase extends Database {
    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port = (credentials.getPort() != null) ? credentials.getPort() : getPort();
        return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=%s;trustServerCertificate=%s;",
                credentials.getHost(),
                port,
                credentials.getDatabaseName(),
                credentials.getUser(),
                credentials.getPwd(),
                credentials.isUseSSL(),
                credentials.isTrustCertificate());
    }

    @Override
    protected void checkStrictMinConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkNumericConstraint(conn, tableName, columns, framework, "numericMinimumValue", "numericMinimumValueData", this.getConstraintQueries().getCheckStrictMinimumConstraintQuery(), true);
    }

    @Override
    protected void checkMinConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkNumericConstraint(conn, tableName, columns, framework, "numericMinimumInclusiveValue", "numericMinimumInclusiveValueData", this.getConstraintQueries().getCheckMinimumConstraintQuery(), true);
    }

    @Override
    protected void checkStrictMaxConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkNumericConstraint(conn, tableName, columns, framework, "numericMaximumValue", "numericMaximumValueData", this.getConstraintQueries().getCheckStrictMaximumConstraintQuery(), false);
    }

    @Override
    protected void checkMaxConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkNumericConstraint(conn, tableName, columns, framework, "numericMaximumInclusiveValue", "numericMaximumInclusiveValueData", this.getConstraintQueries().getCheckMaximumConstraintQuery(), false);
    }

    private void checkNumericConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework, String annotationKey, String dataKey, String sql, boolean isMin) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, "dbo");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String value = null;
                    if (isMin) {
                        value = rs.getString("min_value") ;
                    }
                    else {
                        value = rs.getString("max_value");
                    }

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isNumeric()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldMap.put("value", value);
                            col.getValidationAnnotations().put(dataKey, value);
                            String template = (String) annotations.getOrDefault(annotationKey, "{{removeLine}}");
                            String result = engine.render(template, fieldMap);
                            col.getValidationAnnotations().put(annotationKey, result);
                            col.checkAndCreateRangeAnnotation(annotations, fieldMap, engine, value, isMin);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkPastDateConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkDateConstraint(conn, tableName, columns, framework, "pastOrPresent", this.getConstraintQueries().getCheckPastDateConstraintQuery());
    }

    @Override
    protected void checkStrictPastDateConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkDateConstraint(conn, tableName, columns, framework, "past", this.getConstraintQueries().getCheckStrictPastDateConstraintQuery());
    }

    @Override
    protected void checkFutureDateConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkDateConstraint(conn, tableName, columns, framework, "futureOrPresent", this.getConstraintQueries().getCheckFutureDateConstraintQuery());
    }

    @Override
    protected void checkStrictFutureDateConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        checkDateConstraint(conn, tableName, columns, framework, "future", this.getConstraintQueries().getCheckStrictFutureDateConstraintQuery());
    }

    private void checkDateConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework, String key, String sql) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, "dbo");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String template = (String) annotations.getOrDefault(key, "{{removeLine}}");
                            String result = engine.render(template, fieldMap);
                            col.getValidationAnnotations().put(key, result);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkNotBlankConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(this.getConstraintQueries().getCheckNotBlankConstraintQuery())) {
            stmt.setString(1, tableName);
            stmt.setString(2, "dbo");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String template = (String) annotations.getOrDefault("notBlank", "{{removeLine}}");
                            String result = engine.render(template, fieldMap);
                            col.getValidationAnnotations().put("notBlank", result);
                            col.checkAndCreateNotNullNotBlankCombinedAnnotation(annotations, fieldMap, engine);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMinLengthConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(this.getConstraintQueries().getCheckMinimumLengthConstraintQuery())) {
            stmt.setString(1, tableName);
            stmt.setString(2, "dbo");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String minLength = rs.getString("min_length");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldMap.put("minLength", minLength);
                            String template = (String) annotations.getOrDefault("minAndMaxSize", "{{removeLine}}");
                            String result = engine.render(template, fieldMap);
                            col.getValidationAnnotations().remove("maxSize");
                            col.getValidationAnnotations().put("minAndMaxSize", result);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkRegexConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(this.getConstraintQueries().getCheckRegexConstraintQuery())) {
            stmt.setString(1, tableName);
            stmt.setString(2, "dbo");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String pattern = rs.getString("regex_pattern");
                    int pattern_limiter_index = pattern.indexOf("\'");
                    pattern = pattern.substring(0, pattern_limiter_index);
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldMap.put("value", pattern);
                            String template = (String) annotations.getOrDefault("regexPattern", "{{removeLine}}");
                            String result = engine.render(template, fieldMap);
                            col.getValidationAnnotations().put("regexPattern", result);
                            break;
                        }
                    }
                }
            }
        }
    }

}
