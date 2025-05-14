package org.labs.genesis.connexion;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.TableMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public abstract class Database {
    private int id;
    private String driverName;
    private String driverVersion;
    private String name;
    private String driverType;
    private String sid;
    private Map<Integer, String> connectionString;
    private Map<Integer, String> daoName;
    private Map<Integer, String> addOptions;
    private String driver;
    private String port;
    private Map<String, String> types;
    private List<String> excludeSchemas;
    private Credentials credentials;
    private Map<String, Object> databaseMetadata;
    private Map<String, Framework.Dependency> dependencies;

    public Connection getConnection(Credentials credentials) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        String url = getJdbcUrl(credentials);
        Connection connection = DriverManager.getConnection(url);
        connection.setAutoCommit(false);
        return connection;
    }

    public Connection getConnection(Credentials credentials, String url) throws ClassNotFoundException, SQLException {
        setCredentials(credentials);
        Class.forName(getDriver());
        Connection connection = DriverManager.getConnection(url);
        connection.setAutoCommit(false);
        return connection;
    }

    public abstract String getJdbcUrl(Credentials credentials);

    public TableMetadata getEntity(Connection connection, Credentials credentials, String entityName, Language language) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.setTableName(entityName);
        tableMetadata.initialize(connection, credentials, this, language);
        return tableMetadata;
    }

    public List<TableMetadata> getEntities(Connection connection, Credentials credentials, Language language) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        return tableMetadata.initializeTables(null, connection, credentials, this, language);
    }

    public List<TableMetadata> getViews(Connection connection, Credentials credentials, Language language) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        return tableMetadata.initializeViews(null, connection, credentials, this, language);
    }

    public List<TableMetadata> getEntitiesByNames(List<String> entityNames, Connection connection, Credentials credentials, Language language) throws SQLException, ClassNotFoundException {
        if (entityNames.isEmpty())
            return getEntities(connection, credentials, language);

        List<TableMetadata> tableMetadataList = new ArrayList<>();
        for (String entityName : entityNames) {
            tableMetadataList.add(getEntity(connection, credentials, entityName, language));
        }
        return tableMetadataList;
    }

    public List<TableMetadata> getViewsByNames(List<String> viewNames, Connection connection, Credentials credentials, Language language) throws SQLException, ClassNotFoundException {
        if (viewNames.isEmpty())
            return getViews(connection, credentials, language);

        List<TableMetadata> tableMetadataList = new ArrayList<>();
        for (String viewName : viewNames) {
            TableMetadata viewEntity = getEntity(connection, credentials, viewName, language);
            viewEntity.setIsView(true);
            viewEntity.setPKForView();
            tableMetadataList.add(viewEntity);
        }
        return tableMetadataList;
    }

    public List<String> getAllTableTypeNames(Connection connection, String tableType) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet tables = metaData.getTables(null, credentials.getSchemaName(), "%", new String[]{tableType})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return tableNames;
    }

    public List<String> getAllTableNames(Connection connection) throws SQLException {
        return getAllTableTypeNames(connection, "TABLE");
    }

    public List<String> getAllViewNames(Connection connection) throws SQLException {
        return getAllTableTypeNames(connection, "VIEW");
    }


    public Map<String, Object> getDatabaseMetadataHashMap(Credentials credentials) {
        Map<String, Object> databaseMetadata = new HashMap<>();

        databaseMetadata.put("host", credentials.getHost());
        databaseMetadata.put("port", credentials.getPort());
        databaseMetadata.put("database", credentials.getSchemaName());
        databaseMetadata.put("username", credentials.getUser());
        databaseMetadata.put("password", credentials.getPwd());
        databaseMetadata.put("useSSL", String.valueOf(credentials.isUseSSL()));
        databaseMetadata.put("allowPublicKeyRetrieval", String.valueOf(credentials.isAllowPublicKeyRetrieval()));
        databaseMetadata.put("driverType", driverType);
        databaseMetadata.put("sid", sid);

        return databaseMetadata;
    }

    @Override
    public String toString() {
        return name;
    }


    public List<String> getPaginatedTableNames(Connection connection, int index, int size) throws SQLException {
        List<String> tableNames = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();

        int tempIndex = 0;
        try (ResultSet tables = metaData.getTables(null, credentials.getSchemaName(), "%", new String[]{"TABLE"})) {
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
        try (ResultSet views = metaData.getTables(null, credentials.getSchemaName(), "%", new String[]{"VIEW"})) {
            while (views.next() && size > viewNames.size()) {
//                if (tempIndex < (index * size)) {
//                    tempIndex++;
//                    continue;
//                }
                String viewName = views.getString("TABLE_NAME");
                viewNames.add(viewName);
                tempIndex++;
            }
        }

        return viewNames;
    }
}
