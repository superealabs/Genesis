package org.labs.genesis.apj.utilitaire;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilClassLoader {

    public static URLClassLoader buildLoader(String projectJarDir, String libDir) throws Exception {
        return buildLoader(new File(projectJarDir), new File(libDir));
    }

    public static URLClassLoader buildLoader(File projectJarDir, File libDir) throws Exception {
        File[] jarFiles = libDir.listFiles(f -> f.getName().endsWith(".jar"));
        int len = (jarFiles != null ? jarFiles.length : 0) + 1;

        URL[] urls = new URL[len];
        urls[0] = projectJarDir.toURI().toURL();

        if (jarFiles != null) {
            for (int i = 0; i < jarFiles.length; i++) {
                urls[i + 1] = jarFiles[i].toURI().toURL();
            }
        }
        return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    }

    public static Class<?> loadClass(URLClassLoader loader, String className) throws Exception {
        return loader.loadClass(className);
    }

    public static List<Field> listFields(Class<?> cls, String stopSuperclassName) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = cls;

        while (current != null && !current.getSimpleName().equals(stopSuperclassName)) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }

        return fields;
    }

    public static void closeLoader(URLClassLoader loader) {
        try {
            loader.close();
        } catch (Exception ignored) {
        }
    }
}
