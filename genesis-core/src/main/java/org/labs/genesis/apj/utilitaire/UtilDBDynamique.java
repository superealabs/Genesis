package org.labs.genesis.apj.utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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


}
