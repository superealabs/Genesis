package org.labs.utils;

import java.io.IOException;

public class DockerInstallerUtils {

    // =========================================================
    // PUBLIC API
    // =========================================================

    /**
     * Installe Docker Desktop.
     *
     * Windows -> Docker Desktop
     * macOS   -> Docker Desktop
     * Linux   -> Docker Desktop
     */
    public static void installDockerDesktop(String password)
            throws IOException, InterruptedException {

        if (EnvironmentUtils.isWindows()) {
            installDockerDesktopWindows();

        } else if (EnvironmentUtils.isMacOS()) {
            installDockerDesktopMacOS();

        } else if (EnvironmentUtils.isLinux()) {
            installDockerDesktopLinux(password);

        } else {
            throw unsupportedOS();
        }
    }

    /**
     * Installe uniquement Docker Engine.
     *
     * Windows -> WSL2 + Docker Engine dans WSL2
     * macOS   -> Colima + Docker CLI + Docker Engine via Colima
     * Linux   -> Docker Engine natif
     */
    public static void installDockerEngine(String password)
            throws Exception {

        if (EnvironmentUtils.isWindows()) {

            installDockerEngineWindows();

        } else if (EnvironmentUtils.isMacOS()) {

            installDockerEngineMacOS();

        } else if (EnvironmentUtils.isLinux()) {

            installDockerEngineLinux(password);

        } else {
            throw unsupportedOS();
        }

        verifyDockerEngine();
    }

    /**
     * Vérifie si Colima est installé.
     */
    public static boolean isColimaInstalled() {
        if (!EnvironmentUtils.isMacOS()) {
            return false;
        }

        return EnvironmentUtils.commandExists("colima");
    }

    /**
     * Vérifie si WSL est installé sous Windows.
     */
    public static boolean isWslInstalled() {

        if (!EnvironmentUtils.isWindows()) {
            return false;
        }

        try {
            EnvironmentUtils.run(
                    null,
                    null,
                    "wsl.exe",
                    "--status"
            );

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si WSL2 est réellement disponible.
     */
    public static boolean isWsl2Installed() {

        if (!EnvironmentUtils.isWindows()) {
            return false;
        }

        try {
            String output = EnvironmentUtils.run(
                    null,
                    null,
                    "wsl.exe",
                    "--status"
            );

            String normalized = output.toLowerCase();

            /*
             * Selon la version de Windows/WSL,
             * la sortie peut varier.
             */
            if (normalized.contains("default version: 2")
                    || normalized.contains("version: 2")) {
                return true;
            }

            /*
             * Vérification supplémentaire :
             * une distribution peut être configurée en version 2.
             */
            String distributions = EnvironmentUtils.run(
                    null,
                    null,
                    "wsl.exe",
                    "--list",
                    "--verbose"
            );

            return distributions
                    .toLowerCase()
                    .contains("2");

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérifie si une distribution WSL2 est disponible.
     */
    public static boolean hasWsl2Distribution() {

        if (!isWsl2Installed()) {
            return false;
        }

        try {
            String output = EnvironmentUtils.run(
                    null,
                    null,
                    "wsl.exe",
                    "--list",
                    "--verbose"
            );

            /*
             * Exemple :
             *
             * NAME      STATE    VERSION
             * Ubuntu    Stopped  2
             */
            return output
                    .lines()
                    .anyMatch(line ->
                            line.trim().matches(".*\\s2\\s*$")
                    );

        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================
    // INSTALLATION WSL2
    // =========================================================

    /**
     * Installe WSL2 sous Windows.
     *
     * L'élévation des droits est gérée par Windows,
     * pas par un mot de passe fourni au code Java.
     */
    public static void installWsl2()
            throws IOException, InterruptedException {

        if (!EnvironmentUtils.isWindows()) {
            throw new UnsupportedOperationException(
                    "WSL2 est disponible uniquement sous Windows."
            );
        }

        if (isWsl2Installed()) {
            return;
        }

        EnvironmentUtils.run(
                null,
                null,
                "wsl.exe",
                "--install",
                "--no-distribution"
        );

        /*
         * Après cette commande, Windows peut demander
         * un redémarrage.
         *
         * On ne considère donc pas forcément WSL2
         * immédiatement disponible dans le même processus.
         */
    }

    /**
     * Installe une distribution Linux WSL2.
     *
     * Ubuntu est utilisée ici comme distribution par défaut.
     */
    public static void installWsl2Distribution()
            throws IOException, InterruptedException {

        if (!EnvironmentUtils.isWindows()) {
            throw new UnsupportedOperationException(
                    "WSL2 est disponible uniquement sous Windows."
            );
        }

        if (!isWsl2Installed()) {
            installWsl2();
        }

        if (hasWsl2Distribution()) {
            return;
        }

        EnvironmentUtils.run(
                null,
                null,
                "wsl.exe",
                "--install",
                "-d",
                "Ubuntu"
        );
    }

    // =========================================================
    // COLIMA
    // =========================================================

    /**
     * Installe Colima sous macOS.
     *
     * Colima est installé avec Homebrew.
     */
    public static void installColima()
            throws IOException, InterruptedException {

        if (!EnvironmentUtils.isMacOS()) {
            throw new UnsupportedOperationException(
                    "Colima est utilisé ici uniquement sous macOS."
            );
        }

        if (isColimaInstalled()) {
            return;
        }

        if (!EnvironmentUtils.commandExists("brew")) {
            throw new IllegalStateException(
                    "Homebrew n'est pas installé."
            );
        }

        EnvironmentUtils.run(
                null,
                null,
                "brew",
                "install",
                "colima"
        );
    }

    // =========================================================
    // DOCKER DESKTOP
    // =========================================================

    private static void installDockerDesktopWindows()
            throws IOException, InterruptedException {

        EnvironmentUtils.run(
                null,
                null,
                "winget",
                "install",
                "--id",
                "Docker.DockerDesktop",
                "-e",
                "--source",
                "winget",
                "--accept-source-agreements",
                "--accept-package-agreements"
        );
    }

    private static void installDockerDesktopMacOS()
            throws IOException, InterruptedException {

        if (!EnvironmentUtils.commandExists("brew")) {
            throw new IllegalStateException(
                    "Homebrew n'est pas installé."
            );
        }

        EnvironmentUtils.run(
                null,
                null,
                "brew",
                "install",
                "--cask",
                "docker"
        );
    }

    private static void installDockerDesktopLinux(String password)
            throws IOException, InterruptedException {

        if (!EnvironmentUtils.isLinux()) {
            throw new UnsupportedOperationException(
                    "Docker Desktop Linux est disponible uniquement sous Linux."
            );
        }

        if (!EnvironmentUtils.commandExists("apt-get")) {
            throw new UnsupportedOperationException(
                    "L'installation automatique de Docker Desktop "
                            + "Linux est actuellement supportée uniquement "
                            + "pour les distributions Debian/Ubuntu."
            );
        }

        String architecture = EnvironmentUtils.run(
                null,
                null,
                "dpkg",
                "--print-architecture"
        ).trim();

        String packageUrl;

        if ("amd64".equals(architecture)) {
            packageUrl =
                    "https://desktop.docker.com/linux/main/amd64/docker-desktop-amd64.deb";
        } else if ("arm64".equals(architecture)) {
            packageUrl =
                    "https://desktop.docker.com/linux/main/arm64/docker-desktop-arm64.deb";
        } else {
            throw new UnsupportedOperationException(
                    "Architecture Linux non supportée par Docker Desktop : "
                            + architecture
            );
        }

        /*
         * Répertoire temporaire.
         */
        String packagePath =
                "/tmp/docker-desktop.deb";

        /*
         * Téléchargement du paquet.
         *
         * Le téléchargement lui-même ne nécessite pas sudo.
         */
        EnvironmentUtils.run(
                null,
                null,
                "curl",
                "-fL",
                packageUrl,
                "-o",
                packagePath
        );

        /*
         * Installation du paquet.
         *
         * Ici sudo est nécessaire.
         */
        EnvironmentUtils.runWithSudo(
                password,
                "apt-get",
                "install",
                "-y",
                packagePath
        );

        /*
         * Démarrage de Docker Desktop.
         *
         * Docker Desktop Linux utilise un service systemd utilisateur.
         */
        EnvironmentUtils.run(
                null,
                null,
                "systemctl",
                "--user",
                "start",
                "docker-desktop"
        );

        /*
         * Nettoyage du fichier téléchargé.
         */
        EnvironmentUtils.run(
                null,
                null,
                "rm",
                "-f",
                packagePath
        );
    }

    // =========================================================
    // DOCKER ENGINE - WINDOWS
    // =========================================================

    /**
     * Docker Engine sous Windows.
     *
     * Architecture :
     *
     * Windows
     *   └── WSL2
     *       └── Ubuntu
     *           └── Docker Engine
     */
    private static void installDockerEngineWindows()
            throws IOException, InterruptedException {

        if (!isWsl2Installed()) {
            installWsl2();
        }

        /*
         * Une distribution Linux est nécessaire pour
         * installer Docker Engine.
         */
        if (!hasWsl2Distribution()) {
            installWsl2Distribution();
        }

        /*
         * Installation de Docker Engine dans Ubuntu.
         *
         * Important :
         * le Docker Engine est installé DANS WSL2,
         * pas directement dans Windows.
         */
        installDockerEngineInWsl();
    }

    /**
     * Installe Docker Engine dans Ubuntu/WSL2.
     */
    private static void installDockerEngineInWsl()
            throws IOException, InterruptedException {

        /*
         * On utilise ici Ubuntu.
         *
         * Le mot de passe Linux n'est pas récupéré
         * depuis Windows.
         *
         * La première configuration de la distribution
         * Linux est effectuée par l'utilisateur.
         */

        EnvironmentUtils.run(
                null,
                null,
                "wsl.exe",
                "-d",
                "Ubuntu",
                "--",
                "bash",
                "-lc",
                "command -v docker >/dev/null 2>&1 || " +
                        "curl -fsSL https://get.docker.com | sh"
        );
    }

    // =========================================================
    // DOCKER ENGINE - MACOS
    // =========================================================

    /**
     * Docker Engine sous macOS via Colima.
     *
     * Architecture :
     *
     * macOS
     *   └── Colima
     *       └── VM Linux
     *           └── Docker Engine
     */
    private static void installDockerEngineMacOS()
            throws Exception {

        if (!EnvironmentUtils.commandExists("brew")) {
            throw new IllegalStateException(
                    "Homebrew n'est pas installé."
            );
        }

        /*
         * Colima
         */
        if (!isColimaInstalled()) {
            installColima();
        }

        /*
         * Docker CLI.
         *
         * On installe la CLI, pas Docker Desktop.
         */
        if (!EnvironmentUtils.commandExists("docker")) {
            EnvironmentUtils.run(
                    null,
                    null,
                    "brew",
                    "install",
                    "docker"
            );
        }

        /*
         * Démarre Colima.
         *
         * Colima fournit alors la VM Linux
         * contenant Docker Engine.
         */
        String colima = EnvironmentUtils.getCommand("colima");

        EnvironmentUtils.run(
                null,
                null,
                colima,
                "start"
        );
    }

    // =========================================================
    // DOCKER ENGINE - LINUX
    // =========================================================

    private static void installDockerEngineLinux(String password)
            throws IOException, InterruptedException {

        if (EnvironmentUtils.commandExists("apt-get")) {

            installDebianUbuntu(password);

        } else if (EnvironmentUtils.commandExists("dnf")) {

            installFedora(password);

        } else if (EnvironmentUtils.commandExists("yum")) {

            installRhel(password);

        } else if (EnvironmentUtils.commandExists("pacman")) {

            installArch(password);

        } else {

            throw new UnsupportedOperationException(
                    "Gestionnaire de paquets Linux non supporté."
            );
        }
    }

    // =========================================================
    // LINUX DISTRIBUTIONS
    // =========================================================

    private static void installDebianUbuntu(String password)
            throws IOException, InterruptedException {

        EnvironmentUtils.runWithSudo(
                password,
                "apt-get",
                "update"
        );

        EnvironmentUtils.runWithSudo(
                password,
                "apt-get",
                "install",
                "-y",
                "docker.io",
                "docker-compose-plugin"
        );

        EnvironmentUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    private static void installFedora(String password)
            throws IOException, InterruptedException {

        EnvironmentUtils.runWithSudo(
                password,
                "dnf",
                "install",
                "-y",
                "docker",
                "docker-compose-plugin"
        );

        EnvironmentUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    private static void installRhel(String password)
            throws IOException, InterruptedException {

        EnvironmentUtils.runWithSudo(
                password,
                "yum",
                "install",
                "-y",
                "docker"
        );

        EnvironmentUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    private static void installArch(String password)
            throws IOException, InterruptedException {

        EnvironmentUtils.runWithSudo(
                password,
                "pacman",
                "-S",
                "--noconfirm",
                "docker",
                "docker-compose"
        );

        EnvironmentUtils.runWithSudo(
                password,
                "systemctl",
                "enable",
                "--now",
                "docker"
        );
    }

    // =========================================================
    // VERIFICATION
    // =========================================================

    /**
     * Vérifie que Docker Engine répond réellement.
     *
     * docker --version ne suffit pas :
     * il vérifie seulement la CLI.
     */
    public static void verifyDockerEngine()
            throws Exception {

        if (!EnvironmentUtils.commandExists("docker")) {
            throw new RuntimeException(
                    "La commande 'docker' est introuvable."
            );
        }

        String docker = EnvironmentUtils.getCommand("docker");

        EnvironmentUtils.run(
                null,
                null,
                docker,
                "info"
        );
    }

    private static UnsupportedOperationException unsupportedOS() {
        return new UnsupportedOperationException(
                "OS non supporté : "
                        + System.getProperty("os.name")
        );
    }
}