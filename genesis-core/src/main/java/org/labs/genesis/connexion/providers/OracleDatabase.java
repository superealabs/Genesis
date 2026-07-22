package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OracleDatabase extends Database {




    public List<String> getPaginatedTableNames(Connection connection, int index, int size) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        int tempIndex = 0;
        try (ResultSet tables = metaData.getTables(null, resolveOracleSchema(), "%", new String[]{"TABLE"})) {
            while (tables.next() && size > tableNames.size()) {
                if (tempIndex < (index * size)) {
                    tempIndex++;
                    continue;
                }
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
                tempIndex++;
            }
        }

        return tableNames;
    }

    public List<String> getPaginatedViewNames(Connection connection, int index, int size) throws SQLException {
        List<String> viewNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        int tempIndex = 0;
        try (ResultSet views = metaData.getTables(null, resolveOracleSchema(), "%", new String[]{"VIEW"})) {
            while (views.next() && size > viewNames.size()) {
                if (tempIndex < (index * size)) {
                    tempIndex++;
                    continue;
                }
                String viewName = views.getString("TABLE_NAME");
                viewNames.add(viewName);
                tempIndex++;
            }
        }

        return viewNames;
    }

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictMinimumConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile("\\s*>\\s*([0-9]+(?:\\.[0-9]+)?)");
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String value = matcher.group(1);
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                fieldHashMap.put("value", value);
                                col.getValidationAnnotations().put("numericMinimumValueData", value);

                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumValue", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);

                                col.getValidationAnnotations().put("numericMinimumValue", annotationResult);
                                col.checkAndCreateRangeAnnotation(frameworkValidationAnnotations, fieldHashMap, engine, value, true);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile("\\s*>=\\s*([0-9]+(?:\\.[0-9]+)?)");
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String value = matcher.group(1);
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                fieldHashMap.put("value", value);
                                col.getValidationAnnotations().put("numericMinimumInclusiveValueData", value);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMinimumInclusiveValue", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("numericMinimumInclusiveValue", annotationResult);
                                col.checkAndCreateRangeAnnotation(frameworkValidationAnnotations, fieldHashMap, engine, value, true);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile("\\s*<\\s*([0-9]+(?:\\.[0-9]+)?)");
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String value = matcher.group(1);
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                fieldHashMap.put("value", value);
                                col.getValidationAnnotations().put("numericMaximumValueData", value);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMaximumValue", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("numericMaximumValue", annotationResult);
                                col.checkAndCreateRangeAnnotation(frameworkValidationAnnotations, fieldHashMap, engine, value, false);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile("\\s*<=\\s*([0-9]+(?:\\.[0-9]+)?)");
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String value = matcher.group(1);
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                fieldHashMap.put("value", value);
                                col.getValidationAnnotations().put("numericMaximumInclusiveValueData", value);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("numericMaximumInclusiveValue", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("numericMaximumInclusiveValue", annotationResult);
                                col.checkAndCreateRangeAnnotation(frameworkValidationAnnotations, fieldHashMap, engine, value, false);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void checkStrictPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictPastDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                Pattern strictPastPattern = Pattern.compile(
                        "(\\w+)\\s*>\\s*SYSDATE|SYSDATE\\s*<\\s*(\\w+)",
                        Pattern.CASE_INSENSITIVE
                );
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Matcher matcher = strictPastPattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String matchedColName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(matchedColName) &&
                                    (col.isDate() || col.isDateTime())
                            )
                            {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("past", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("past", annotationResult);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void checkPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckPastDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile(
                            "\\bSYSDATE\\b\\s*<=\\s*\\w+|\\w+\\s*>=\\s*\\bSYSDATE\\b",
                            Pattern.CASE_INSENSITIVE
                    );
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colName) &&
                                        (col.isDate() || col.isDateTime())
                            )
                            {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("pastOrPresent", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("pastOrPresent", annotationResult);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void checkStrictFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckStrictFutureDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile(
                            "\\bSYSDATE\\b\\s*<\\s*\\w+|\\w+\\s*>\\s*\\bSYSDATE\\b",
                            Pattern.CASE_INSENSITIVE
                    );
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colName) &&
                                        (col.isDate() || col.isDateTime())
                            )
                            {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("future", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("future", annotationResult);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void checkFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns, Framework framework) throws Exception {
        String sql = this.getConstraintQueries().getCheckFutureDateConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile(
                            "\\bSYSDATE\\b\\s*<=\\s*\\w+|\\w+\\s*>=\\s*\\bSYSDATE\\b",
                            Pattern.CASE_INSENSITIVE
                    );
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colName) &&
                                        (col.isDate() || col.isDateTime())
                            )
                            {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);
                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("futureOrPresent", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("futureOrPresent", annotationResult);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile("TRIM\\([^\\)]+\\)\\s*<>\\s*''|IS\\s+NOT\\s+NULL", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = pattern.matcher(searchCondition);
                    if (matcher.find()) {
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);

                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("notBlank", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);
                                col.getValidationAnnotations().put("notBlank", annotationResult);
                                col.checkAndCreateNotNullNotBlankCombinedAnnotation(frameworkValidationAnnotations, fieldHashMap, engine);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    Pattern pattern = Pattern.compile(
                            "LENGTH\\s*\\(\\s*(?:TRIM\\s*\\(\\s*)?(\\w+)(?:\\s*\\))?\\s*\\)\\s*>=\\s*(\\d+)",
                            Pattern.CASE_INSENSITIVE
                    );
                    Matcher matcher = pattern.matcher(searchCondition);

                    if (matcher.find()) {
                        String colNameFromExpression = matcher.group(1);
                        String minLength = matcher.group(2);

                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colNameFromExpression) && col.isText()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);

                                fieldHashMap.put("minLength", minLength);

                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("minAndMaxSize", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);

                                col.getValidationAnnotations().remove("maxSize");
                                col.getValidationAnnotations().put("minAndMaxSize", annotationResult);
                                break;
                            }
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
            stmt.setString(1, tableName.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String searchCondition = rs.getString("search_condition");
                    if (searchCondition == null) continue;
                    Pattern regexpLikePattern = Pattern.compile(
                            "REGEXP_LIKE\\s*\\(\\s*\\w+\\s*,\\s*'([^']*)'",
                            Pattern.CASE_INSENSITIVE
                    );
                    Matcher matcher = regexpLikePattern.matcher(searchCondition);
                    if (matcher.find()) {
                        String extractedPattern = matcher.group(1);
                        String processedPattern = extractedPattern.replace("\\", "\\\\");

                        if (processedPattern.startsWith("^")) {
                            processedPattern = processedPattern.substring(1);
                        }
                        if (processedPattern.endsWith("$")) {
                            processedPattern = processedPattern.substring(0, processedPattern.length() - 1);
                        }
                        for (ColumnMetadata col : columns) {
                            if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                                Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
                                Map<String, Object> fieldHashMap = FrameworkMetadataProvider.getFieldHashMap(col);

                                fieldHashMap.put("value", processedPattern);

                                String annotationTemplate = (String) frameworkValidationAnnotations.getOrDefault("regexPattern", "{{removeLine}}");
                                String annotationResult = engine.render(annotationTemplate, fieldHashMap);

                                col.getValidationAnnotations().put("regexPattern", annotationResult);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }



    public String handleType(String columnType, int decimalDigits) {
        if (decimalDigits > 0 && columnType.contains("NUMBER")) {
            return columnType + "(*,*)";
        }
        if (columnType.contains("TIMESTAMP")) {
            return "TIMESTAMP";
        }
        return columnType;
    }

    private Set<String> fetchIdentityColumns(Connection connection, String tableName) {
        Set<String> identityColumns = new HashSet<>();
        String sql = """
            SELECT COLUMN_NAME
            FROM ALL_TAB_IDENTITY_COLS
            WHERE OWNER = ?
              AND TABLE_NAME = ?
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resolveOracleSchema());
            statement.setString(2, tableName.toUpperCase());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    identityColumns.add(resultSet.getString("COLUMN_NAME"));
                }
            }
        } catch (SQLException exception) {
            return identityColumns;
        }
        return identityColumns;
    }
    @Override
    public List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language,Connection connex,Framework framework) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();
        Set<String> identityColumns = fetchIdentityColumns(connex, tableName);

        try (ResultSet columns = metaData.getColumns(null, resolveOracleSchema(), tableName, null)) {
            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
            while (columns.next()) {
                ColumnMetadata column = new ColumnMetadata();
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                columnType = handleType(columnType, decimalDigits);
                // COLUMN_DEF doit être lu avant IS_NULLABLE
                String defaultValue = columns.getString("COLUMN_DEF");
                String isNullable = columns.getString("IS_NULLABLE");
                boolean isColumnNumeric = isColumnNumeric(columns);
                boolean isColumnNumericWithPrecision = isColumnNumericWithPrecision(columns);
                boolean isColumnText = isColumnText(columns);
                boolean isColumnDate = isColumnDate(columns);
                boolean isColumnTime = isColumnTime(columns);
                boolean isColumnTimeTz = isColumnTimeTz(columns);
                boolean isColumnDateTime = isColumnDateTime(columns);
                boolean isColumnDateTimeTz = isColumnDateTimeTz(columns);
                boolean useTimeZone = useTimeZone(columns);
                boolean isColumnInterval = isColumnInterval(columns);

                column.setName(StringUtils.toCamelCase(columnName.toLowerCase()));
                column.setReferencedColumn(columnName);
                column.setAutoGenerated(isAutoGenerated(columns)|| identityColumns.stream().anyMatch(identityColumn -> identityColumn.equalsIgnoreCase(columnName)));
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
                    throw new RuntimeException("Database type not supported yet : " + columnType);
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
    public List<String> getAllTableTypeNames(Connection connection, String tableType) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        // Résolution intelligente du schéma pour Oracle
        String schema = getCredentials().getSchemaName();
        if (schema == null || schema.isEmpty()) {
            schema = getCredentials().getUser();
        }
        if (schema != null && schema.equals(schema.toUpperCase())) {
            schema = schema.toUpperCase();
        }
        // Si contient des minuscules → identifiant quoté, on garde la casse exacte

        // Utilisation de l'API JDBC standard au lieu de la vue legacy "tab"
        try (ResultSet rs = metaData.getTables(null, schema, "%", new String[]{tableType.toUpperCase()})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                // Exclure les tables corbeille Oracle (BIN$...)
                if (!tableName.startsWith("BIN$")) {
                    tableNames.add(tableName);
                }
            }
        }
        return tableNames;
    }

    @Override
    public Connection getConnection(Credentials credentials) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        String url = getJdbcUrl(credentials);
        Connection connection = DriverManager.getConnection(url, credentials.getUser(), credentials.getPwd());
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public Connection getConnection(Credentials credentials, String url) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        Connection connection = DriverManager.getConnection(url, credentials.getUser(), credentials.getPwd());
        connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
        return String.format("jdbc:oracle:%s:@//%s:%s/%s",
                getDriverType(),
                credentials.getHost(),
                port,
                credentials.getSID());
    }


    private String resolveOracleSchema() {
        String schema = getCredentials().getSchemaName();
        if (schema == null || schema.isEmpty()) {
            schema = getCredentials().getUser();
        }
        if (schema == null) return null;
        if (!schema.equals(schema.toUpperCase())) {
            return schema;
        }
        return schema.toUpperCase();
    }
}
