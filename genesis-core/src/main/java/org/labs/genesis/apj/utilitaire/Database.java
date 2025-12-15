package org.labs.genesis.apj.utilitaire;

import lombok.Getter;
import lombok.Setter;
import org.labs.genesis.apj.component.ApjField;
import org.labs.utils.FileUtils;
import org.labs.utils.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

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

        Map<Integer, Database> databases =
                Arrays.stream(FileUtils.fromJson(Database[].class, ConstantesApj.DATABASE_JSON))
                        .collect(Collectors.toMap(Database::getId, d -> d));

        Database database = databases.get(dbId);

        String schema = dbProduct.equalsIgnoreCase("PostgreSQL") ? "public" : null;
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

                String typeBase;
                if (scale > 0) typeBase = typeName + "(" + size + "," + scale + ")";
                else if (size > 0) typeBase = typeName + "(" + size + ")";
                else typeBase = typeName;

                String normalizedDbType = StringUtils.normalizeDbType(typeBase);
                String javaType = database.getTypes().get(normalizedDbType);

                ApjField field = new ApjField();
                field.setNomBase(columnName);
                field.setNom(columnName.toLowerCase());
                field.setTypeBase(typeBase);
                field.setType(javaType != null ? javaType : "Object");
                field.setPrimaryKey(primaryKeys.contains(columnName));

                fieldsMap.put(columnName, field);
            }
        }

        return new ArrayList<>(fieldsMap.values());
    }


}
