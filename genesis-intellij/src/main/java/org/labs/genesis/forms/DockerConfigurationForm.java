package org.labs.genesis.forms;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.docker.DockerConf;
import org.labs.utils.DockerInstallerUtils;
import org.labs.utils.DockerUtils;
import org.labs.utils.EnvironmentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

@Getter
public class DockerConfigurationForm {

    private JPanel mainPanel;
    private final ProjectGenerationContext context;

    private JCheckBox configureDockerCheckBox;

    private JLabel dockerGenerationModeLabel;

    private JRadioButton dockerFrontendRadioButton;
    private JRadioButton dockerBackendRadioButton;
    private JRadioButton dockerBothRadioButton;

    private JLabel commandLabel;
    private JLabel commandFrontendLabel;
    private JComboBox<DockerConf.Command> command;
    private JComboBox<DockerConf.Command> commandFrontend;

    private JLabel imageLabel;
    private JLabel imageFrontendLabel;

    private JComboBox<DockerConf.Image> image;
    private JComboBox<DockerConf.Image> imageFrontend;

    private JLabel frontendContainerNameLabel;
    private JTextField frontendContainerNameField;

    private JLabel backendContainerNameLabel;
    private JTextField backendContainerNameField;
    private boolean initCombo = false;


    public DockerConfigurationForm(ProjectGenerationContext context) {

        this.context = context;

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
            if(configureDockerCheckBox.isSelected()) {
                if(!initCombo) {
                    configureDockerOptions();
                    initCombo = true;
                }
                checkDocker();
            }
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

    private void configureCommands(List<DockerConf.Command> commands,
                                   JComboBox<DockerConf.Command> command) {
        if (commands == null || commands.isEmpty()) {
            command.setEnabled(false);
            return;
        }

        for (DockerConf.Command dockerCommand : commands) {

            if (dockerCommand != null) {
                command.addItem(dockerCommand);
            }
        }

        command.setEnabled(command.getItemCount() > 0);

        if (command.getItemCount() > 0) {
            command.setSelectedIndex(0);
        }

        command.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof DockerConf.Command dockerCommand) {
                    setText(dockerCommand.getCommand());
                }

                return this;
            }
        });
    }

    private void configureImages(List<DockerConf.Image> images,
                                 JComboBox<DockerConf.Image> image) {

        if (images == null || images.isEmpty()) {
            image.setEnabled(false);
            return;
        }

        for (DockerConf.Image dockerImage : images) {
            if (dockerImage != null) {
                image.addItem(dockerImage);
            }
        }

        image.setEnabled(image.getItemCount() > 0);

        if (image.getItemCount() > 0) {
            image.setSelectedIndex(0);
        }

        image.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {

                super.getListCellRendererComponent(
                        list,
                        value,
                        index,
                        isSelected,
                        cellHasFocus
                );

                if (value instanceof DockerConf.Image dockerImage) {
                    String image = dockerImage.getImage().replaceAll("([^a-zA-Z0-9])\\$\\{[^}]+}", "")
                            .replaceAll("\\$\\{[^}]+}", "");

                    setText(image);
                }
                return this;
            }
        });
    }

    private String formatImageDisplayName(String image) {
        if (image == null || image.isBlank()) {
            return "";
        }

        if (image.startsWith("maven:")) {
            if (image.contains("eclipse-temurin")) {
                return "Maven / Eclipse Temurin";
            }
            if (image.contains("amazoncorretto")) {
                return "Maven / Amazon Corretto";
            }
            return "Maven";
        }

        if (image.startsWith("node:")) {
            if (image.contains("alpine")) {
                return "Node.js / Alpine";
            }
            if (image.contains("bookworm")) {
                return "Node.js / Bookworm";
            }
            return "Node.js";
        }

        if (image.startsWith("mcr.microsoft.com/dotnet/sdk:")) {
            return ".NET SDK";
        }

        if (image.startsWith("python:")) {
            if (image.contains("alpine")) {
                return "Python / Alpine";
            }
            if (image.contains("slim")) {
                return "Python / Slim";
            }
            return "Python";
        }

        return image;
    }

    private void configureDockerOptions() {

        if (context == null) return;

        command.removeAllItems();
        commandFrontend.removeAllItems();

        image.removeAllItems();
        imageFrontend.removeAllItems();

        if (context.getFramework() != null
                && context.getFramework().getDocker() != null) {

            List<DockerConf.Command> commands =
                    context.getFramework().getDocker().getCommands();

            configureCommands(commands, command);

            List<DockerConf.Image> images =
                    context.getFramework().getDocker().getImages();

            configureImages(images, image);
        }

        if (context.getFrontendFramework() != null
                && context.getFrontendFramework().getDocker() != null) {

            List<DockerConf.Command> commandsFrontend =
                    context.getFrontendFramework().getDocker().getCommands();

            configureCommands(commandsFrontend, commandFrontend);

            List<DockerConf.Image> imagesFrontend =
                    context.getFrontendFramework().getDocker().getImages();

            configureImages(imagesFrontend, imageFrontend);
        }
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

        boolean useDocker = configureDockerCheckBox.isSelected();

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
        // COMMANDS
        // =========================================================

        commandFrontendLabel.setVisible(frontendDockerized);
        commandFrontend.setVisible(frontendDockerized);

        commandLabel.setVisible(backendDockerized);
        command.setVisible(backendDockerized);

        // =========================================================
        // IMAGES
        // =========================================================

        imageFrontendLabel.setVisible(frontendDockerized);
        imageFrontend.setVisible(frontendDockerized);

        imageLabel.setVisible(backendDockerized);
        image.setVisible(backendDockerized);


        // =========================================================
        // FRONTEND CONTAINER
        // =========================================================

        frontendContainerNameLabel.setVisible(frontendDockerized);
        frontendContainerNameField.setVisible(frontendDockerized);


        // =========================================================
        // BACKEND CONTAINER
        // =========================================================

        backendContainerNameLabel.setVisible(backendDockerized);
        backendContainerNameField.setVisible(backendDockerized);


        // =========================================================
        // REFRESH UI
        // =========================================================

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}