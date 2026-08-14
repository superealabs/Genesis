package org.labs.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class EnvironmentUtils {

    public static String getFreshWindowsPath() {
        String systemPath = getRegistryValue("HKLM\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment", "Path");
        String userPath = getRegistryValue("HKCU\\Environment", "Path");

        return userPath + ";" + systemPath;
    }

    private static String getRegistryValue(String key, String value) {
        try {
            Process process = new ProcessBuilder("reg", "query", key, "/v", value).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(value)) {
                        String[] tokens = line.split("REG_SZ|REG_EXPAND_SZ");
                        if (tokens.length > 1) {
                            return tokens[1].trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return "";
    }
}
