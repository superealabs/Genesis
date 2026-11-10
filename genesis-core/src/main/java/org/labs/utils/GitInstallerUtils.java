package org.labs.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GitInstallerUtils {

    public static void installGit(String password) throws IOException, InterruptedException {

        if (EnvironmentUtils.commandExists("git")) {
            return;
        }

        if (EnvironmentUtils.isWindows()) {
            installWindows();
        } else if (EnvironmentUtils.isMacOS()) {
            installMacOS();
        } else if (EnvironmentUtils.isLinux()) {
            installLinux(password);
        } else {
            throw new UnsupportedOperationException(
                    "OS non supporté : " + System.getProperty("os.name")
            );
        }

        // Vérification après installation
        if (!EnvironmentUtils.commandExists("git")) {
            throw new RuntimeException(
                    "Git semble avoir été installé, mais la commande 'git' reste introuvable."
            );
        }
    }

    private static void installWindows() throws IOException, InterruptedException {
        EnvironmentUtils.run(
                null,
                null,
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
        if (!EnvironmentUtils.commandExists("brew")) {
            throw new IllegalStateException("Homebrew n'est pas installé.");
        }
        EnvironmentUtils.run(null, null, "brew", "install", "git");
    }

    private static void installLinux(String password) throws IOException, InterruptedException {
        if (EnvironmentUtils.commandExists("apt-get")) {

            // apt-get update peut échouer (code 1) si un seul dépôt PPA tiers est expiré.
            // On tente l'update, mais on autorise la suite du script si ce n'est pas critique.
            try {
                EnvironmentUtils.runWithSudo(password, "apt-get", "update");
            } catch (Exception e) {
                System.err.println("Avertissement lors de apt-get update : " + e.getMessage());
            }

            EnvironmentUtils.runWithSudo(
                    password,
                    "apt-get",
                    "install",
                    "-y",
                    "git"
            );

        } else if (EnvironmentUtils.commandExists("dnf")) {

            EnvironmentUtils.runWithSudo(
                    password,
                    "dnf",
                    "install",
                    "-y",
                    "git"
            );

        } else if (EnvironmentUtils.commandExists("yum")) {

            EnvironmentUtils.runWithSudo(
                    password,
                    "yum",
                    "install",
                    "-y",
                    "git"
            );

        } else if (EnvironmentUtils.commandExists("pacman")) {

            EnvironmentUtils.runWithSudo(
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
}