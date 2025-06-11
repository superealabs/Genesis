package org.labs.genesis.connexion;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.labs.utils.StringUtils.toCamelCase;
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

    public List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language,Connection connex) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();
        try (ResultSet columns = metaData.getColumns(null, this.getCredentials().getSchemaName(), tableName, null)) {
            while (columns.next()) {
                ColumnMetadata column = new ColumnMetadata();
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");

                String isNullable = columns.getString("IS_NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");
                int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                int columnSize = columns.getInt("COLUMN_SIZE");
                boolean isColumnNumeric = isColumnNumeric(columns);
                boolean isColumnNumericWithPrecision = isColumnNumericWithPrecision(columns);
                boolean isColumnText = isColumnText(columns);
                boolean isColumnDate = isColumnDate(columns);

                column.setName(toCamelCase(columnName.toLowerCase()));
                column.setReferencedColumn(columnName);
                column.setNullable(isNullable);
                column.setDefaultValue(defaultValue);
                column.setDecimalDigits(decimalDigits);
                column.setColumnSize(columnSize);
                column.setNumeric(isColumnNumeric);
                column.setNumericWithPrecision(isColumnNumericWithPrecision);
                column.setText(isColumnText);
                column.setDate(isColumnDate);

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
        return listeCols;
    }

    private String getDatabaseType(ResultSet columns) throws Exception {
        String columnType = columns.getString("TYPE_NAME");

        if (columns.getInt("DATA_TYPE") == Types.NUMERIC && this.getId() == Constantes.Oracle_ID) {
            if (columns.getInt("DECIMAL_DIGITS") > 0) {
                columnType = getBeforeBracketsSimple(columnType) + "(*,*)";
            } else {
                columnType = getBeforeBracketsSimple(columnType);
            }
        }
        if (columns.getInt("DATA_TYPE") == Types.TIMESTAMP && this.getId() == Constantes.Oracle_ID) {
            columnType = getBeforeBracketsSimple(columnType);
        }
        return this.getTypes().get(columnType);
    }

    private String getBeforeBracketsSimple(String columnType) {
        int index = columnType.indexOf('(');
        if (index != -1) {
            return columnType.substring(0, index).trim();
        }
        return columnType.trim();
    }


    protected boolean isColumnNumeric(ResultSet column) throws Exception {
        int dataType = column.getInt("DATA_TYPE");

        return dataType == Types.INTEGER ||
                dataType == Types.SMALLINT ||
                dataType == Types.TINYINT ||
                dataType == Types.BIGINT ||
                dataType == Types.FLOAT ||
                dataType == Types.REAL ||
                dataType == Types.DOUBLE ||
                dataType == Types.NUMERIC ||
                dataType == Types.DECIMAL;
    }

    protected boolean isColumnNumericWithPrecision(ResultSet column) throws Exception {
        int dataType = column.getInt("DATA_TYPE");
        int columnSize = column.getInt("COLUMN_SIZE");
        int decimalDigits = column.getInt("DECIMAL_DIGITS");

        if ((dataType == Types.NUMERIC || dataType == Types.DECIMAL)
                && columnSize > 0
                && decimalDigits > 0) {
            return true;
        }
        return false;
    }

    protected boolean isColumnText(ResultSet column) throws SQLException {
        int dataType = column.getInt("DATA_TYPE");

        return dataType == Types.CHAR ||
                dataType == Types.VARCHAR ||
                dataType == Types.LONGVARCHAR ||
                dataType == Types.NCHAR ||
                dataType == Types.NVARCHAR ||
                dataType == Types.LONGNVARCHAR;
    }

    private boolean isColumnDate(ResultSet column) throws SQLException {
        int dataType = column.getInt("DATA_TYPE");

        return dataType == Types.DATE ||
                dataType == Types.TIME ||
                dataType == Types.TIMESTAMP ||
                dataType == Types.TIME_WITH_TIMEZONE ||
                dataType == Types.TIMESTAMP_WITH_TIMEZONE;
    }
}
