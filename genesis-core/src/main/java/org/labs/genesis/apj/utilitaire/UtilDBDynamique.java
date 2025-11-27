package org.labs.genesis.apj.utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;

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

}
