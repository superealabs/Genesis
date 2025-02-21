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



}
