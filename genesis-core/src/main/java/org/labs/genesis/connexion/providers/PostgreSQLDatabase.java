package org.labs.genesis.connexion.providers;

import org.apache.commons.text.StringEscapeUtils;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostgreSQLDatabase extends Database {
    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:postgresql://%s:%s/%s?user=%s&password=%s",
                credentials.getHost(),
                port,
                credentials.getDatabaseName(),
                credentials.getUser(),
                credentials.getPwd());
    }

    @Override
    public List<String> getAllTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    @Override
    public List<String> getAllViewNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictMinimumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMinimumValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumValue","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("numericMinimumValue", annotationResult);
                            col.checkAndCreateRangeAnnotation(
                                    frameworkValidationAnnotations,
                                    fieldHashMap,
                                    engine,
                                    value,
                                    true
                            );
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMinimumConstraintQuery();

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMinimumInclusiveValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumInclusiveValue","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("numericMinimumInclusiveValue", annotationResult);
                            col.checkAndCreateRangeAnnotation(
                                    frameworkValidationAnnotations,
                                    fieldHashMap,
                                    engine,
                                    value,
                                    true
                            );
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictMaximumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMaximumValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMaximumValue","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("numericMaximumValue", annotationResult);
                            col.checkAndCreateRangeAnnotation(
                                    frameworkValidationAnnotations,
                                    fieldHashMap,
                                    engine,
                                    value,
                                    false
                            );
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMaximumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMaximumInclusiveValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMaximumInclusiveValue","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("numericMaximumInclusiveValue", annotationResult);
                            col.checkAndCreateRangeAnnotation(
                                    frameworkValidationAnnotations,
                                    fieldHashMap,
                                    engine,
                                    value,
                                    false
                            );
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkStrictPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        // Strict past: col > CURRENT_DATE ou CURRENT_DATE < col
        String sql = this.getConstraintQueries().getCheckStrictPastDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("past","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("past",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        // Past with inclusion: col >= CURRENT_DATE ou CURRENT_DATE <= col
        String sql = this.getConstraintQueries().getCheckPastDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("pastOrPresent","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("pastOrPresent",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkStrictFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        // Strict future: col < CURRENT_DATE ou CURRENT_DATE > col
        String sql = this.getConstraintQueries().getCheckStrictFutureDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("future","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("future",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        // Future with inclusion: col <= CURRENT_DATE ou CURRENT_DATE >= col
        String sql = this.getConstraintQueries().getCheckFutureDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String operator = rs.getString("operator");
                    String dateFunc = rs.getString("date_function");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("futureOrPresent","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("futureOrPresent",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckNotBlankConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("notBlank","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("notBlank",annotationResult);
                            col.checkAndCreateNotNullNotBlankCombinedAnnotation(
                                    frameworkValidationAnnotations,
                                    fieldHashMap,
                                    engine
                            );
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMinimumLengthConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String minLength = rs.getString("min_length");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("minLength",minLength);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("minAndMaxSize","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().remove("maxSize");
                            col.getValidationAnnotations().put("minAndMaxSize",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckRegexConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String pattern = rs.getString("regex_pattern");
                    pattern = StringEscapeUtils.escapeJava(pattern);
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",pattern);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("regexPattern","{{removeLine}}");
                            String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                            col.getValidationAnnotations().put("regexPattern",annotationResult);
                            break;
                        }
                    }
                }
            }
        }
    }
}

