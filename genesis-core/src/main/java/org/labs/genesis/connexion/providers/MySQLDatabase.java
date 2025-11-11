package org.labs.genesis.connexion.providers;

import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.labs.utils.StringUtils.toCamelCase;

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

        String query = "SELECT TABLE_NAME " +
                "FROM information_schema.tables " +
                "WHERE TABLE_TYPE = 'BASE TABLE' " +
                "AND TABLE_SCHEMA = DATABASE()"; // base actuellement utilisée

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }

        return tableNames;
    }

    @Override
    public List<String> getAllViewNames(Connection connection) throws SQLException {
        List<String> viewNames = new ArrayList<>();

        String query = "SELECT TABLE_NAME " +
                "FROM information_schema.tables " +
                "WHERE TABLE_TYPE = 'VIEW' " +
                "AND TABLE_SCHEMA = DATABASE()"; // base actuellement utilisée

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {
            while (rs.next()) {
                viewNames.add(rs.getString("TABLE_NAME"));
            }
        }

        return viewNames;
    }

    @Override
    public List<String> getPaginatedTableNames(Connection connection, int index, int size) throws SQLException {
        List<String> tableNames = new ArrayList<>();

        // index = numéro de page (0-based)
        int offset = index * size;

        String query = "SELECT TABLE_NAME " +
                "FROM information_schema.tables " +
                "WHERE TABLE_TYPE = 'BASE TABLE' " +
                "AND TABLE_SCHEMA = DATABASE() " +
                "ORDER BY TABLE_NAME " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tableNames.add(rs.getString("TABLE_NAME"));
                }
            }
        }

        return tableNames;
    }

    @Override
    public List<String> getPaginatedViewNames(Connection connection, int index, int size) throws SQLException {
        List<String> viewNames = new ArrayList<>();
        int offset = index * size;

        String query = "SELECT TABLE_NAME " +
                "FROM information_schema.tables " +
                "WHERE TABLE_TYPE = 'VIEW' " +
                "AND TABLE_SCHEMA = DATABASE() " +
                "ORDER BY TABLE_NAME " +
                "LIMIT ? OFFSET ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, size);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    viewNames.add(rs.getString("TABLE_NAME"));
                }
            }
        }

        return viewNames;
    }

    @Override
    public List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language, Connection connex, Framework framework) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();

        // Récupérer le nom de la base de données active depuis la connexion
        String databaseName = connex.getCatalog();

        try (ResultSet columns = metaData.getColumns(databaseName, null, tableName, null)) {
            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                    framework.getModel().getValidationAnnotations(), 
                    new HashMap<>()
            );
            while (columns.next()) {
                // Vérification CRITIQUE : s'assurer que la colonne vient de la bonne table
                String actualTableName = columns.getString("TABLE_NAME");
                String actualDatabaseName = columns.getString("TABLE_CAT");

                // Filtrer pour n'avoir que les colonnes de la table exacte dans la bonne base
                if (!actualTableName.equalsIgnoreCase(tableName) ||
                        !actualDatabaseName.equalsIgnoreCase(databaseName)) {
                    continue;
                }

                ColumnMetadata column = new ColumnMetadata();
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");

                String isNullable = columns.getString("IS_NULLABLE");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String defaultValue = columns.getString("COLUMN_DEF");
                boolean isColumnNumeric = isColumnNumeric(columns);
                boolean isColumnNumericWithPrecision = isColumnNumericWithPrecision(columns);
                boolean isColumnText = isColumnText(columns);
                boolean isColumnDate = isColumnDate(columns);
                boolean isColumnTime = isColumnTime(columns);
                boolean isColumnTimeTz = isColumnTimeTz(columns);
                boolean isColumnDateTime = isColumnDateTime(columns);
                boolean isColumnDateTimeTz = isColumnDateTimeTz(columns);
                boolean isColumnInterval = isColumnInterval(columns);
                boolean useTimeZone = useTimeZone(columns);

                column.setName(toCamelCase(columnName.toLowerCase()));
                column.setReferencedColumn(columnName);
                column.setNumeric(isColumnNumeric);
                column.setNumericWithPrecision(isColumnNumericWithPrecision);
                column.setText(isColumnText);
                column.setDate(isColumnDate);
                column.setTime(isColumnTime);
                column.setTimeTz(isColumnTimeTz);
                column.setDateTime(isColumnDateTime);
                column.setDateTimeTz(isColumnDateTimeTz);
                column.setUseTimeZone(useTimeZone);
                column.setInterval(isColumnInterval);

                column.setNullable(isNullable,frameworkValidationAnnotations,engine);
                column.setDefaultValue(defaultValue,frameworkValidationAnnotations,engine);
                column.setColumnSize(columnSize,frameworkValidationAnnotations,engine);
                column.setDecimalDigits(decimalDigits,frameworkValidationAnnotations,engine);

                if (language.getTypes().get(getDatabaseType(columns)) == null)
                    throw new RuntimeException("Database type not supported yet : " + columnType +" ["+tableName+"("+columnName+")]");
                else
                    column.setType(language.getTypes().get(getDatabaseType(columns)));

                column.setColumnType(columnType);
                listeCols.add(column);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            checkUnique(metaData, tableName, listeCols,framework);
            checkStrictMinConstraint(connex, tableName, listeCols,framework);
            checkMinConstraint(connex, tableName, listeCols,framework);
            checkStrictMaxConstraint(connex, tableName, listeCols,framework);
            checkMaxConstraint(connex, tableName, listeCols,framework);
            checkStrictPastDateConstraint(connex, tableName, listeCols,framework);
            checkPastDateConstraint(connex, tableName, listeCols,framework);
            checkStrictFutureDateConstraint(connex, tableName, listeCols,framework);
            checkFutureDateConstraint(connex, tableName, listeCols,framework);
            checkNotBlankConstraint(connex, tableName, listeCols,framework);
            checkMinLengthConstraint(connex, tableName, listeCols,framework);
            checkRegexConstraint(connex, tableName, listeCols,framework);
            removeUnusedData(listeCols);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return listeCols;
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
                            Map<String, Object> frameworkValidationAnnotations = Objects.requireNonNullElse(
                                    framework.getModel().getValidationAnnotations(), 
                                    new HashMap<>()
                            );
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
