package org.labs.genesis.connexion.providers;

import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
                            col.setHasMaximumConstraint(true);
                            col.setMaximumConstraint(value);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
        String sql = this.getConstraintQueries().getCheckNotBlankConstraintQuery();
        try (PreparedStatement stmt = connex.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, this.getCredentials().getDatabaseName());
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

    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
                            col.setHasMinimumLengthConstraint(true);
                            col.setMinimumLengthConstraint(minLength);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns) throws SQLException {
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
