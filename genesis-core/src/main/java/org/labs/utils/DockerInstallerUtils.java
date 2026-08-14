package org.labs.utils;

import java.io.IOException;
import java.util.Locale;

public class DockerInstallerUtils {

    public static void installDocker(String password)
            throws IOException, InterruptedException {

        // Docker déjà installé
        if (commandExists("docker")) {
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
    }

    /**
     * Installation de Docker Desktop sous Windows.
     */
    private static void installWindows()
            throws IOException, InterruptedException {

        run(
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

        if (!commandExists("brew")) {
            throw new IllegalStateException(
                    "Homebrew n'est pas installé."
            );
        }

        run(
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

        if (commandExists("apt-get")) {

            installDebianUbuntu(password);

        } else if (commandExists("dnf")) {

            installFedora(password);

        } else if (commandExists("yum")) {

            installRhel(password);

        } else if (commandExists("pacman")) {

            installArch(password);

        } else {
            throw new UnsupportedOperationException(
                    "Gestionnaire de paquets Linux non supporté."
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

    private static boolean commandExists(String command) {

        try {
            Process process = new ProcessBuilder(
                    command,
                    "--version"
            )
                    .redirectErrorStream(true)
                    .start();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    private static void run(String... command)
            throws IOException, InterruptedException {

        Process process = new ProcessBuilder(command)
                .inheritIO()
                .start();

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException(
                    "Échec de la commande : "
                            + String.join(" ", command)
            );
        }
    }

    public static void main(String[] args) throws Exception {

        String password = args.length > 0
                ? args[0]
                : null;

        installDocker(password);
    }
}