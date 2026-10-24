package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.exception.ProjectConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FrontendConfigService {
    private final GenerationContextStore store;
    private final FrontendFrameworkIsolationService isolation;

    public FrontendConfigService(GenerationContextStore store, FrontendFrameworkIsolationService isolation) {
        this.store = store;
        this.isolation = isolation;
    }

    public List<FrameworkOption> frameworks() {
        return ProjectGenerator.frontendFrameworks.values().stream()
                .sorted(Comparator.comparingInt(FrontendFramework::getId))
                .map(framework -> new FrameworkOption(framework.getId(), framework.getName(),
                        framework.getCoreFramework(), framework.getLanguageId(), framework.getDefaultPort()))
                .toList();
    }

    public FrontendConfigResponse configure(FrontendConfigRequest request) {
        ProjectGenerationContext context = store.getContext();
        if (context.getFramework() == null || context.getFramework() instanceof FrameworkMVC
                || !Boolean.TRUE.equals(context.getFramework().getUseFrontendApp())) {
            throw new ProjectConfigException("Sélectionnez un framework API compatible avec un frontend séparé");
        }
        if (request == null || request.frameworkId() == null) {
            throw new ProjectConfigException("Le framework frontend est obligatoire");
        }
        FrontendFramework framework = ProjectGenerator.frontendFrameworks.get(request.frameworkId());
        if (framework == null) {
            throw new ProjectConfigException("Framework frontend inconnu");
        }
        var language = ProjectGenerator.frontendLanguage.get(
                request.languageId() == null ? framework.getLanguageId() : request.languageId());
        if (language == null || language.getId() != framework.getLanguageId()) {
            throw new ProjectConfigException("Langage frontend inconnu ou incompatible");
        }
        String port = request.port() == null || request.port().isBlank()
                ? framework.getDefaultPort() : request.port().trim();
        try {
            int numericPort = Integer.parseInt(port);
            if (numericPort < 1 || numericPort > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new ProjectConfigException("Le port frontend doit être compris entre 1 et 65535");
        }
        context.setFrontendFramework(framework);
        isolation.isolate(context);
        context.setFrontendLanguage(language);
        context.setFrontendPort(port);
        context.setGenerateFrontendApp(true);
        context.setFrontendGenerationOptions(new ArrayList<>(List.of(
                ProjectGenerationContext.FRONTEND_COMPONENT_MODEL,
                ProjectGenerationContext.FRONTEND_COMPONENT_SERVICE,
                ProjectGenerationContext.FRONTEND_COMPONENT)));
        return new FrontendConfigResponse(true, framework.getId(), language.getId(), port);
    }

    public record FrameworkOption(int id, String name, String coreFramework, int languageId, String defaultPort) {}
    public record FrontendConfigRequest(Integer frameworkId, Integer languageId, String port) {}
    public record FrontendConfigResponse(boolean success, int frameworkId, int languageId, String port) {}
}
