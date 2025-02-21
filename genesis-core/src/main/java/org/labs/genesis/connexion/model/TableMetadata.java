package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.Constantes;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.utils.FileUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.labs.utils.FileUtils.toCamelCase;

@Setter
@Getter
@NoArgsConstructor
@ToString
public class TableMetadata {
    private Database database;
    private String tableName;
    private ColumnMetadata[] columns;
    private ColumnMetadata primaryColumn;
    private String className;

    public void initialize(Connection connex, Credentials credentials, Database database, Language language) throws SQLException, ClassNotFoundException {
        boolean opened = false;
        Connection connect = connex;

        if (connect == null || connect.isClosed()) {
            connect = database.getConnection(credentials);
            opened = true;
        }

        try {
            DatabaseMetaData metaData = connect.getMetaData();

            String driverName = metaData.getDriverName();
            String driverVersion = metaData.getDriverVersion();

            database.setDriverName(driverName);
            database.setDriverVersion(driverVersion);

            setDatabase(database);

            String tableName = getTableName();

            List<ColumnMetadata> listeCols = fetchColumns(metaData, tableName, language, database);
            fetchPrimaryKeys(metaData, tableName, listeCols);
            fetchForeignKeys(metaData, tableName, language, listeCols);

            setClassName(
                    Stream.of(tableName)
                            .map(String::toLowerCase)
                            .map(FileUtils::toCamelCase)
                            .map(FileUtils::majStart)
                            .map(FileUtils::removeLastS)
                            .findFirst()
                            .orElse("")
            );
            setColumns(listeCols.toArray(new ColumnMetadata[0]));

        } finally {
            if (opened && !connect.isClosed()) {
                connect.close();
            }
        }
    }

    public List<String> getAllTableNames(Database database, Connection connection) throws SQLException {
        return database.getAllTableNames(connection);
    }

    public List<String> getAllViewNames(Database database, Connection connection) throws SQLException {
        return database.getAllViewNames(connection);
    }

    public List<TableMetadata> initializeTableType(List<String> tableTypeNames, Connection connex, Credentials credentials, Database database, Language language, boolean isView) throws SQLException, ClassNotFoundException {
        List<TableMetadata> tableMetadataList = new ArrayList<>();
        boolean opened = false;
        Connection connect = connex;

        if (connect == null || connect.isClosed()) {
            connect = database.getConnection(credentials);
            opened = true;
        }

        try {
            if (tableTypeNames == null || tableTypeNames.isEmpty()) {
                if (isView) {
                    tableTypeNames = getAllViewNames(database, connect);
                } else {
                    tableTypeNames = getAllTableNames(database, connect);
                }
            }

            for (String tableTypeName : tableTypeNames) {
                TableMetadata tableMetadata = new TableMetadata();
                tableMetadata.setTableName(tableTypeName);
                tableMetadata.initialize(connect, credentials, database, language);
                tableMetadataList.add(tableMetadata);
            }
        } finally {
            if (opened && !connect.isClosed()) {
                connect.close();
            }
        }

        return tableMetadataList;
    }



    public List<TableMetadata> initializeTables(List<String> tableNames, Connection connex, Credentials credentials, Database database, Language language) throws SQLException, ClassNotFoundException {
       return initializeTableType(tableNames, connex, credentials, database, language, false);
    }

    public List<TableMetadata> initializeViews(List<String> viewNames, Connection connex, Credentials credentials, Database database, Language language) throws SQLException, ClassNotFoundException {
      return initializeTableType(viewNames, connex, credentials, database, language, true);
    }


    private List<ColumnMetadata> fetchColumns(DatabaseMetaData metaData, String tableName, Language language, Database database) throws SQLException {
        List<ColumnMetadata> listeCols = new ArrayList<>();
        database.getDriverType().equals("Oracle");
        try (ResultSet columns = metaData.getColumns(null, database.getCredentials().getSchemaName(), tableName, null)) {
            while (columns.next()) {
                ColumnMetadata column = new ColumnMetadata();
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");

                column.setName(toCamelCase(columnName.toLowerCase()));
                column.setReferencedColumn(columnName);

//                if (language.getTypes().get(database.getTypes().get(columnType)) == null)
                if (language.getTypes().get(getDatabaseType(database,columns)) == null)
                    throw new RuntimeException("Database type not supported yet : " + columnType);
                else
                    column.setType(language.getTypes().get(getDatabaseType(database,columns)));
//                    column.setType(language.getTypes().get(database.getTypes().get(columnType)));

                column.setColumnType(columnType);
                listeCols.add(column);
            }
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
        return listeCols;
    }
    
    private String getDatabaseType(Database database, ResultSet columns) throws Exception {
        String columnType = columns.getString("TYPE_NAME");

        if (columns.getInt("DATA_TYPE") == Types.NUMERIC && database.getId() == Constantes.Oracle_ID) {
            if (columns.getInt("DECIMAL_DIGITS") > 0){
                columnType = getBeforeBracketsSimple(columnType)+"(*,*)";
            }else {
                columnType = getBeforeBracketsSimple(columnType);
            }
        }
        if (columns.getInt("DATA_TYPE") == Types.TIMESTAMP && database.getId() == Constantes.Oracle_ID) {
            columnType = getBeforeBracketsSimple(columnType);
        }
        return database.getTypes().get(columnType);
    }

    private String getBeforeBracketsSimple(String columnType) {
        int index = columnType.indexOf('(');
        if (index != -1) {
            return columnType.substring(0, index).trim();
        }
        return columnType.trim();
    }

    private void fetchPrimaryKeys(DatabaseMetaData metaData, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, database.getCredentials().getSchemaName(), tableName)) {
            while (primaryKeys.next()) {
                String pkColumnName = primaryKeys.getString("COLUMN_NAME");

                for (ColumnMetadata column : columns) {
                    if (column.getReferencedColumn().equalsIgnoreCase(pkColumnName)) {
                        column.setPrimary(true);
                        setPrimaryColumn(column);
                        break;
                    }
                }
            }
        }
    }

    private void fetchForeignKeys(DatabaseMetaData metaData, String tableName, Language language, List<ColumnMetadata> listeCols) throws SQLException {
        try (ResultSet foreignKeys = metaData.getImportedKeys(null, database.getCredentials().getSchemaName(), tableName)) {

            while (foreignKeys.next()) {
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                for (ColumnMetadata field : listeCols) {
                    if (field.getReferencedColumn().equalsIgnoreCase(fkColumnName)) {
                        field.setForeign(true);

                        field.setReferencedColumn(field.getReferencedColumn());
                        field.setReferencedColumnType(field.getReferencedColumnType());
                        field.setColumnType(toCamelCase(field.getType()));
                        field.setName(
                                field.getName()
                                        .transform(FileUtils::toCamelCase)
                                        .transform(name -> name + FileUtils.majStart(FileUtils.toCamelCase(pkTableName.toLowerCase())))
                        );
                        field.setReferencedTable(pkTableName.transform(FileUtils::toCamelCase));

                        // Récupérer le type de la colonne référencée
                        try (ResultSet pkColumn = metaData.getColumns(null, database.getCredentials().getSchemaName(), pkTableName, pkColumnName)) {
                            if (pkColumn.next()) {
                                String pkColumnType = pkColumn.getString("TYPE_NAME");
                                field.setReferencedColumnType(language.getTypes().get(database.getTypes().get(pkColumnType)));
                            }
                        }

                        field.setType(pkTableName
                                .transform(FileUtils::toCamelCase)
                                .transform(FileUtils::removeLastS)
                                .transform(FileUtils::majStart)
                        );

                    }
                }
            }
        }
    }

    private void printColumnsInfo(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet columns = metaData.getColumns(null, database.getCredentials().getSchemaName(), tableName, null);
        System.out.println("columns:");

        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            int columnType = columns.getInt("DATA_TYPE");
            String columnTypeName = columns.getString("TYPE_NAME");
            int columnSize = columns.getInt("COLUMN_SIZE");
            boolean nullable = columns.getBoolean("NULLABLE");

            String dataTypeName = JDBCType.valueOf(columnType).getName();
            System.out.println("\t" + columnName + " (" + dataTypeName + "), Size: " + columnSize + ", Nullable: " + nullable + "Columname type: " + columnTypeName);
        }
    }

    private void printPrimaryKeys(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet primaryKeys = metaData.getPrimaryKeys(null, database.getCredentials().getSchemaName(), tableName);
        System.out.println("Primary Keys:");
        while (primaryKeys.next()) {
            String pkColumnName = primaryKeys.getString("COLUMN_NAME");
            System.out.println("\t" + pkColumnName);
        }
    }

    private void printForeignKeys(DatabaseMetaData metaData, String tableName) throws SQLException {
        ResultSet foreignKeys = metaData.getImportedKeys(null, database.getCredentials().getSchemaName(), tableName);
        System.out.println("Foreign Keys:");
        while (foreignKeys.next()) {
            String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
            String fkName = foreignKeys.getString("FK_NAME");
            String pkTableName = foreignKeys.getString("PKTABLE_NAME");
            String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
            System.out.println("\t" + fkColumnName + " -> " + pkTableName + "." + pkColumnName + " (" + fkName + ")");
        }
    }

    public void getMetaData(Credentials credentials, Database database) {
        try (Connection connection = database.getConnection(credentials)) {
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet tables = metaData.getTables(null, database.getCredentials().getSchemaName(), "%", new String[]{"TABLE"});

            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("Table Name: " + tableName);

                printColumnsInfo(metaData, tableName);
                printPrimaryKeys(metaData, tableName);
                printForeignKeys(metaData, tableName);

                System.out.println();
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException("Error while accessing database metadata: ", e);
        }
    }

}
