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


}

