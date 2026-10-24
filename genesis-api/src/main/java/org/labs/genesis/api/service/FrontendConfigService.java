package org.labs.genesis.api.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.FrontendDtos.*;
import org.labs.genesis.api.exception.ProjectConfigException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.labs.genesis.config.langage.FrameworkMVC;
import org.labs.genesis.config.langage.generator.project.ProjectGenerator;
import org.labs.genesis.frontend.FrontendLanguage;
import org.labs.genesis.frontend.generator.FrontendFramework;
import org.labs.genesis.frontend.generator.model.FrontendLayout;
import org.labs.genesis.frontend.generator.model.InterfaceLang;
import org.labs.genesis.frontend.generator.model.ProjectBranding;
import org.springframework.stereotype.Service;

@Service
public class FrontendConfigService {
    private final GenerationContextStore store;
    private final FrontendFrameworkIsolationService isolation;
    private final FrontendAssetService assetService;

    public FrontendConfigService(GenerationContextStore store, FrontendFrameworkIsolationService isolation, FrontendAssetService assetService) {
        this.store = store;
        this.isolation = isolation;
        this.assetService = assetService;
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










    public List<ProgrammingLanguageOption> programmingLanguages() {
        return ProjectGenerator.frontendLanguage.values()
                .stream()
                .sorted(Comparator.comparingInt(FrontendLanguage::getId))
                .map(language -> new ProgrammingLanguageOption(language.getId(), language.getName(), language.getExtension()
                ))
                .toList();
    }

    public List<InterfaceLanguageOption> interfaceLanguages() {
        return ProjectGenerator.langs.values()
                .stream()
                .sorted(Comparator.comparingInt(InterfaceLang::getId))
                .map(language -> new InterfaceLanguageOption(language.getId(), language.getLocale(), language.getName()
                ))
                .toList();
    }

    public List<String> navbarTypes() {
        return List.of("side");
    }





    public void configureLayout(FrontendLayoutRequest request) {
        ProjectGenerationContext context = store.getContext();

        if (request == null || context.getFrontendFramework() == null) {
            throw new ProjectConfigException("Configurez d'abord le framework frontend");
        }

        // 1. Valider la navbar
        String navbar = switch (Objects.toString(request.navbarType(), "").trim().toLowerCase(Locale.ROOT)) {
            case "side" -> "Sidebar";
            default -> throw new ProjectConfigException("Type de navbar non pris en charge : " + request.navbarType());
        };

        // 2. Valider les couleurs
        Pattern hexColor = Pattern.compile("^#[0-9a-fA-F]{6}$");

        if (request.primaryColor() == null || !hexColor.matcher(request.primaryColor()).matches()) {
            throw new ProjectConfigException("Couleur principale invalide");
        }

        if (request.secondaryColor() == null || !hexColor.matcher(request.secondaryColor()).matches()) {
            throw new ProjectConfigException("Couleur secondaire invalide"
            );
        }

        // 3. Valider les langues d'interface
        if (request.selectedInterfaceLanguages() == null || request.selectedInterfaceLanguages().isEmpty()) {
            throw new ProjectConfigException("Sélectionnez au moins une langue d'interface"
            );
        }

        List<InterfaceLang> languages =
                request.selectedInterfaceLanguages()
                        .stream()
                        .map(code -> {
                            if (code == null || code.isBlank()) {
                                throw new ProjectConfigException("Code de langue invalide");
                            }

                            return ProjectGenerator.langs.values()
                                    .stream()
                                    .filter(lang -> lang.getLocale().equalsIgnoreCase(code.trim()))
                                    .findFirst()
                                    .orElseThrow(() -> new ProjectConfigException("Langue inconnue : " + code));
                        })
                        .distinct()
                        .toList();

        // 4. Valider le port avant de modifier le contexte
        String validatedPort = null;

        if (request.port() != null && !request.port().isBlank()) {
            int port;

            try {
                port = Integer.parseInt(request.port().trim());
            } catch (NumberFormatException exception) {
                throw new ProjectConfigException("Port frontend invalide");
            }

            if (port < 1 || port > 65535) {
                throw new ProjectConfigException("Port frontend hors limites");
            }

            validatedPort = Integer.toString(port);
        }

        // 5. Valider les URL HTTPS du logo et du favicon
        String logoUrl = validateBrandingUrl(request.logoPath(), "Logo");

        String faviconUrl = validateBrandingUrl(request.faviconPath(), "Favicon");

        // 6. Normaliser les identifiants de fichiers transférés
        String logoAssetId = request.logoAssetId();
        String faviconAssetId = request.faviconAssetId();

        if (logoAssetId != null && logoAssetId.isBlank()) {
            logoAssetId = null;
        }

        if (faviconAssetId != null && faviconAssetId.isBlank()) {
            faviconAssetId = null;
        }

        // 7. Interdire une URL et un fichier pour la même ressource
        if (logoUrl != null && logoAssetId != null) {
            throw new ProjectConfigException("Choisissez soit une URL, soit un fichier pour le logo");
        }

        if (faviconUrl != null && faviconAssetId != null) {
            throw new ProjectConfigException("Choisissez soit une URL, soit un fichier pour le favicon");
        }

        // 8. Résoudre les fichiers locaux déjà transférés au serveur
        Path logoFile = null;
        Path faviconFile = null;

        try {
            if (logoAssetId != null) {
                logoFile = assetService.resolve(logoAssetId);
            }

            if (faviconAssetId != null) {
                faviconFile = assetService.resolve(faviconAssetId);
            }
        } catch (IllegalArgumentException exception) {
            throw new ProjectConfigException("Logo ou favicon transféré introuvable ou invalide");
        }

        // 9. Copier le framework sans modifier le contexte original
        var framework = isolation.copy(context.getFrontendFramework());

        // 10. Configurer le layout sur la copie
        FrontendLayout layout = framework.getFrontendLayout();

        if (layout == null) {
            layout = new FrontendLayout();
            framework.setFrontendLayout(layout);
        }

        layout.setNavbar(navbar);
        layout.setPrimaryColor(request.primaryColor());
        layout.setSecondaryColor(request.secondaryColor());
        layout.setLangs(new ArrayList<>(languages));

        // 11. Configurer le branding sur la copie
        ProjectBranding branding = framework.getProjectBranding();

        if (branding == null) {
            branding = new ProjectBranding();
            framework.setProjectBranding(branding);
        }

        branding.setLogoFile(logoFile == null ? null : logoFile.toFile());
        branding.setFaviconFile(faviconFile == null ? null : faviconFile.toFile());

        branding.setLogoLink(logoUrl);
        branding.setFaviconLink(faviconUrl);

        // 12. Enregistrer le framework seulement après validation
        context.setFrontendFramework(framework);

        // 13. Enregistrer le port validé
        if (validatedPort != null) {
            context.setFrontendPort(validatedPort);
        }
    }

    private String validateBrandingUrl(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            java.net.URI uri = java.net.URI.create(value.trim());

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getHost().isBlank()
                    || uri.getRawUserInfo() != null) {
                throw new IllegalArgumentException();
            }
            return uri.toString();
        } catch (IllegalArgumentException exception) {
            throw new ProjectConfigException(field + " doit être une URL HTTPS valide");
        }
    }
}
