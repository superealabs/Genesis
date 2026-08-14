package org.labs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GitInstallerUtils {

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

    public static void installGit(String password) throws IOException, InterruptedException {

        if (commandExists("git")) {
            return;
        }

        if (isWindows()) {
            installWindows();
        } else if (isMacOS()) {
            installMacOS();
        } else if (isLinux()) {
            installLinux(password);
        } else {
            throw new UnsupportedOperationException(
                    "OS non supporté : " + System.getProperty("os.name")
            );
        }

        // Vérification après installation
        if (!commandExists("git")) {
            throw new RuntimeException(
                    "Git semble avoir été installé, mais la commande 'git' reste introuvable."
            );
        }
    }

    private static void installWindows() throws IOException, InterruptedException {
        run(
                "winget",
                "install",
                "--id", "Git.Git",
                "-e",
                "--source", "winget",
                "--accept-source-agreements",
                "--accept-package-agreements"
        );
    }

    private static void installMacOS() throws IOException, InterruptedException {
        if (!commandExists("brew")) {
            throw new IllegalStateException("Homebrew n'est pas installé.");
        }
        run("brew", "install", "git");
    }

    private static void installLinux(String password) throws IOException, InterruptedException {
        if (commandExists("apt-get")) {

            // apt-get update peut échouer (code 1) si un seul dépôt PPA tiers est expiré.
            // On tente l'update, mais on autorise la suite du script si ce n'est pas critique.
            try {
                runWithSudo(password, "apt-get", "update");
            } catch (Exception e) {
                System.err.println("Avertissement lors de apt-get update : " + e.getMessage());
            }

            runWithSudo(
                    password,
                    "apt-get",
                    "install",
                    "-y",
                    "git"
            );

        } else if (commandExists("dnf")) {

            runWithSudo(
                    password,
                    "dnf",
                    "install",
                    "-y",
                    "git"
            );

        } else if (commandExists("yum")) {

            runWithSudo(
                    password,
                    "yum",
                    "install",
                    "-y",
                    "git"
            );

        } else if (commandExists("pacman")) {

            runWithSudo(
                    password,
                    "pacman",
                    "-S",
                    "--noconfirm",
                    "git"
            );

        } else {

            throw new UnsupportedOperationException(
                    "Gestionnaire de paquets Linux non supporté."
            );
        }
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
            ProcessBuilder processBuilder = new ProcessBuilder(command, "--version");
            if (isWindows()) {
                String freshPath = EnvironmentUtils.getFreshWindowsPath();
                processBuilder.environment().put("PATH", freshPath);
            }
            Process process = processBuilder.redirectErrorStream(true).start();

            // Vider le flux pour éviter tout blocage éventuel
            process.getInputStream().readAllBytes();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    public static void run(String... command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

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
    }
}