package org.labs.utils;

import java.io.IOException;
import java.util.Locale;

public class DockerInstallerUtils {

    public static void installDocker(String password)
            throws IOException, InterruptedException {

        // Docker déjà installé
        if (GitInstallerUtils.commandExists("docker")) {
            return;
        }

        String os = System.getProperty("os.name")
                .toLowerCase(Locale.ROOT);

        if (os.contains("win")) {
            installWindows();

        } else if (os.contains("mac")) {
            installMacOS();

        } else if (os.contains("nux")) {
            installLinux(password);

        } else {
            throw new UnsupportedOperationException(
                    "OS non supporté : "
                            + System.getProperty("os.name")
            );
        }
        if (!GitInstallerUtils.commandExists("docker")) {
            throw new RuntimeException(
                    "Git semble avoir été installé, mais la commande 'git' reste introuvable."
            );
        }
    }

    /**
     * Installation de Docker Desktop sous Windows.
     */
    private static void installWindows()
            throws IOException, InterruptedException {

        GitInstallerUtils.run(
                "winget",
                "install",
                "--id", "Docker.DockerDesktop",
                "-e",
                "--source", "winget",
                "--accept-source-agreements",
                "--accept-package-agreements"
        );
    }

    /**
     * Installation de Docker Desktop sous macOS.
     * Nécessite Homebrew.
     */
    private static void installMacOS()
            throws IOException, InterruptedException {

        if (!GitInstallerUtils.commandExists("brew")) {
            throw new IllegalStateException(
                    "Homebrew n'est pas installé."
            );
        }

        GitInstallerUtils.run(
                "brew",
                "install",
                "--cask",
                "docker"
        );
    }

    /**
     * Installation de Docker Engine sous Linux.
     */
    private static void installLinux(String password)
            throws IOException, InterruptedException {

        if (GitInstallerUtils.commandExists("apt-get")) {
            installDebianUbuntu(password);

        } else if (GitInstallerUtils.commandExists("dnf")) {
            installFedora(password);

        } else if (GitInstallerUtils.commandExists("yum")) {
            installRhel(password);

        } else if (GitInstallerUtils.commandExists("pacman")) {
            installArch(password);

        } else {
            throw new UnsupportedOperationException(
                    "Gestionnaire de paquets Linux non supporté."
            );
        }
        // Vérification après installation
        if (!GitInstallerUtils.commandExists("docker")) {
            throw new RuntimeException(
                    "Git semble avoir été installé, mais la commande 'git' reste introuvable."
            );
        }
    }

    /**
     * Debian / Ubuntu.
     */
    private static void installDebianUbuntu(String password)
            throws IOException, InterruptedException {

        GitInstallerUtils.runWithSudo(
                password,
                "apt-get",
                "update"
        );

        GitInstallerUtils.runWithSudo(
                password,
                "apt-get",
                "install",
                "-y",
                "docker.io",
                "docker-compose-plugin"
        );

        GitInstallerUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    /**
     * Fedora.
     */
    private static void installFedora(String password)
            throws IOException, InterruptedException {

        GitInstallerUtils.runWithSudo(
                password,
                "dnf",
                "install",
                "-y",
                "docker",
                "docker-compose-plugin"
        );

        GitInstallerUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    /**
     * RHEL / CentOS / distributions utilisant yum.
     */
    private static void installRhel(String password)
            throws IOException, InterruptedException {

        GitInstallerUtils.runWithSudo(
                password,
                "yum",
                "install",
                "-y",
                "docker"
        );

        GitInstallerUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    /**
     * Arch Linux.
     */
    private static void installArch(String password)
            throws IOException, InterruptedException {

        GitInstallerUtils.runWithSudo(
                password,
                "pacman",
                "-S",
                "--noconfirm",
                "docker",
                "docker-compose"
        );

        GitInstallerUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }
}