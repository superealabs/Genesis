package org.labs.genesis.apj.utilitaire;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;

public class UtilDBDynamique {

    public static Connection GetConn(File projetJarDir, File libDir) throws Exception {
        File[] jarFiles = libDir.listFiles(f -> f.getName().endsWith(".jar"));
        int urlsLength = (jarFiles != null ? jarFiles.length : 0) + 1;
        URL[] urls = new URL[urlsLength];
        urls[0] = projetJarDir.toURI().toURL();
        if (jarFiles != null) {
            for (int i = 0; i < jarFiles.length; i++) {
                urls[i + 1] = jarFiles[i].toURI().toURL();
            }
        }

        URLClassLoader loader = new URLClassLoader(urls, UtilDBDynamique.class.getClassLoader());
        Thread.currentThread().setContextClassLoader(loader);

        Class<?> utilClass = loader.loadClass("utilitaire.UtilDB");
        Object utilInstance = utilClass.getDeclaredConstructor().newInstance();
        Method getConnMethod = utilClass.getMethod("GetConn");
        return (Connection) getConnMethod.invoke(utilInstance);
    }
}
