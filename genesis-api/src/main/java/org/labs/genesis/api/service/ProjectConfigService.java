package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigOptionsResponse;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigRequest;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigResponse;
import org.labs.genesis.api.exception.ProjectConfigException;
import org.labs.genesis.api.exception.FrameworkNotFoundException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.ConfigurationMetadata;
import org.labs.genesis.config.langage.Framework;
import org.labs.genesis.config.langage.Language;
import org.labs.genesis.config.langage.Project;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ProjectConfigService {

    private final GenerationContextStore generationContextStore;

    public ProjectConfigService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public ProjectConfigOptionsResponse getOptions() {
        ProjectGenerationContext context = generationContextStore.getContext();

        Framework framework = requireFramework(context);
        Language language = requireLanguage(context);

        return new ProjectConfigOptionsResponse(
                getOptions(language.getConfigurations(), "languageVersion"),
                getCompatibleProjects(framework).stream()
                        .map(Project::getName)
                        .map(name -> name.trim().toLowerCase(Locale.ROOT))
                        .toList(),
                getOptions(framework.getConfigurations(), "frameworkVersion"),
                Boolean.TRUE.equals(framework.getWithGroupId())
        );
    }

    public ProjectConfigResponse configure(ProjectConfigRequest request) {
        ProjectGenerationContext context = generationContextStore.getContext();

        Framework framework = requireFramework(context);
        Language language = requireLanguage(context);

        validateRequired(request);

        Project project = findCompatibleProject(framework, request.buildTool());
        Map<String, Object> languageConfiguration = createDefaultConfiguration(language.getConfigurations());
        Map<String, Object> frameworkConfiguration = createDefaultConfiguration(framework.getConfigurations());

        validateOption(language.getConfigurations(), "languageVersion", request.languageVersion(), "Version du langage non supportée");
        validateOption(framework.getConfigurations(), "frameworkVersion", request.frameworkVersion(), "Version du framework non supportée");

        languageConfiguration.put("languageVersion", request.languageVersion());
        frameworkConfiguration.put("frameworkVersion", request.frameworkVersion());

        applyFrameworkOption(framework, frameworkConfiguration, "loggingLevel", request.loggingLevel());
        applyFrameworkOption(framework, frameworkConfiguration, "securityType", request.securityType());
        applyFrameworkOption(framework, frameworkConfiguration, "cacheProvider", request.cacheProvider());
        applyFrameworkOption(framework, frameworkConfiguration, "hibernateDdlAuto", request.hibernateDdlAuto());

        String groupId = Boolean.TRUE.equals(framework.getWithGroupId()) ? request.groupId() : "";

        if (Boolean.TRUE.equals(framework.getWithGroupId()) && (groupId == null || groupId.isBlank())) {
            throw new ProjectConfigException("Le Group ID est obligatoire pour ce framework");
        }

        String projectPort = normalizeOptional(request.projectPort());
        validateProjectPort(projectPort);

        context.setProjectName(request.projectName().trim());
        context.setDestinationFolder(request.projectLocation().trim());
        context.setGroupLink(groupId == null ? "" : groupId.trim());
        context.setProject(project);
        context.setProjectDescription(normalizeOptional(request.projectDescription()));
        context.setProjectPort(projectPort);
        context.setLanguageConfiguration(languageConfiguration);
        context.setFrameworkConfiguration(frameworkConfiguration);

        return new ProjectConfigResponse(
                true,
                "Configuration du projet enregistrée avec succès",
                context.getProjectName(),
                context.getDestinationFolder(),
                request.languageVersion(),
                project.getName().trim().toLowerCase(Locale.ROOT),
                context.getGroupLink(),
                request.frameworkVersion(),
                context.getProjectDescription(),
                context.getProjectPort(),
                getStringOption(frameworkConfiguration, "loggingLevel"),
                getStringOption(frameworkConfiguration, "securityType"),
                getStringOption(frameworkConfiguration, "cacheProvider"),
                getStringOption(frameworkConfiguration, "hibernateDdlAuto")
        );
    }


    private void applyFrameworkOption(Framework framework, Map<String, Object> configuration, String variableName, String requestedValue) {
        if (requestedValue == null || requestedValue.isBlank()) {
            return;
        }
        List<ConfigurationMetadata> metadata = framework.getConfigurations();
        ConfigurationMetadata option = metadata == null
                ? null
                : metadata.stream()
                .filter(item -> variableName.equals(item.getVariableName()))
                .findFirst()
                .orElse(null);
        if (option == null) {
            throw new ProjectConfigException(
                    "L'option " + variableName
                            + " n'est pas supportée par ce framework"
            );
        }
        final String trimmed = requestedValue.trim();
        String value = trimmed;
        List<String> allowedValues = option.getOptions();
        if (allowedValues != null && !allowedValues.isEmpty()) {
            value = allowedValues.stream()
                    .filter(allowed -> allowed.equalsIgnoreCase(trimmed))
                    .findFirst()
                    .orElseThrow(() -> new ProjectConfigException(
                            "Valeur non supportée pour " + variableName + " : " + trimmed
                    ));
        }
        configuration.put(variableName, value);
    }


    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
    }

    private String getStringOption(Map<String, Object> configuration, String key) {
        Object value = configuration.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private void validateProjectPort(String port) {
        if (port.isEmpty()) {
            return;
        }

        final int numericPort;

        try {
            numericPort = Integer.parseInt(port);
        } catch (NumberFormatException exception) {
            throw new ProjectConfigException(
                    "Le port du projet doit être un entier valide"
            );
        }

        if (numericPort < 1 || numericPort > 65535) {
            throw new ProjectConfigException(
                    "Le port du projet doit être compris entre 1 et 65535"
            );
        }
    }



    private Framework requireFramework(ProjectGenerationContext context) {
        if (context.getFramework() == null) {
            throw new ProjectConfigException("Aucun framework sélectionné. " + "Sélectionnez d'abord un framework.");
        }
        return context.getFramework();
    }

    private Language requireLanguage(ProjectGenerationContext context) {
        if (context.getLanguage() == null) {
            throw new ProjectConfigException("Aucun langage n'est associé au framework sélectionné.");
        }
        return context.getLanguage();
    }

    private void validateRequired(ProjectConfigRequest request) {
        if (request == null) {
            throw new ProjectConfigException("La configuration du projet est obligatoire");
        }
        if (request.projectName() == null || request.projectName().isBlank()) {
            throw new ProjectConfigException("Le nom du projet est obligatoire");
        }
        if (request.projectLocation() == null || request.projectLocation().isBlank()) {
            throw new ProjectConfigException("La localisation du projet est obligatoire");
        }
        if (request.languageVersion() == null || request.languageVersion().isBlank()) {
            throw new ProjectConfigException("La version du langage est obligatoire");
        }
        if (request.buildTool() == null || request.buildTool().isBlank()) {
            throw new ProjectConfigException("L'outil de build est obligatoire");
        }
        if (request.frameworkVersion() == null || request.frameworkVersion().isBlank()) {
            throw new ProjectConfigException("La version du framework est obligatoire");
        }
    }

    private Project findCompatibleProject(Framework framework, String buildTool
    ) {
        return getCompatibleProjects(framework).stream()
                .filter(project ->
                        project.getName()
                                .trim()
                                .equalsIgnoreCase(buildTool.trim())
                )
                .findFirst()
                .orElseThrow(() -> new ProjectConfigException(
                        "Outil de build non supporté pour "
                                + framework.getCoreFramework()
                                + " : "
                                + buildTool
                ));
    }

    private List<Project> getCompatibleProjects(Framework framework) {
        return ProjectGenerator.projects.values().stream()
                .filter(project ->
                        project.getCoreFrameworks() != null
                                && project.getCoreFrameworks().stream()
                                .anyMatch(coreFramework ->
                                        coreFramework.equalsIgnoreCase(
                                                framework.getCoreFramework()
                                        )
                                )
                )
                .sorted((first, second) ->
                        Integer.compare(first.getId(), second.getId())
                )
                .toList();
    }

    private Map<String, Object> createDefaultConfiguration(List<ConfigurationMetadata> configurations) {
        Map<String, Object> result = new HashMap<>();

        if (configurations == null) {
            return result;
        }

        for (ConfigurationMetadata configuration : configurations) {
            result.put(configuration.getVariableName(), configuration.getDefaultOption());
        }

        return result;
    }

    private List<String> getOptions(List<ConfigurationMetadata> configurations, String variableName) {
        if (configurations == null) {
            return List.of();
        }

        return configurations.stream()
                .filter(configuration ->
                        variableName.equals(
                                configuration.getVariableName()
                        )
                )
                .findFirst()
                .map(ConfigurationMetadata::getOptions)
                .filter(options -> options != null)
                .orElse(List.of());
    }

    private void validateOption(List<ConfigurationMetadata> configurations, String variableName, String selectedValue, String errorMessage) {
        List<String> options = getOptions(configurations, variableName);

        if (!options.isEmpty() && !options.contains(selectedValue)) {
            throw new ProjectConfigException(errorMessage + " : " + selectedValue);
        }
    }

    public List<String> frameworkOptions(int frameworkId, String variableName) {
        Framework framework = ProjectGenerator.frameworks.get(frameworkId);

        if (framework == null) {
            throw new FrameworkNotFoundException(frameworkId);
        }

        return getOptions(framework.getConfigurations(), variableName);
    }

    public List<String> languageVersions(int languageId) {
        Language language = ProjectGenerator.findLanguageById(languageId);

        if (language == null) {
            throw new ProjectConfigException("Langage inconnu : " + languageId);
        }

        return getOptions(language.getConfigurations(), "languageVersion");
    }

    public List<String> buildTools(int frameworkId) {
        Framework framework = ProjectGenerator.frameworks.get(frameworkId);

        if (framework == null) {
            throw new FrameworkNotFoundException(frameworkId);
        }

        return getCompatibleProjects(framework)
                .stream()
                .map(Project::getName)
                .map(name -> name.trim().toLowerCase(Locale.ROOT))
                .toList();
    }

}