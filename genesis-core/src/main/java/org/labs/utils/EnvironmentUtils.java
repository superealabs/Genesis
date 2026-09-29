package org.labs.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class EnvironmentUtils {

    public static String getCommand(String command) throws Exception {
        if (isWindows()) {
            return run(null, null, "where", command)
                    .lines()
                    .findFirst()
                    .orElseThrow();
        }

        if (isMacOS()) {
            return run(null, null,
                    "zsh", "-lc", "command -v " + command).trim();
        }

        if (isLinux()) {
            return run(null,null,
                    "bash", "-lc", "command -v " + command).trim();
        }

        return command;
    }

    public static boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return os.contains("win");
    }

    public static boolean isMacOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return os.contains("mac");
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return os.contains("nux") || os.contains("nix");
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


    public static void runWithSudo(String password, String... command) throws IOException, InterruptedException {

        List<String> fullCommand = new ArrayList<>();
        fullCommand.add("sudo");
        fullCommand.add("-S"); // Lit le mot de passe sur stdin
        fullCommand.add("-p");
        fullCommand.add("");   // Réinitialise l'invite de commande sudo
        fullCommand.addAll(Arrays.asList(command));

        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);

        // Empêche les paquets Debian/Ubuntu de bloquer sur une invite interactive
        processBuilder.environment().put("DEBIAN_FRONTEND", "noninteractive");
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Envoi du mot de passe dans stdin
        try (OutputStream outputStream = process.getOutputStream()) {
            outputStream.write((password + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }

        // LECTURE IMPÉRATIVE DE LA SORTIE pour éviter le blocage du buffer système
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            // Détection explicite de mot de passe sudo incorrect
            if (output.contains("incorrect password") || output.contains("mot de passe incorrect") || output.contains("Sorry, try again")) {
                throw new RuntimeException("Mot de passe sudo incorrect.");
            }

            throw new RuntimeException(
                    "Échec de la commande : sudo " + String.join(" ", command)
                            + "\nCode de sortie : " + exitCode
                            + "\nDétails : " + output.trim()
            );
        }
    }

    public static boolean commandExists(String command) {
        try {
            run(null, null,getCommand(command), "--version");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String run(String path,
                           Map<String, String> environments, String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        if(path != null)
            processBuilder.directory(Paths.get(path)
                    .toFile())
                    .inheritIO();
        if(environments != null) processBuilder.environment().putAll(environments);

        Process process = processBuilder.start();

        // Lecture du flux pour éviter le blocage
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Échec de la commande : " + String.join(" ", command)
                            + "\nDétails : " + output.trim()
            );
        }

        return output;
    }
}
