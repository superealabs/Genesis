package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.connexion.model.ColumnMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    private static final String GENERIC_NUMERIC_CHECK_SQL = """
        WITH check_constraints AS (
            SELECT c.conname AS constraint_name,
                   t.relname AS table_name,
                   pg_get_constraintdef(c.oid) AS constraint_definition
            FROM pg_constraint c
            JOIN pg_class t ON c.conrelid = t.oid
            WHERE c.contype = 'c' AND t.relkind = 'r'
        ),
        cleaned AS (
            SELECT constraint_name, table_name, constraint_definition,
                   regexp_replace(constraint_definition, '[\\(\\)]', '', 'g') AS cleaned_def
            FROM check_constraints
        ),
        matches AS (
            SELECT constraint_name, table_name, constraint_definition, cleaned_def,
                   regexp_matches(cleaned_def, '%s', 'g') AS match,
                   regexp_matches(cleaned_def, '%s', 'g') AS inverse_match
            FROM cleaned       
        )
        SELECT table_name,
               COALESCE(match[1], inverse_match[3]) AS column_name,
               COALESCE(match[3], inverse_match[1])::numeric AS value
        FROM matches
        WHERE table_name = ?
          AND (match IS NOT NULL OR inverse_match IS NOT NULL);
    """;

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String pattern = "(\\w+)\\s*(>)\\s*([0-9]+(?:\\.[0-9]+)?)";
        String inversePattern = "([0-9]+(?:\\.[0-9]+)?)\\s*(<)\\s*(\\w+)";
        String sql = String.format(GENERIC_NUMERIC_CHECK_SQL, pattern, inversePattern);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasStrictMinimumConstraint(true);
                            col.setStrictMinimumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String pattern = "(\\w+)\\s*(>=)\\s*([0-9]+(?:\\.[0-9]+)?)";
        String inversePattern = "([0-9]+(?:\\.[0-9]+)?)\\s*(<=)\\s*(\\w+)";
        String sql = String.format(GENERIC_NUMERIC_CHECK_SQL, pattern, inversePattern);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasMinimumConstraint(true);
                            col.setMinimumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String pattern = "(\\w+)\\s*(<)\\s*([0-9]+(?:\\.[0-9]+)?)";
        String inversePattern = "([0-9]+(?:\\.[0-9]+)?)\\s*(>)\\s*(\\w+)";
        String sql = String.format(GENERIC_NUMERIC_CHECK_SQL, pattern, inversePattern);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasStrictMaximumConstraint(true);
                            col.setStrictMaximumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String pattern = "(\\w+)\\s*(<=)\\s*([0-9]+(?:\\.[0-9]+)?)";
        String inversePattern = "([0-9]+(?:\\.[0-9]+)?)\\s*(>=)\\s*(\\w+)";
        String sql = String.format(GENERIC_NUMERIC_CHECK_SQL, pattern, inversePattern);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String value = rs.getString("value");

                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(columnName) && col.isNumeric()) {
                            col.setHasMaximumConstraint(true);
                            col.setMaximumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String GENERIC_DATE_CHECK_SQL = """
        WITH check_constraints AS (
            SELECT
                c.conname AS constraint_name,
                t.relname AS table_name,
                pg_get_constraintdef(c.oid) AS constraint_definition
            FROM
                pg_constraint c
            JOIN
                pg_class t ON c.conrelid = t.oid
            WHERE
                c.contype = 'c'  -- CHECK
                AND t.relkind = 'r'  -- tables
                AND (
                    pg_get_constraintdef(c.oid) ~* '%s'
                    OR pg_get_constraintdef(c.oid) ~* '%s'
                )
        ),
        parsed AS (
            SELECT
                constraint_name,
                table_name,
                constraint_definition,
                regexp_matches(constraint_definition, '%s', 'i') AS m1,
                regexp_matches(constraint_definition, '%s', 'i') AS m2
            FROM
                check_constraints
        )
        SELECT
            constraint_name,
            table_name,
            COALESCE(m1[1], m2[3]) AS column_name,
            COALESCE(m1[2], m2[2]) AS operator,
            COALESCE(m1[3], m2[1]) AS date_function
        FROM
            parsed
        WHERE
            table_name = ?
            AND (m1 IS NOT NULL OR m2 IS NOT NULL)
        ORDER BY
            table_name,
            column_name;
    """;

    protected void checkStrictPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Strict past: col > CURRENT_DATE ou CURRENT_DATE < col
        String pattern1 = "(\\w+)\\s*(>)\\s*(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))";
        String pattern2 = "(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))\\s*(<)\\s*(\\w+)";
        String sql = String.format(GENERIC_DATE_CHECK_SQL, pattern1, pattern2, pattern1, pattern2);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            col.setHasStrictPastDateConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void checkPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Past with inclusion: col >= CURRENT_DATE ou CURRENT_DATE <= col
        String pattern1 = "(\\w+)\\s*(>=)\\s*(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))";
        String pattern2 = "(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))\\s*(<=)\\s*(\\w+)";
        String sql = String.format(GENERIC_DATE_CHECK_SQL, pattern1, pattern2, pattern1, pattern2);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            col.setHasPastDateConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void checkStrictFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Strict future: col < CURRENT_DATE ou CURRENT_DATE > col
        String pattern1 = "(\\w+)\\s*(<)\\s*(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))";
        String pattern2 = "(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))\\s*(>)\\s*(\\w+)";
        String sql = String.format(GENERIC_DATE_CHECK_SQL, pattern1, pattern2, pattern1, pattern2);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            col.setHasStrictFutureDateConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void checkFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Future with inclusion: col <= CURRENT_DATE ou CURRENT_DATE >= col
        String pattern1 = "(\\w+)\\s*(<=)\\s*(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))";
        String pattern2 = "(CURRENT_DATE|CURRENT_TIMESTAMP|NOW\\(\\))\\s*(>=)\\s*(\\w+)";
        String sql = String.format(GENERIC_DATE_CHECK_SQL, pattern1, pattern2, pattern1, pattern2);

        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String operator = rs.getString("operator");
                    String dateFunc = rs.getString("date_function");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isDate()) {
                            col.setHasFutureDateConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String TRIM_NOT_BLANK_SQL = """
        WITH check_constraints AS (
            SELECT
                c.conname AS constraint_name,
                t.relname AS table_name,
                pg_get_constraintdef(c.oid) AS constraint_definition
            FROM pg_constraint c
            JOIN pg_class t ON c.conrelid = t.oid
            WHERE c.contype = 'c'
              AND t.relkind = 'r'
              AND pg_get_constraintdef(c.oid) ~* '(<>\s*''|::text\s*<>\s*''::text)'
        )
        SELECT
            cc.constraint_name,
            cc.table_name,
            cc.constraint_definition,
            matches[1] AS column_name
        FROM check_constraints cc
        CROSS JOIN LATERAL regexp_matches(
            cc.constraint_definition,
            '(?:trim\\s*\\(\\s*(?:both\\s+from\\s+)?(\\w+)\\s*\\)|(\\w+)|\\(?(\\w+)\\)?::text)\\s*<>\\s*''(?:::text)?',
            'i'
        ) AS matches
        WHERE cc.table_name = ?
        ORDER BY cc.table_name, column_name;
    """;

    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (PreparedStatement stmt = connex.prepareStatement(TRIM_NOT_BLANK_SQL)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            col.setHasNotBlankConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String MIN_LENGTH_SQL = """
        WITH check_constraints AS (
            SELECT
                c.conname AS constraint_name,
                t.relname AS table_name,
                pg_get_constraintdef(c.oid) AS constraint_definition
            FROM pg_constraint c
            JOIN pg_class t ON c.conrelid = t.oid
            WHERE c.contype = 'c'
              AND t.relkind = 'r'
              AND pg_get_constraintdef(c.oid) ~* 'length\\s*\\('
        )
        SELECT
            cc.constraint_name,
            cc.table_name,
            cc.constraint_definition,
            matches[1] AS column_name,
            matches[3] AS operator,
            matches[4]::int AS min_length
        FROM check_constraints cc
        CROSS JOIN LATERAL regexp_matches(
            cc.constraint_definition,
            'length\\s*\\(\\s*\\(*\\s*(\\w+)\\s*\\)*\\s*(::text)?\\s*\\)\\s*(>=|>)\\s*(\\d+)',
            'i'
        ) AS matches
        WHERE cc.table_name = ?
        ORDER BY cc.table_name, column_name;
    """;

    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (PreparedStatement stmt = connex.prepareStatement(MIN_LENGTH_SQL)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String minLength = rs.getString("min_length");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            col.setHasMinimumLengthConstraint(true);
                            col.setMinimumLengthConstraint(minLength);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static final String REGEX_CONSTRAINT_SQL = """
        WITH check_constraints AS (
            SELECT
                c.conname AS constraint_name,
                t.relname AS table_name,
                pg_get_constraintdef(c.oid) AS constraint_definition
            FROM pg_constraint c
            JOIN pg_class t ON c.conrelid = t.oid
            WHERE c.contype = 'c'
                AND t.relkind = 'r'
                AND pg_get_constraintdef(c.oid) ~ '(::text)?\\s*~\\*?\\s*'''
        ),
        regex_matches AS (
            SELECT
                constraint_name,
                table_name,
                constraint_definition,
                regexp_replace(constraint_definition, E'[()]', '', 'g') AS cleaned_def
            FROM check_constraints
        ),
        parsed AS (
            SELECT
                constraint_name,
                table_name,
                constraint_definition,
                regexp_matches(
                    cleaned_def,
                    E'(\\\\w+)(::text)?\\\\s*(~\\\\*?)\\\\s*''([^'']+)''(::text)?',
                    'i'
                ) AS match
            FROM regex_matches
        )
        SELECT
            constraint_name,
            table_name,
            match[1] AS column_name,
            match[3] AS operator,
            match[4] AS regex_pattern,
            constraint_definition
        FROM parsed
        WHERE table_name = ?;
    """;

    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (PreparedStatement stmt = connex.prepareStatement(REGEX_CONSTRAINT_SQL)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String pattern = rs.getString("regex_pattern");
                    for (ColumnMetadata col : columns) {
                        if (col.getReferencedColumn().equalsIgnoreCase(colName) && col.isText()) {
                            col.setHasRegexConstraint(true);
                            col.setRegexConstraint(pattern);
                            break;
                        }
                    }
                }
            }
        }
    }
}

