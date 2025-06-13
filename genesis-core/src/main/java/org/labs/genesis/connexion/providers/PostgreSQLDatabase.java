package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = this.getConstraintQueries().getCheckStrictMinimumConstraintQuery();
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
        String sql = this.getConstraintQueries().getCheckMinimumConstraintQuery();

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
        String sql = this.getConstraintQueries().getCheckStrictMaximumConstraintQuery();
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
        String sql = this.getConstraintQueries().getCheckMaximumConstraintQuery();
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

    @Override
    protected void checkStrictPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Strict past: col > CURRENT_DATE ou CURRENT_DATE < col
        String sql = this.getConstraintQueries().getCheckStrictPastDateConstraintQuery();
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

    @Override
    protected void checkPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Past with inclusion: col >= CURRENT_DATE ou CURRENT_DATE <= col
        String sql = this.getConstraintQueries().getCheckPastDateConstraintQuery();
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

    @Override
    protected void checkStrictFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        // Strict future: col < CURRENT_DATE ou CURRENT_DATE > col
        String sql = this.getConstraintQueries().getCheckStrictFutureDateConstraintQuery();
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

    @Override
    protected void checkFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
                            col.setHasFutureDateConstraint(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = this.getConstraintQueries().getCheckNotBlankConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
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

    @Override
    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = this.getConstraintQueries().getCheckMinimumLengthConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
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

    @Override
    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = this.getConstraintQueries().getCheckRegexConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
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

