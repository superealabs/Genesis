package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.forms.SpecificConfigurationForm;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SpecificConfigurationWizardStep extends ModuleWizardStep {
    private final SpecificConfigurationForm specificConfigurationForm;
    private final ProjectGenerationContext projectGenerationContext;
    private final ProjectGenerator projectGenerator = new ProjectGenerator();

    public SpecificConfigurationWizardStep(ProjectGenerationContext projectGenerationContext) {
        this.specificConfigurationForm = new SpecificConfigurationForm();
        this.projectGenerationContext = projectGenerationContext;

        // Initialiser les composants du formulaire
        specificConfigurationForm.initializeForm();
    }

    @Override
    public JComponent getComponent() {
        return specificConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        Framework framework = projectGenerationContext.getFramework();
        Map<String, Object> frameworkConfiguration = new HashMap<>();

        // Gestion d'Eureka
        if (specificConfigurationForm.getUseAnEurekaServerCheckBox().isSelected()) {
            framework.setUseCloud(true);
            framework.setUseEurekaServer(true);
            frameworkConfiguration.put("eurekaServerURL", specificConfigurationForm.getEurekaServerHostField().getText().trim());
        }

        // Gestion de loggingLevel
        frameworkConfiguration.put("loggingLevel", Objects.requireNonNullElseGet(
                specificConfigurationForm.getLoggingLevelOptions().getSelectedItem(), () -> "").toString()
        );

        // Gestion de hibernate ddl option
        frameworkConfiguration.put("hibernateDdlAuto", Objects.requireNonNullElseGet(
                specificConfigurationForm.getDdlAutoOptions().getSelectedItem(), () -> "").toString()
        );

        // Gestion des routes et de l'authentification si c'est une Gateway
        if (framework != null && framework.getIsGateway()) {
            frameworkConfiguration.put("routes", specificConfigurationForm.getRouteConfigurationData());
            frameworkConfiguration.put("username", specificConfigurationForm.getUsernameField().getText().trim());
            frameworkConfiguration.put("password", new String(specificConfigurationForm.getPasswordField().getPassword()).trim());
            frameworkConfiguration.put("role", specificConfigurationForm.getRoleField().getText().trim());
        }

        // Ajouter projectPort et projectDescription au contexte
        projectGenerationContext.setProjectPort(specificConfigurationForm.getProjectPortField().getText().trim());
        projectGenerationContext.setProjectDescription(specificConfigurationForm.getProjectDescriptionField().getText().trim());

        projectGenerationContext.setFrameworkConfiguration(frameworkConfiguration);

        // Génération du projet
        generateProject();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        Framework framework = projectGenerationContext.getFramework();

        // Valider les options spécifiques au framework
        validateLoggingLevel(framework);
        validateHibernateDdlAuto(framework);

        // Valider les options Eureka Server
        validateEurekaServer();

        // Valider les champs spécifiques au projet
        validateProjectPort();
        validateProjectDescription();

        // Valider les champs pour les API Gateway
        if (framework != null && framework.getIsGateway()) {
            validateGatewayAuthentication();
            validateRouteTable();
        }

        return true;
    }

    private void validateProjectPort() throws ConfigurationException {
        String projectPort = specificConfigurationForm.getProjectPortField().getText().trim();
        if (projectPort.isEmpty()) {
            throw new ConfigurationException("Project Port cannot be empty.");
        }
        try {
            int port = Integer.parseInt(projectPort);
            if (port < 1 || port > 65535) {
                throw new ConfigurationException("Project Port must be a valid number between 1 and 65535.");
            }
        } catch (NumberFormatException e) {
            throw new ConfigurationException("Project Port must be a valid integer.");
        }
    }

    private void validateProjectDescription() throws ConfigurationException {
        String projectDescription = specificConfigurationForm.getProjectDescriptionField().getText().trim();
        if (projectDescription.isEmpty()) {
            throw new ConfigurationException("Project Description cannot be empty.");
        }
    }

    private void validateLoggingLevel(Framework framework) throws ConfigurationException {
        if (frameworkHasConfiguration(framework, "loggingLevel")) {
            String loggingLevel = Objects.toString(specificConfigurationForm.getLoggingLevelOptions().getSelectedItem(), "").trim();
            if (loggingLevel.isEmpty()) {
                throw new ConfigurationException("Logging Level cannot be empty.");
            }
        }
    }

    private void validateHibernateDdlAuto(Framework framework) throws ConfigurationException {
        if (frameworkHasConfiguration(framework, "hibernateDdlAuto")) {
            String hibernateDdlAuto = Objects.toString(specificConfigurationForm.getDdlAutoOptions().getSelectedItem(), "").trim();
            if (hibernateDdlAuto.isEmpty()) {
                throw new ConfigurationException("Hibernate DDL Auto cannot be empty.");
            }
        }
    }

    private void validateEurekaServer() throws ConfigurationException {
        if (specificConfigurationForm.getUseAnEurekaServerCheckBox().isSelected() &&
                specificConfigurationForm.getEurekaServerHostField().getText().trim().isEmpty()) {
            throw new ConfigurationException("Eureka Server Host cannot be empty if Eureka is enabled.");
        }
    }

    private void validateGatewayAuthentication() throws ConfigurationException {
        String username = specificConfigurationForm.getUsernameField().getText().trim();
        if (username.isEmpty()) {
            throw new ConfigurationException("Username for API Gateway cannot be empty.");
        }

        String password = new String(specificConfigurationForm.getPasswordField().getPassword()).trim();
        if (password.isEmpty()) {
            throw new ConfigurationException("Password for API Gateway cannot be empty.");
        }

        String role = specificConfigurationForm.getRoleField().getText().trim();
        if (role.isEmpty()) {
            throw new ConfigurationException("Role for API Gateway cannot be empty.");
        }
    }

    private void validateRouteTable() throws ConfigurationException {
        List<Map<String, String>> routes = specificConfigurationForm.getRouteConfigurationData();

        for (int i = 0; i < routes.size(); i++) {
            Map<String, String> route = routes.get(i);

            String routeId = route.get("id");
            if (routeId == null || routeId.trim().isEmpty()) {
                throw new ConfigurationException("Route ID in row " + (i + 1) + " cannot be empty.");
            }

            String uri = route.get("uri");
            if (uri == null || uri.trim().isEmpty()) {
                throw new ConfigurationException("URI in row " + (i + 1) + " cannot be empty.");
            }

            String path = route.get("path");
            if (path == null || path.trim().isEmpty()) {
                throw new ConfigurationException("Path in row " + (i + 1) + " cannot be empty.");
            }

            String method = route.get("method");
            if (method == null || method.trim().isEmpty()) {
                throw new ConfigurationException("Method in row " + (i + 1) + " cannot be empty.");
            }
        }
    }

    private void generateProject() {
        try {
            projectGenerator.generateProject(projectGenerationContext);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
        }
    }

    public static boolean frameworkHasConfiguration(Framework framework, String variableName) {
        return framework != null && framework.getConfigurations().stream()
                .anyMatch(config -> variableName.equals(config.getVariableName()));
    }

    public void onFrameworkSelected(Framework framework) {
        if (framework == null) {
            throw new IllegalArgumentException("Framework must not be null");
        }
        specificConfigurationForm.updateFormWithFramework(framework);
    }
}
