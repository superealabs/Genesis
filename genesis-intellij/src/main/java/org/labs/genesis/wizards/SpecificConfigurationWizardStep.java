package org.labs.genesis.wizards;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.generator.indicator.ProgressReporter;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.context.GenerationContextManager;
import org.labs.genesis.forms.SpecificConfigurationForm;
import org.labs.genesis.indicator.IntelliJProgressAdapter;
import org.labs.utils.StringUtils;
import com.intellij.openapi.project.Project;
import com.intellij.ide.impl.ProjectUtil;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.sql.Connection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecificConfigurationWizardStep extends ModuleWizardStep {
    private final SpecificConfigurationForm specificConfigurationForm;
    private final GenerationContextManager generationContextManager;
    private final ProjectGenerator projectGenerator = new ProjectGenerator();

    public SpecificConfigurationWizardStep(GenerationContextManager generationContextManagert) {
        this.specificConfigurationForm = new SpecificConfigurationForm();
        this.generationContextManager = generationContextManagert;

        // Initialiser les composants du formulaire
        specificConfigurationForm.initializeForm();
    }

    public static boolean frameworkHasConfiguration(Framework framework, String variableName) {
        return framework != null && framework.getConfigurations().stream()
                .anyMatch(config -> variableName.equals(config.getVariableName()));
    }

    @Override
    public JComponent getComponent() {
        return specificConfigurationForm.getMainPanel();
    }

    @Override
    public void updateDataModel() {
        Framework framework = generationContextManager.getContext().getFramework();
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

        // Gestion du type de sécurisation
        frameworkConfiguration.put("securityType", Objects.requireNonNullElseGet(
                specificConfigurationForm.getSecurityTypeOptions().getSelectedItem(), () -> "").toString()
        );

        // Gestion du cache
        frameworkConfiguration.put("cacheProvider", Objects.requireNonNullElseGet(
                specificConfigurationForm.getCacheProviderOptions().getSelectedItem(), () -> "").toString()
        );
        if (!specificConfigurationForm.getSelectedTableAndViewNamesList().getSelectedValuesList().isEmpty()) {
            List<String> selectedEntities = specificConfigurationForm.getSelectedTableAndViewNamesList().getSelectedValuesList();

            // Formate every entity name to match with class naming convention
            List<String> entitiesCacheable = selectedEntities.stream()
                    .filter(tableName -> tableName != null && !tableName.isBlank())
                    .map(tableName -> Stream.of(tableName)
                            .map(String::toLowerCase)
                            .map(StringUtils::toCamelCase)
                            .map(StringUtils::majStart)
                            .map(StringUtils::removeLastS)
                            .findFirst()
                            .orElse(""))
                    .filter(formatted -> !formatted.isEmpty())
                    .collect(Collectors.toList());

            frameworkConfiguration.put("entitiesCacheable", entitiesCacheable);
        }

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

        // Gestion de la création du venv pour Django
        if (framework != null && framework.getCoreFramework() != null && 
            framework.getCoreFramework().equalsIgnoreCase("Django")) {
            boolean createVenv = specificConfigurationForm.getCreateVenvCheckBox() != null && 
                                specificConfigurationForm.getCreateVenvCheckBox().isSelected();
            frameworkConfiguration.put("createVenv", createVenv);

            // Gestion de l'auth optionnelle (login/inscription)
            boolean enableAuth = true;
            if (specificConfigurationForm.getEnableAuthCheckBox() != null) {
                enableAuth = specificConfigurationForm.getEnableAuthCheckBox().isSelected();
            }
            frameworkConfiguration.put("enableAuth", enableAuth);
        }

        // Ajouter projectPort et projectDescription au contexte
        generationContextManager.getContext().setProjectPort(specificConfigurationForm.getProjectPortField().getText().trim());
        generationContextManager.getContext().setProjectDescription(specificConfigurationForm.getProjectDescriptionField().getText().trim());

        generationContextManager.getContext().setFrameworkConfiguration(frameworkConfiguration);

        Project project = ProjectUtil.getProjectForComponent(specificConfigurationForm.getMainPanel());
        if (project == null) {
            project = ProjectUtil.getActiveProject();
        }

        if (project == null) {
            throw new IllegalStateException("Impossible de déterminer le contexte Project IntelliJ.");
        }
        try {
            ProgressManager.getInstance().run(new Task.Modal(project, "Génération du Projet", true) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    IntelliJProgressAdapter processIndicator = new IntelliJProgressAdapter(indicator);
                    processIndicator.setProgress(0,"Processing ...");
                    try {
                        generateProject(processIndicator);
                        processIndicator.setProgress(1,"Generation complete");
                    } catch (Exception e) {
                        throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
                    }
                }
            });
            Messages.showInfoMessage(project,
                    "Project generation completed successfully",
                    "Success");

        } catch (Exception e) {
            Messages.showErrorDialog(project,
                    "Une erreur inattendue est survenue lors de la génération : " + e.getMessage() + "\n\n" +
                            "Veuillez consulter la console d'exécution ou les **logs d'IntelliJ** pour plus de détails (Help -> Show Log in Explorer/Finder).",
                    "Échec de la Génération du Projet");
            throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean validate() throws ConfigurationException {
        Framework framework = generationContextManager.getContext().getFramework();

        // Valider les options spécifiques au framework
        validateLoggingLevel(framework);
        validateHibernateDdlAuto(framework);

        // Valider les options Eureka Server
        validateEurekaServer();

        // Valider les champs spécifiques au projet
        validateProjectPort();
        validateProjectDescription();

        // Valider la configuration du cache
        validateCache();

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
        if (!frameworkHasConfiguration(framework, "logginglevel")) {
            return;
        }
        String loggingLevel = Objects.toString(specificConfigurationForm.getLoggingLevelOptions().getSelectedItem(), "").trim();
        if (loggingLevel.isEmpty()) {
            throw new ConfigurationException("Logging Level cannot be empty.");
        }
    }

    private void validateHibernateDdlAuto(Framework framework) throws ConfigurationException {
        if (!frameworkHasConfiguration(framework, "hibernateDdlAuto")) {
            return;
        }
        String hibernateDdlAuto = Objects.toString(specificConfigurationForm.getDdlAutoOptions().getSelectedItem(), "").trim();
        if (hibernateDdlAuto.isEmpty()) {
            throw new ConfigurationException("Hibernate DDL Auto cannot be empty.");
        }
    }

    private void validateEurekaServer() throws ConfigurationException {
        if (specificConfigurationForm.getUseAnEurekaServerCheckBox().isSelected() &&
                specificConfigurationForm.getEurekaServerHostField().getText().trim().isEmpty()) {
            throw new ConfigurationException("Eureka Server Host cannot be empty if Eureka is enabled.");
        }
    }

    private void validateCache() throws ConfigurationException {
        if (!specificConfigurationForm.getCacheProviderOptions().getSelectedItem().equals("NONE") &&
                specificConfigurationForm.getSelectedTableAndViewNamesList().getSelectedValuesList().isEmpty()) {
            throw new ConfigurationException("Please select at least one table or view to cache.");
        }
    }

    private void validateGatewayAuthentication() throws ConfigurationException {
        String username = specificConfigurationForm.getUsernameField().getText().trim();
        String password = new String(specificConfigurationForm.getPasswordField().getPassword()).trim();
        String role = specificConfigurationForm.getRoleField().getText().trim();
        HashMap<String, String> gatewayMap = new HashMap<>() {{
            put(username, "Username for API Gateway cannot be empty.");
            put(password, "Password for API Gateway cannot be empty.");
            put(role, "Role for API Gateway cannot be empty.");
        }};
        for (Map.Entry<String, String> e : gatewayMap.entrySet()) {
            if (e.getKey().isEmpty()) {
                throw new ConfigurationException(e.getValue());
            }
        }
    }

    private void validateRouteTable() throws ConfigurationException {
        List<Map<String, String>> routes = specificConfigurationForm.getRouteConfigurationData();
        HashMap<String, String> routeOptionsMap = new HashMap<>();
        String routeId = "";
        String uri = "";
        String path = "";
        String method = "";
        for (int i = 0; i < routes.size(); i++) {
            routeId = routes.get(i).get("id");
            uri = routes.get(i).get("uri");
            path = routes.get(i).get("path");
            method = routes.get(i).get("method");

            routeOptionsMap.put(routeId, "Route ID in row " + (i + 1) + " cannot be empty.");
            routeOptionsMap.put(uri, "URI in row " + (i + 1) + " cannot be empty.");
            routeOptionsMap.put(path, "Path in row " + (i + 1) + " cannot be empty.");
            routeOptionsMap.put(method, "Method in row " + (i + 1) + " cannot be empty.");

            for (Map.Entry<String, String> e : routeOptionsMap.entrySet()) {
                if (e.getKey() == null || e.getKey().trim().isEmpty()) {
                    throw new ConfigurationException(e.getValue());
                }
            }
            routeOptionsMap.clear();
        }
    }

    private void generateProject(ProgressReporter indicator) throws Exception {
        try {
            projectGenerator.generateProject(generationContextManager.getContext(), indicator);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Project generation failed: " + e.getMessage(), e);
        } finally {
            Connection con = generationContextManager.getContext().getConnection();
            if(con!=null) con.close();
        }
    }

    public void onFrameworkSelected(Framework framework) {
        if (framework == null) {
            throw new IllegalArgumentException("Framework must not be null");
        }
        specificConfigurationForm.updateFormWithFramework(framework);
    }

    public void onTablesAndViewsSelected(List<String> selectedValues, List<String> selectedViewValues) {
        if (selectedValues.isEmpty() && selectedViewValues.isEmpty()) {
            throw new IllegalArgumentException("At least one table or view must be selected");
        }
        specificConfigurationForm.updateFormWithTablesAndViews(selectedValues, selectedViewValues);
    }

    @Override
    public boolean isStepVisible() {
        return generationContextManager.getContext().getGenerationProcess().isGenerateProjectProcess();
    }
}
