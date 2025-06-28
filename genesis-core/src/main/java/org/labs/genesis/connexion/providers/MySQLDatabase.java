package org.labs.genesis.connexion.providers;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MySQLDatabase extends Database {

    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useSSL=%s&allowPublicKeyRetrieval=%s",
                credentials.getHost(),
                port,
                credentials.getDatabaseName(),
                credentials.getUser(),
                credentials.getPwd(),
                credentials.isUseSSL(),
                credentials.isAllowPublicKeyRetrieval());
    }


    @Override
    public List<String> getAllTableNames(Connection connection) throws SQLException {
        List<String> tableNames = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet tables = statement.executeQuery("SHOW FULL TABLES WHERE Table_type = 'BASE TABLE'")) {
            while (tables.next()) {
                String tableName = tables.getString(1);
                tableNames.add(tableName);
            }
        }

        return tableNames;
    }

    @Override
    public List<String> getAllViewNames(Connection connection) throws SQLException {
        List<String> viewNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (Statement statement = connection.createStatement();
             ResultSet views = statement.executeQuery("SHOW FULL TABLES WHERE Table_type = 'VIEW'")) {
            while (views.next()) {
                String viewName = views.getString(1);
                viewNames.add(viewName);
            }
        }

        return viewNames;
    }

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictMinimumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("min_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMinimumValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumValue","{{removeLine}}");
                            String annotationResult = this.engine.render(annotationTemplate, fieldHashMap);
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
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictMaximumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("max_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMaximumValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMaximumValue","{{removeLine}}");
                            String annotationResult = this.engine.render(annotationTemplate, fieldHashMap);
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
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMinimumConstraintQuery();

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("min_value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldHashMap.put("value",value);
                            col.getValidationAnnotations().put("numericMinimumInclusiveValueData",value);
                            String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumInclusiveValue","{{removeLine}}");
                            String annotationResult = this.engine.render(annotationTemplate, fieldHashMap);
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
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMaximumConstraintQuery();

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("max_value");

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
    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckNotBlankConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
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
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckMinimumLengthConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
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
    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckRegexConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String pattern = rs.getString("regex_pattern");
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
