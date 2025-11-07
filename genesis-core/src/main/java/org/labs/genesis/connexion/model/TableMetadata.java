package org.labs.genesis.connexion.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.connexion.Credentials;
import org.labs.genesis.connexion.Database;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.utils.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;



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
    private Boolean isView;
    private Boolean hasFk = false;

    // relation mere fille
    private Boolean isParent = false;
    private Boolean isChild = false;
    private List<ChildTableMetadata> childTables = new ArrayList<>();
    private List<TableMetadata> parentTables = new ArrayList<>();

    public void setColumnsFrontendTypes(FrontendLanguage frontendLanguage,Database database)
    {
        for(ColumnMetadata column : columns)
        {
            column.setFrontEndType(frontendLanguage,database);
            column.setFrontEndReferencedColumnType(frontendLanguage,database);
        }
    }

    public void initialize(Connection connex, Credentials credentials, Database database, Language language, Framework framework) throws SQLException, ClassNotFoundException {
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
            //Initialise l'entité en le considérant comme table
            setIsView(false);

            List<ColumnMetadata> listeCols = database.fetchColumns(metaData, tableName, language,connect,framework);
            fetchPrimaryKeys(metaData, tableName, listeCols);
            fetchForeignKeys(metaData, tableName, language, listeCols);

            setClassName(
                    Stream.of(tableName)
                            .map(String::toLowerCase)
                            .map(StringUtils::toCamelCase)
                            .map(StringUtils::majStart)
                            .map(StringUtils::removeLastS)
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

    public List<TableMetadata> initializeTableType(List<String> tableTypeNames, Connection connex, Credentials credentials, Database database, Language language, boolean isView,Framework framework) throws SQLException, ClassNotFoundException {
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
                tableMetadata.initialize(connect, credentials, database, language, framework);
                tableMetadata.setIsView(isView);
                tableMetadata.setPKForView();
                tableMetadataList.add(tableMetadata);
            }
        } finally {
            if (opened && !connect.isClosed()) {
                connect.close();
            }
        }

        return tableMetadataList;
    }


    public List<TableMetadata> initializeTables(List<String> tableNames, Connection connex, Credentials credentials, Database database, Language language,Framework framework) throws SQLException, ClassNotFoundException {
        return initializeTableType(tableNames, connex, credentials, database, language, false,framework);
    }

    public List<TableMetadata> initializeViews(List<String> viewNames, Connection connex, Credentials credentials, Database database, Language language,Framework framework) throws SQLException, ClassNotFoundException {
        return initializeTableType(viewNames, connex, credentials, database, language, true,framework);
    }

    private void fetchPrimaryKeys(DatabaseMetaData metaData, String tableName, List<ColumnMetadata> columns) throws SQLException {
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null,  (database.getName().equals("Oracle")) ?database.getCredentials().getUser():database.getCredentials().getSchemaName(), tableName)) {
            while (primaryKeys.next()) {
                String pkColumnName = primaryKeys.getString("COLUMN_NAME");

                for (ColumnMetadata column : columns) {
                    if (column.getReferencedColumn().equalsIgnoreCase(pkColumnName)) {
                        column.setPrimary(true);
                        column.getValidationAnnotations().remove("notNull");
                        setPrimaryColumn(column);
                        break;
                    }
                }
            }
        }
    }

    public void setPKForView()  {
        if(!isView) return;

        for (ColumnMetadata column : columns) {
            if (column.getReferencedColumn().equalsIgnoreCase("id")) {
                column.setPrimary(true);
                setPrimaryColumn(column);
                return;
            }
        }
        if(columns.length>0){
            columns[0].setPrimary(true);
            setPrimaryColumn(columns[0]);
        }
    }

    private void fetchForeignKeys(DatabaseMetaData metaData, String tableName, Language language, List<ColumnMetadata> listeCols) throws SQLException {
        try (ResultSet foreignKeys = metaData.getImportedKeys(null, (database.getName().equals("Oracle")) ?database.getCredentials().getUser():database.getCredentials().getSchemaName(), tableName)) {

            while (foreignKeys.next()) {
                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                for (ColumnMetadata field : listeCols) {
                    if (field.getReferencedColumn().equalsIgnoreCase(fkColumnName)) {
                        setHasFk(true);
                        field.setForeign(true);

                        field.setReferencedColumn(field.getReferencedColumn());
                        field.setReferencedColumnType(field.getReferencedColumnType());
                        field.setReferencedPrimaryKeyColumn((pkColumnName.toLowerCase()).transform(StringUtils::toCamelCase));
                        field.setName(
                                field.getName()
                                        .transform(StringUtils::toCamelCase)
                                        .transform(name -> name + (StringUtils.majStart(StringUtils.toCamelCase(pkTableName.toLowerCase())))
                                        ));
                        field.setReferencedTable(pkTableName.transform(StringUtils::toCamelCase));

                        try (ResultSet pkColumn = metaData.getColumns(null, (database.getName().equals("Oracle")) ?database.getCredentials().getUser():database.getCredentials().getSchemaName(), pkTableName, pkColumnName)) {
                            if (pkColumn.next()) {
                                String pkColumnType = pkColumn.getString("TYPE_NAME");
                                field.setDatabaseColumnType(pkColumnType);
                                field.setReferencedColumnType(language.getTypes().get(database.getTypes().get(pkColumnType)));
                            }
                        }

                        field.setType(Stream.of(pkTableName)
                                .map(String::toLowerCase)
                                .map(StringUtils::toCamelCase)
                                .map(StringUtils::majStart)
                                .map(StringUtils::removeLastS)
                                .findFirst()
                                .orElse("")
                        );

                    }
                }
            }
        }
    }

    public List<ColumnMetadata> getForeignKeysColumns() {
        List<ColumnMetadata> foreignKeys = new ArrayList<>();
        for (ColumnMetadata column : columns) {
            if (column.isForeign()) {
                foreignKeys.add(column);
            }
        }
        return foreignKeys;
    }

    public ColumnMetadata findForeingKeyColumnByClassName(String className) {
        for (ColumnMetadata column : columns) {
            if (column.isForeign() && column.getType().equalsIgnoreCase(className)) {
                return column;
            }
        }
        return null;
    }

    public void addChild(TableMetadata child, Boolean mandatory){
        if (childTables == null) { setChildTables(new ArrayList<>());}
        if (child == null)  return;
        if (childTables.contains(child)) {
            return;
        }
        ColumnMetadata fkColumn = child.findForeingKeyColumnByClassName(this.getClassName());
        fkColumn.setIsParentForeignKey(true);
        this.childTables.add(new ChildTableMetadata(child, mandatory, fkColumn));
        this.setIsParent(true);
    }

    public void setParentTable(TableMetadata parentTable) {
        if (parentTables == null) { setParentTables(new ArrayList<>());}
        this.parentTables.add(parentTable);
        this.setIsChild(true);
    }
}
