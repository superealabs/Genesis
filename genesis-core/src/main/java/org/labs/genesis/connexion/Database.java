package org.labs.genesis.connexion;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.generator.framework.FrameworkMetadataProvider;
import org.labs.genesis.connexion.model.ColumnMetadata;
import org.labs.genesis.connexion.model.TableMetadata;
import org.labs.genesis.engine.GenesisTemplateEngine;
import org.labs.utils.FileUtils;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import static org.labs.utils.StringUtils.toCamelCase;
import java.util.*;

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
    private ConstraintQueries constraintQueries;
    protected static final GenesisTemplateEngine engine = new GenesisTemplateEngine();

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

    public TableMetadata getEntity(Connection connection, Credentials credentials, String entityName, Language language, Framework framework) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        tableMetadata.setTableName(entityName);
        tableMetadata.initialize(connection, credentials, this, language,framework);
        return tableMetadata;
    }

    public List<TableMetadata> getEntities(Connection connection, Credentials credentials, Language language,Framework framework) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        return tableMetadata.initializeTables(null, connection, credentials, this, language,framework);
    }

    public List<TableMetadata> getViews(Connection connection, Credentials credentials, Language language,Framework framework) throws SQLException, ClassNotFoundException {
        TableMetadata tableMetadata = new TableMetadata();
        return tableMetadata.initializeViews(null, connection, credentials, this, language,framework);
    }

    public List<TableMetadata> getEntitiesByNames(List<String> entityNames, Connection connection, Credentials credentials, Language language, Framework framework) throws SQLException, ClassNotFoundException {
        if (entityNames.isEmpty())
            return getEntities(connection, credentials, language, framework);

        List<TableMetadata> tableMetadataList = new ArrayList<>();
        for (String entityName : entityNames) {
            tableMetadataList.add(getEntity(connection, credentials, entityName, language, framework));
        }
        return tableMetadataList;
    }

    public List<TableMetadata> getViewsByNames(List<String> viewNames, Connection connection, Credentials credentials, Language language, Framework framework) throws SQLException, ClassNotFoundException {
        if (viewNames.isEmpty())
            return getViews(connection, credentials, language, framework);

        List<TableMetadata> tableMetadataList = new ArrayList<>();
        for (String viewName : viewNames) {
            TableMetadata viewEntity = getEntity(connection, credentials, viewName, language, framework);
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

    public void setConstraintQueries() throws IOException {
        this.constraintQueries = Arrays.stream(FileUtils.fromYaml(ConstraintQueries[].class, Constantes.CONSTRAINT_QUERIES_YAML))
                .filter(q -> q.getDatabaseId() == this.id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No ConstraintQueries found for id : " + this.id));
    }

    public List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language,Connection connex,Framework framework) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();
        try (ResultSet columns = metaData.getColumns(null, this.getCredentials().getSchemaName(), tableName, null)) {
            Map<String, Object> frameworkValidationAnnotations = framework.getModel().getValidationAnnotations();
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
                column.setNumeric(isColumnNumeric);
                column.setNumericWithPrecision(isColumnNumericWithPrecision);
                column.setText(isColumnText);
                column.setDate(isColumnDate);

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

    protected void checkUnique(DatabaseMetaData metaData, String tableName, List<ColumnMetadata> listeCols, Framework framework) throws SQLException {
        try (ResultSet indexes = metaData.getIndexInfo(null, this.getCredentials().getSchemaName(), tableName, false, true)) {
            while (indexes.next()) {
                String columnNameInIndex = indexes.getString("COLUMN_NAME");
                boolean isUnique = !indexes.getBoolean("NON_UNIQUE");

                for(ColumnMetadata col : listeCols){
                    if (col.getName().equals(columnNameInIndex)) {
                        col.setUnique(isUnique);
                        break;
                    }
                }

            }
        }
    }

    protected void checkStrictMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkMinConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkStrictMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkMaxConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}

    protected void checkStrictPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkPastDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkStrictFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkFutureDateConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}

    protected void checkNotBlankConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkMinLengthConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}
    protected void checkRegexConstraint(Connection connex, String tableName, List<ColumnMetadata> columns,Framework framework) throws Exception {}

    protected void removeUnusedData(List<ColumnMetadata> listeCols){
        for(ColumnMetadata col : listeCols){
            Map<String, Object> map = col.getValidationAnnotations(); // ton map initial

            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                if (key.toLowerCase().contains("data")) {
                    it.remove();
                }
            }
        }
    }
}
