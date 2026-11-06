package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class SQLServerDatabase extends Database {
    @Override
    public String resolveSchema(Connection connection) throws SQLException {
        if (getCredentials() != null && getCredentials().getSchemaName() != null && !getCredentials().getSchemaName().isEmpty()) {
            return getCredentials().getSchemaName();
        }
        return "dbo";
    }
    @Override
    public String getJdbcUrl(Credentials credentials) {
        String port;
        if (credentials.getPort() != null)
            port = credentials.getPort();
        else port = getPort();
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
            stmt.setString(2, resolveSchema(conn));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) &&
                                (col.isDate() || col.isDateTime())
                        )
                        {
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
            stmt.setString(2, resolveSchema(conn));
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
            stmt.setString(2, resolveSchema(conn));
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
        detectJsonColumns(conn, tableName, columns);
        try (PreparedStatement stmt = conn.prepareStatement(this.getConstraintQueries().getCheckRegexConstraintQuery())) {
            stmt.setString(1, tableName);
            stmt.setString(2, resolveSchema(conn));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String pattern = rs.getString("regex_pattern");
                    String likeOperator = rs.getString("like_operator");
                    boolean negated = "NOT LIKE".equalsIgnoreCase(likeOperator);
                    pattern = convertSqlServerLikeToRegex(pattern, negated);
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            Map<String, Object> annotations = framework.getModel().getValidationAnnotations();
                            Map<String, Object> fieldMap = FrameworkMetadataProvider.getFieldHashMap(col);
                            fieldMap.put("value", StringUtils.correctPattern(pattern));
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

    private void checkNumericConstraint(Connection conn, String tableName, List<ColumnMetadata> columns, Framework framework, String annotationKey, String dataKey, String sql, boolean isMin) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, resolveSchema(conn));
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
    public String normalizeColumnType(String columnType) {
        if (columnType == null) {
            return null;
        }
        String normalized = columnType
                .trim()
                .replaceAll("(?i)\\s+identity$", "");
        return super.normalizeColumnType(normalized);
    }

    @Override
    protected boolean isAutoGenerated(ResultSet columns) {
        if (super.isAutoGenerated(columns)) {
            return true;
        }
        try {
            String typeName = columns.getString("TYPE_NAME");
            return typeName != null && typeName.toLowerCase().contains("identity");
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Convertit un pattern LIKE SQL Server en regex Java équivalente.
     * Gère : % , _ , [...] / [^...] (classes de caractères), et échappe
     * les autres métacaractères regex qui sont littéraux en LIKE.
     */
    private String convertSqlServerLikeToRegex(String pattern, boolean negated) {
        if (pattern == null) {
            return null;
        }
        String body = convertSqlServerLikeToRegexBody(pattern.trim());

        if (negated) {
            return "^(?!" + body + "$)[\\s\\S]*$";
        }

        return "^" + body + "$";
    }

    private String convertSqlServerLikeToRegexBody(String pattern) {
        StringBuilder regex = new StringBuilder();

        int i = 0;

        while (i < pattern.length()) {
            char c = pattern.charAt(i);

            switch (c) {
                case '%':
                    regex.append("[\\s\\S]*");
                    i++;
                    break;

                case '_':
                    regex.append("[\\s\\S]");
                    i++;
                    break;

                case '[':
                    int close = findClosingBracket(pattern, i);

                    if (close == -1) {
                        regex.append("\\[");
                        i++;
                    } else {
                        regex.append(
                                convertCharClass(
                                        pattern.substring(i, close + 1)
                                )
                        );
                        i = close + 1;
                    }
                    break;

                case '\\':
                case '.':
                case '^':
                case '$':
                case '|':
                case '?':
                case '*':
                case '+':
                case '(':
                case ')':
                case '{':
                case '}':
                case ']':
                    regex.append('\\').append(c);
                    i++;
                    break;

                default:
                    regex.append(c);
                    i++;
            }
        }

        return regex.toString();
    }

    // Trouve l'index du ']' fermant une classe [...] ou [^...], -1 si absent
    private int findClosingBracket(String pattern, int openIndex) {
        int i = openIndex + 1;
        if (i < pattern.length() && pattern.charAt(i) == '^') {
            i++;
        }
        while (i < pattern.length()) {
            if (pattern.charAt(i) == ']') {
                return i;
            }
            i++;
        }
        return -1;
    }

    // Traduit une classe SQL Server [xxx] / [^xxx] en classe regex équivalente.
    private String convertCharClass(String sqlClass) {
        StringBuilder sb = new StringBuilder("[");
        int i = 1;
        int end = sqlClass.length() - 1; // exclut le ']' final

        if (i < end && sqlClass.charAt(i) == '^') {
            sb.append('^');
            i++;
        }
        for (; i < end; i++) {
            char c = sqlClass.charAt(i);
            // à l'intérieur d'une classe, seuls \ , [ et ] doivent être échappés
            if (c == '\\' || c == '[' || c == ']') {
                sb.append('\\');
            }
            sb.append(c);
        }
        sb.append(']');
        return sb.toString();
    }

    private boolean isJsonColumn(Connection connection, String schema, String tableName, String columnName) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM sys.check_constraints cc
            JOIN sys.tables t
                ON cc.parent_object_id = t.object_id
            JOIN sys.schemas s
                ON t.schema_id = s.schema_id
            WHERE s.name = ?
              AND t.name = ?
              AND LOWER(REPLACE(REPLACE(cc.definition, '[', ''), ']', ''))
                 LIKE ?
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            ps.setString(3, "%isjson(" + columnName.toLowerCase() + ")%");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        }
    }

    private void detectJsonColumns(Connection connection, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String schema = resolveSchema(connection);
        for (ColumnMetadata column : columns) {
            if (column.isText()) {
                column.setJson(isJsonColumn(connection, schema, tableName, column.getReferencedColumn())
                );
            }
        }
    }
}
