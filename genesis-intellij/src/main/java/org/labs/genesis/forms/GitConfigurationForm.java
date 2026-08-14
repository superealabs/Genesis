package org.labs.genesis.forms;

import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.labs.utils.GitInstallerUtils;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Ref;

import javax.swing.*;
import java.awt.*;

@Getter
public class GitConfigurationForm {

    private JPanel mainPanel;

    private JCheckBox useGitCheckBox;

    private JLabel repositoryModeLabel;
    private JRadioButton singleRepositoryRadioButton;
    private JRadioButton separateRepositoriesRadioButton;

    private JLabel repositoryNameLabel;
    private JTextField repositoryNameField;

    private JLabel backendRepositoryNameLabel;
    private JTextField backendRepositoryNameField;

    private JLabel frontendRepositoryNameLabel;
    private JTextField frontendRepositoryNameField;

    private JLabel githubUsernameLabel;
    private JTextField githubUsernameField;

    private JCheckBox createRemoteRepositoryCheckBox;

    private JLabel githubTokenLabel;
    private JPasswordField githubTokenField;

    private JCheckBox privateRepositoryCheckBox;


    public GitConfigurationForm() {

        // ---------------------------------------------------------
        // Repository mode
        // ---------------------------------------------------------

        ButtonGroup repositoryGroup = new ButtonGroup();

        repositoryGroup.add(singleRepositoryRadioButton);
        repositoryGroup.add(separateRepositoriesRadioButton);


        // ---------------------------------------------------------
        // Listeners
        // ---------------------------------------------------------

        useGitCheckBox.addActionListener(e -> {
            if (useGitCheckBox.isSelected()) {
                checkGit();
            } else {
                refreshVisibility();
            }
        });

        singleRepositoryRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        separateRepositoriesRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        createRemoteRepositoryCheckBox.addActionListener(e ->
                refreshVisibility()
        );


        // ---------------------------------------------------------
        // Default values
        // ---------------------------------------------------------

        useGitCheckBox.setSelected(false);

        singleRepositoryRadioButton.setSelected(true);

        createRemoteRepositoryCheckBox.setSelected(false);

        privateRepositoryCheckBox.setSelected(false);


        // ---------------------------------------------------------
        // Initial visibility
        // ---------------------------------------------------------

        refreshVisibility();
    }

    private void checkGit() {

        // Git est déjà installé
        if (GitInstallerUtils.commandExists("git")) {
            refreshVisibility();
            return;
        }

        // ---------------------------------------------------------
        // Git absent
        // ---------------------------------------------------------
        int result = Messages.showYesNoDialog(
                "Git CLI n'est pas installé sur cette machine.\n\n"
                        + "L'initialisation d'un repository Git nécessite Git CLI.\n\n"
                        + "Voulez-vous installer Git maintenant ?",
                "Git requis",
                "Installer Git",
                "Annuler",
                Messages.getQuestionIcon()
        );

        // ---------------------------------------------------------
        // L'utilisateur refuse
        // ---------------------------------------------------------

        if (result != Messages.YES) {

            useGitCheckBox.setSelected(false);

            refreshVisibility();

            return;
        }

        // ---------------------------------------------------------
        // Installation
        // ---------------------------------------------------------

        installGit();
    }

    private void installGit() {
        // 1. Demander le mot de passe AVANT de lancer la tâche de fond (sur le thread principal)
        String sudoPassword = null;
        if (GitInstallerUtils.isLinux()) {
            sudoPassword = askSudoPassword();

            // Si l'utilisateur a annulé la saisie du mot de passe
            if (sudoPassword == null) {
                useGitCheckBox.setSelected(false);
                refreshVisibility();
                return; // On annule l'installation
            }
        }

        // 2. On désactive la checkbox et on lance la tâche
        useGitCheckBox.setEnabled(false);
        final String finalPassword = sudoPassword; // Doit être 'effectively final' pour être utilisé dans le thread

        new Task.Backgroundable(
                null,
                "Installation de Git",
                true
        ) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);
                    indicator.setText("Installation de Git...");

                    // 3. Plus besoin de UI ici, on utilise juste le mot de passe récupéré plus haut
                    GitInstallerUtils.installGit(finalPassword);

                    indicator.setText("Vérification de l'installation...");

                    if (!GitInstallerUtils.commandExists("git")) {
                        throw new RuntimeException(
                                "Git semble avoir été installé, mais la commande 'git' reste introuvable."
                        );
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(
                            "Erreur lors de l'installation de Git: " + e.getMessage(),
                            e
                    );
                }
            }

            @Override
            public void onSuccess() {
                useGitCheckBox.setEnabled(true);
                useGitCheckBox.setSelected(true);
                refreshVisibility();

                Messages.showInfoMessage(
                        mainPanel,
                        "Git a été installé avec succès.",
                        "Git installé"
                );
            }

            @Override
            public void onThrowable(Throwable error) {
                useGitCheckBox.setEnabled(true);
                useGitCheckBox.setSelected(false);
                refreshVisibility();

                String message = error.getMessage();
                if (message == null || message.isBlank()) {
                    message = error.toString();
                }

                Messages.showErrorDialog(
                        mainPanel,
                        message,
                        "Erreur lors de l'installation de Git"
                );
            }
        }.queue();
    }

    private String askSudoPassword() {
        // Comme nous sommes déjà sur le thread principal (EDT), plus besoin de invokeAndWait !
        JPasswordField passwordField = new JPasswordField();

        int result = JOptionPane.showConfirmDialog(
                mainPanel, // Utilisez mainPanel au lieu de null pour bien centrer la modale
                passwordField,
                "Mot de passe Sudo requis",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            return new String(passwordField.getPassword());
        }

        return null;
    }

    private static class InstallationCancelledException
            extends RuntimeException {

        public InstallationCancelledException() {
            super("Installation annulée.");
        }
    }


    private void refreshVisibility() {

        boolean useGit =
                useGitCheckBox.isSelected();

        // =========================================================
        // REPOSITORY MODE
        // =========================================================

        repositoryModeLabel.setVisible(useGit);

        singleRepositoryRadioButton.setVisible(useGit);

        separateRepositoriesRadioButton.setVisible(useGit);


        boolean separate =
                separateRepositoriesRadioButton.isSelected();


        // =========================================================
        // SINGLE REPOSITORY
        // =========================================================

        repositoryNameLabel.setVisible(
                useGit && !separate
        );

        repositoryNameField.setVisible(
                useGit && !separate
        );


        // =========================================================
        // SEPARATE REPOSITORIES
        // =========================================================

        backendRepositoryNameLabel.setVisible(
                useGit && separate
        );

        backendRepositoryNameField.setVisible(
                useGit && separate
        );

        frontendRepositoryNameLabel.setVisible(
                useGit && separate
        );

        frontendRepositoryNameField.setVisible(
                useGit && separate
        );


        // =========================================================
        // GITHUB USERNAME
        // =========================================================

        // Le username est demandé dès qu'un repository est configuré.
        githubUsernameLabel.setVisible(useGit);

        githubUsernameField.setVisible(useGit);


        // =========================================================
        // REMOTE REPOSITORY
        // =========================================================

        createRemoteRepositoryCheckBox.setVisible(useGit);


        boolean createRemote =
                useGit &&
                        createRemoteRepositoryCheckBox.isSelected();


        // =========================================================
        // TOKEN
        // =========================================================

        githubTokenLabel.setVisible(createRemote);

        githubTokenField.setVisible(createRemote);


        // =========================================================
        // PRIVATE REPOSITORY
        // =========================================================

        privateRepositoryCheckBox.setVisible(createRemote);


        // =========================================================
        // REFRESH
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}