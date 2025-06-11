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
}

