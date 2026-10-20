package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.TableSelectionRequest;
import org.labs.genesis.api.dto.MetadataDtos.TableSelectionResponse;
import org.labs.genesis.api.dto.MetadataDtos.TablesMetadataResponse;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParametersResponse;

import java.util.*;

@Service
public class TableSelectionService {

    private final GenerationContextStore generationContextStore;
    private final TableMetadataService tableMetadataService;
    private final GenerationProfilesStore generationProfilesStore;
    private final ProfileRelationService profileRelationService;


    public TableSelectionService(GenerationContextStore generationContextStore, TableMetadataService tableMetadataService, GenerationProfilesStore generationProfilesStore, ProfileRelationService profileRelationService) {
        this.generationContextStore = generationContextStore;
        this.tableMetadataService = tableMetadataService;
        this.generationProfilesStore = generationProfilesStore;
        this.profileRelationService = profileRelationService;
    }


    public TableSelectionResponse select(TableSelectionRequest request) {
        validateRequest(request);
        ProjectGenerationContext context = generationContextStore.getContext();
        validateContext(context);
        List<String> selectedTables = normalizeNames(request.selectedTables());
        List<String> selectedViews = normalizeNames(request.selectedViews());
        if (selectedTables.isEmpty() && selectedViews.isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins une table ou une vue");
        }
        List<String> generationOptions = mapComponents(request.selectedComponents());
        context.setEntityNames(selectedTables);
        context.setViewNames(selectedViews);
        context.setGenerationOptions(generationOptions);
        try {
            context.setTables();
            context.autoDetectRelationParameters();
        } catch (RuntimeException exception) {
            throw new TableSelectionException("Impossible de charger les tables et vues sélectionnées : " + rootMessage(exception));
        }
        return new TableSelectionResponse(
                true,
                List.copyOf(context.getEntityNames()),
                List.copyOf(context.getViewNames()),
                List.copyOf(context.getGenerationOptions())
        );
    }

    private void validateRequest(TableSelectionRequest request) {
        if (request == null) {
            throw new TableSelectionException("La sélection est obligatoire");
        }
        if (request.selectedComponents() == null || request.selectedComponents().isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins un composant");
        }
    }

    private void validateContext(ProjectGenerationContext context) {
        if (context.getDatabase() == null || context.getCredentials() == null || context.getConnection() == null) {
            throw new TableSelectionException("La base de données doit être configurée avant la sélection");
        }
        if (context.getFramework() == null || context.getLanguage() == null) {
            throw new TableSelectionException("Le framework doit être sélectionné avant la sélection des tables");
        }
    }

    private List<String> normalizeNames(List<String> names) {
        if (names == null) {
            return List.of();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String name : names) {
            if (name != null && !name.isBlank()) {
                normalized.add(name.trim());
            }
        }
        return new ArrayList<>(normalized);
    }

    private List<String> mapComponents(List<String> components) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String component : components) {
            if (component == null || component.isBlank()) {
                continue;
            }
            switch (component.trim().toLowerCase(Locale.ROOT)) {
                case "model" ->
                        result.add(ProjectGenerationContext.COMPONENT_MODEL);
                case "dao" ->
                        result.add(ProjectGenerationContext.COMPONENT_DAO);
                case "service" ->
                        result.add(ProjectGenerationContext.COMPONENT_SERVICE);
                case "controller" ->
                        result.add(ProjectGenerationContext.COMPONENT_CONTROLLER);
                default ->
                        throw new TableSelectionException("Composant inconnu : " + component);
            }
        }
        if (result.isEmpty()) {
            throw new TableSelectionException("Sélectionnez au moins un composant valide");
        }
        return new ArrayList<>(result);
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }



    public synchronized GenerationParametersResponse saveGenerationParameters(List<GenerationParameterRequest> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new TableSelectionException("Au moins un profil de génération est obligatoire");
        }

        ProjectGenerationContext context = generationContextStore.getContext();

        validateContext(context);

        TablesMetadataResponse metadata = tableMetadataService.loadAll();

        Set<String> availableTables = metadata.tables()
                .stream()
                .map(table -> table.tableName().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        Set<String> availableViews = metadata.views()
                .stream()
                .map(view -> view.tableName().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        Set<String> profileNames = new HashSet<>();
        List<GenerationParameterRequest> validatedProfiles = new ArrayList<>();

        for (GenerationParameterRequest profile : parameters) {
            if (profile == null) {
                throw new TableSelectionException("Un profil de génération est null");
            }

            String profileName = profile.profileName();

            if (profileName == null || profileName.isBlank()) {
                throw new TableSelectionException("Le nom du profil est obligatoire");
            }

            profileName = profileName.trim();

            if (!profileNames.add(profileName.toLowerCase(Locale.ROOT))) {
                throw new TableSelectionException("Nom de profil dupliqué : " + profileName);
            }

            List<String> tables = normalizeNames(profile.selectedTables());
            List<String> views = normalizeNames(profile.selectedViews());

            if (tables.isEmpty() && views.isEmpty()) {
                throw new TableSelectionException("Le profil " + profileName + " ne contient aucune table ni vue");
            }

            validateSelectedNames(profileName, tables, availableTables, "table");
            validateSelectedNames(profileName, views, availableViews, "vue");

            if (profile.selectedComponents() == null) {
                throw new TableSelectionException("Les composants sont obligatoires pour " + profileName);
            }

            List<String> mappedComponents = mapComponents(profile.selectedComponents());
            List<String> normalizedComponents =
                    mappedComponents.stream()
                            .map(component -> component.toLowerCase(Locale.ROOT))
                            .toList();

            validatedProfiles.add(new GenerationParameterRequest(profileName, tables, views, normalizedComponents));
        }

        List<GenerationParameterRequest> previousProfiles = generationProfilesStore.getProfiles(context);

        profileRelationService.reconcileProfiles(previousProfiles, validatedProfiles);


        generationProfilesStore.save(context, validatedProfiles);

        return new GenerationParametersResponse(true, validatedProfiles.size() + " profil(s) de génération enregistré(s)");
    }

    private void validateSelectedNames(String profileName, List<String> selectedNames, Set<String> availableNames, String type) {
        for (String name : selectedNames) {
            if (!availableNames.contains(name.toLowerCase(Locale.ROOT))) {
                throw new TableSelectionException("Le profil " + profileName + " référence une " + type + " inconnue : " + name);
            }
        }
    }


    public List<GenerationParameterRequest> getGenerationParameters() {
        return generationProfilesStore.getProfiles(generationContextStore.getContext());
    }

    public void clearGenerationParameters() {
        generationProfilesStore.clear();
        profileRelationService.clear();
    }

}
