package org.labs.genesis.forms;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.labs.utils.DockerInstallerUtils;
import org.labs.utils.DockerUtils;
import org.labs.utils.EnvironmentUtils;

import javax.swing.*;

@Getter
public class DockerConfigurationForm {

    private JPanel mainPanel;

    private JCheckBox configureDockerCheckBox;

    private JLabel dockerGenerationModeLabel;

    private JRadioButton dockerFrontendRadioButton;
    private JRadioButton dockerBackendRadioButton;
    private JRadioButton dockerBothRadioButton;

    private JLabel frontendContainerNameLabel;
    private JTextField frontendContainerNameField;

    private JLabel backendContainerNameLabel;
    private JTextField backendContainerNameField;


    public DockerConfigurationForm() {

        // ---------------------------------------------------------
        // Docker mode
        // ---------------------------------------------------------

        ButtonGroup dockerGroup = new ButtonGroup();

        dockerGroup.add(dockerFrontendRadioButton);
        dockerGroup.add(dockerBackendRadioButton);
        dockerGroup.add(dockerBothRadioButton);


        // ---------------------------------------------------------
        // Listeners
        // ---------------------------------------------------------

        configureDockerCheckBox.addActionListener(e -> {
            refreshVisibility();
            if(configureDockerCheckBox.isSelected()) checkDocker();
        });

        dockerFrontendRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        dockerBackendRadioButton.addActionListener(e ->
                refreshVisibility()
        );

        dockerBothRadioButton.addActionListener(e ->
                refreshVisibility()
        );


        // ---------------------------------------------------------
        // Default values
        // ---------------------------------------------------------

        configureDockerCheckBox.setSelected(false);

        dockerBothRadioButton.setSelected(true);

        frontendContainerNameField.setText("frontend");

        backendContainerNameField.setText("backend");


        // ---------------------------------------------------------
        // Initial visibility
        // ---------------------------------------------------------

        refreshVisibility();
    }

    private void checkDocker() {

        // Docker est déjà installé
        if (DockerUtils.isDockerAvailable()) {
            return;
        }

        // ---------------------------------------------------------
        // Docker absent
        // ---------------------------------------------------------
        int result = Messages.showYesNoDialog(
                mainPanel,
                "Docker n'est pas installé sur cette machine.\n"
                        + "Voulez-vous installer Docker maintenant ?",
                "Installation de Docker",
                Messages.getQuestionIcon()
        );

        // ---------------------------------------------------------
        // L'utilisateur refuse
        // ---------------------------------------------------------
        if (result != Messages.YES) {
            return;
        }

        // ---------------------------------------------------------
        // Choisir Docker Desktop ou Docker Engine
        // ---------------------------------------------------------
        String[] options = {
                "Docker Desktop",
                "Docker Engine"
        };

        int dockerType = Messages.showDialog(
                mainPanel,
                "Choisissez le type d'installation de Docker :",
                "Type d'installation",
                options,
                0,
                Messages.getQuestionIcon()
        );

        // L'utilisateur ferme/annule la boîte de dialogue
        if (dockerType < 0) {
            return;
        }

        // ---------------------------------------------------------
        // Demander le mot de passe AVANT le Background Task
        // ---------------------------------------------------------
        String password = null;

        if (EnvironmentUtils.isLinux()) {
            password = GitConfigurationForm.askSudoPassword(mainPanel);

            // L'utilisateur a annulé
            if (password == null) {
                return;
            }
        }

        final String finalPassword = password;
        final int finalDockerType = dockerType;

        // ---------------------------------------------------------
        // Installation dans une tâche de fond
        // ---------------------------------------------------------
        new Task.Backgroundable(
                null,
                "Installation de Docker",
                true
        ) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(true);

                    if (finalDockerType == 0) {

                        indicator.setText(
                                "Installation de Docker Desktop..."
                        );

                        DockerInstallerUtils.installDockerDesktop(finalPassword);

                    } else {

                        indicator.setText(
                                "Installation de Docker Engine..."
                        );

                        DockerInstallerUtils.installDockerEngine(
                                finalPassword
                        );
                    }

                    indicator.setText(
                            "Vérification de l'installation..."
                    );

                    if (!DockerUtils.isDockerAvailable()) {
                        throw new RuntimeException(
                                "Docker semble avoir été installé, "
                                        + "mais la commande 'docker' reste introuvable."
                        );
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    throw new RuntimeException(
                            "Erreur lors de l'installation de Docker : "
                                    + e.getMessage(),
                            e
                    );
                }
            }

            @Override
            public void onSuccess() {
                Messages.showInfoMessage(
                        mainPanel,
                        "Docker a été installé avec succès.",
                        "Docker installé"
                );
            }

            @Override
            public void onThrowable(Throwable error) {

                String message = error.getMessage();

                if (message == null || message.isBlank()) {
                    message = error.toString();
                }

                Messages.showErrorDialog(
                        mainPanel,
                        message,
                        "Erreur lors de l'installation de Docker"
                );
            }

        }.queue();
    }


    private void refreshVisibility() {

        boolean useDocker =
                configureDockerCheckBox.isSelected();


        // =========================================================
        // DOCKER GENERATION MODE
        // =========================================================

        dockerGenerationModeLabel.setVisible(useDocker);

        dockerFrontendRadioButton.setVisible(useDocker);

        dockerBackendRadioButton.setVisible(useDocker);

        dockerBothRadioButton.setVisible(useDocker);


        // =========================================================
        // DOCKERIZED SERVICES
        // =========================================================

        boolean frontendDockerized =
                useDocker &&
                        (
                                dockerFrontendRadioButton.isSelected()
                                        || dockerBothRadioButton.isSelected()
                        );

        boolean backendDockerized =
                useDocker &&
                        (
                                dockerBackendRadioButton.isSelected()
                                        || dockerBothRadioButton.isSelected()
                        );


        // =========================================================
        // FRONTEND CONTAINER
        // =========================================================

        frontendContainerNameLabel.setVisible(
                frontendDockerized
        );

        frontendContainerNameField.setVisible(
                frontendDockerized
        );


        // =========================================================
        // BACKEND CONTAINER
        // =========================================================

        backendContainerNameLabel.setVisible(
                backendDockerized
        );

        backendContainerNameField.setVisible(
                backendDockerized
        );


        // =========================================================
        // REFRESH UI
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}