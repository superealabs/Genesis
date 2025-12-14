package org.labs.genesis.apj.utilitaire;

import org.labs.genesis.apj.component.ApjField;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UtilDBDynamique {

    public static Connection GetConn(File projectJarDir, File libDir) throws Exception {
        var loader = UtilClassLoader.buildLoader(projectJarDir, libDir);
        Thread.currentThread().setContextClassLoader(loader);
        Class<?> utilDBClass;
        try {
            utilDBClass = UtilClassLoader.loadClass(loader, "utilitaire.UtilDB");
        } catch (Exception e) {
            throw new Exception("Failed to load class. Check libDir and jarDir.", e);
        }
        Object utilDB = utilDBClass.getDeclaredConstructor().newInstance();
        Method getConnMethod = utilDBClass.getMethod("GetConn");
        Connection conn = (Connection) getConnMethod.invoke(utilDB);
        if (conn == null || conn.isClosed()) {
            throw new Exception("Connection could not be established.");
        }

        return conn;
    }

    public static Connection GetConn(String projectJarDir, String libDir) throws Exception {
        return GetConn(new File(projectJarDir), new File(libDir));
    }

    public static String[] getTablesOrViews(Connection conn, boolean views) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String dbName = meta.getDatabaseProductName();
        String type = views ? "VIEW" : "TABLE";

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            if ("Oracle".equalsIgnoreCase(dbName)) {
                String sql = views
                        ? "SELECT view_name FROM all_views WHERE owner = USER"
                        : "SELECT table_name FROM all_tables WHERE owner = USER";
                ps = conn.prepareStatement(sql);

            } else if ("PostgreSQL".equalsIgnoreCase(dbName)) {
                String sql = views
                        ? "SELECT table_name FROM information_schema.views WHERE table_schema = 'public'"
                        : "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_type='BASE TABLE'";
                ps = conn.prepareStatement(sql);

            } else {
                String catalog = conn.getCatalog();
                String schema = conn.getSchema();
                rs = meta.getTables(catalog, schema, "%", new String[]{type});
                rs.setFetchSize(100);
            }

            if (ps != null) {
                rs = ps.executeQuery();
            }

            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }

            return result.toArray(new String[0]);

        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }
    }

    public static List<ApjField> getTableColumns(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String dbName = meta.getDatabaseProductName();

        Set<String> primaryKeys = new HashSet<>();
        try (ResultSet pkRs = meta.getPrimaryKeys(null, getSchema(conn, dbName), tableName.toUpperCase())) {
            while (pkRs.next()) {
                primaryKeys.add(pkRs.getString("COLUMN_NAME"));
            }
        }
        List<ApjField> fields = new ArrayList<>();
        try (ResultSet colRs = meta.getColumns(null, getSchema(conn, dbName), tableName.toUpperCase(), null)) {
            while (colRs.next()) {
                String colName = colRs.getString("COLUMN_NAME");
                String typeName = colRs.getString("TYPE_NAME");
                int size = colRs.getInt("COLUMN_SIZE");
                int precision = colRs.getInt("DECIMAL_DIGITS");

                String ddlType;

                if ("Oracle".equalsIgnoreCase(dbName)) {
                    ddlType = oracleColumnType(typeName, size, precision);
                } else if ("PostgreSQL".equalsIgnoreCase(dbName)) {
                    ddlType = postgresColumnType(typeName, size, precision);
                } else {
                    ddlType = defaultColumnType(typeName, size, precision);
                }

                ApjField field = new ApjField();
                field.setNomBase(colName);
                field.setTypeBase(ddlType);
                field.setPrimaryKey(primaryKeys.contains(colName));

                fields.add(field);
            }
        }
        return fields;
    }

    private static String getSchema(Connection conn, String dbName) throws SQLException {
        if ("Oracle".equalsIgnoreCase(dbName)) return conn.getSchema() != null ? conn.getSchema() : "USER";
        if ("PostgreSQL".equalsIgnoreCase(dbName)) return "public";
        return conn.getSchema();
    }

    private static String oracleColumnType(String typeName, int size, int precision) {
        if ("NUMBER".equalsIgnoreCase(typeName)) {
            if (size > 0 && precision > 0) return String.format("NUMBER(%d,%d)", size, precision);
            if (size > 0) return String.format("NUMBER(%d)", size);
            return "NUMBER";
        } else if (typeName.matches("VARCHAR2|CHAR|NVARCHAR2")) {
            return typeName + "(" + size + ")";
        } else if ("DATE".equalsIgnoreCase(typeName)) {
            return "DATE";
        } else {
            return typeName;
        }
    }

    private static String postgresColumnType(String typeName, int size, int precision) {
        if ("NUMERIC".equalsIgnoreCase(typeName) || "DECIMAL".equalsIgnoreCase(typeName)) {
            if (size > 0 && precision > 0) return String.format("NUMERIC(%d,%d)", size, precision);
            if (size > 0) return String.format("NUMERIC(%d)", size);
            return "NUMERIC";
        } else if (typeName.matches("VARCHAR|CHAR|TEXT")) {
            return typeName + "(" + size + ")";
        } else if ("DATE".equalsIgnoreCase(typeName) || "TIMESTAMP".equalsIgnoreCase(typeName)) {
            return typeName.toUpperCase();
        } else {
            return typeName;
        }
    }

    private static String defaultColumnType(String typeName, int size, int precision) {
        if (precision > 0) return String.format("%s(%d,%d)", typeName, size, precision);
        if (size > 0) return String.format("%s(%d)", typeName, size);
        return typeName;
    }
}
