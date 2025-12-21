package org.labs.genesis.apj.utilitaire;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.component.ApjField;
import org.labs.utils.StringUtils;
import java.sql.*;
import java.util.*;

import static org.labs.genesis.apj.generator.ApjFileGenerator.databases;

@Setter
@Getter
public class Database {
    private int id;
    private String name;
    private Map<String, String> types;

    public static List<ApjField> getTableColumns(Connection conn, String tableName) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        String dbProduct = meta.getDatabaseProductName();
        int dbId = dbProduct.equalsIgnoreCase("PostgreSQL") ? 2 : 1;

        Database database = databases.get(dbId);

        String schema = dbProduct.equalsIgnoreCase("PostgreSQL") ? "public" : getCurrentOracleSchema(conn);
        String tableNameUc = tableName.toUpperCase();
        String schemaUc = schema != null ? schema.toUpperCase() : null;

        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet pkRs = meta.getPrimaryKeys(null, schemaUc, tableNameUc)) {
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME").toUpperCase());
            }
        }

        Map<String, ApjField> fieldsMap = new LinkedHashMap<>();
        try (ResultSet rs = meta.getColumns(null, schemaUc, tableNameUc, "%")) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toUpperCase();
                if (fieldsMap.containsKey(columnName)) continue;

                String typeName = rs.getString("TYPE_NAME");
                int size = rs.getInt("COLUMN_SIZE");
                int scale = rs.getInt("DECIMAL_DIGITS");

                String typeBase = scale > 0 ? typeName + "(" + size + "," + scale + ")" :
                        size > 0 ? typeName + "(" + size + ")" :
                                typeName;

                String javaType = database.getTypes().getOrDefault(StringUtils.normalizeDbType(typeBase), "Object");

                ApjField field = new ApjField();
                field.setNomBase(columnName);
                field.setNom(columnName.toLowerCase());
                field.setTypeBase(typeBase);
                field.setType(javaType);
                field.setPrimaryKey(primaryKeys.contains(columnName));

                fieldsMap.put(columnName, field);
            }
        }

        return new ArrayList<>(fieldsMap.values());
    }

    private static String getCurrentOracleSchema(Connection conn) throws SQLException {
        String sql = "SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') AS CURRENT_SCHEMA FROM DUAL";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("CURRENT_SCHEMA");
            }
        }
        throw new IllegalStateException("Impossible de déterminer le schéma Oracle courant");
    }

}
