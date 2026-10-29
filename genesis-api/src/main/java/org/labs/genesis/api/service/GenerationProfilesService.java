package org.labs.genesis.api.service;

import java.util.*;
import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.context.GenerationMode;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeResponse;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionRequest;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionResponse;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParametersResponse;
import org.labs.genesis.api.dto.MetadataDtos.TablesMetadataResponse;
import org.labs.genesis.api.exception.GenerationModeException;
import org.labs.genesis.api.exception.TableSelectionException;
import org.labs.genesis.api.service.MetadataService;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

@Service
public class GenerationProfilesService {

    public GenerationProfilesService(GenerationContextStore generationContextStore, MetadataService metadataService, GenerationProfilesStore generationProfilesStore, ProfileRelationService profileRelationService) {
        this.generationContextStore = generationContextStore;
        this.metadataService = metadataService;
        this.generationProfilesStore = generationProfilesStore;
        this.profileRelationService = profileRelationService;
    }

    private final GenerationContextStore generationContextStore;

    private final MetadataService metadataService;

    private final GenerationProfilesStore generationProfilesStore;

    private final ProfileRelationService profileRelationService;

    public synchronized GenerationParametersResponse saveGenerationParameters(List<GenerationParameterRequest> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new TableSelectionException("Au moins un profil de génération est obligatoire");
        }
        ProjectGenerationContext context = generationContextStore.getContext();
        metadataService.validateSelectionContext(context);
        TablesMetadataResponse metadata = metadataService.loadAll();
        Set<String> availableTables = metadata.tables().stream().map(table -> table.tableName().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
        Set<String> availableViews = metadata.views().stream().map(view -> view.tableName().toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toSet());
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
            List<String> tables = metadataService.normalizeNames(profile.selectedTables());
            List<String> views = metadataService.normalizeNames(profile.selectedViews());
            if (tables.isEmpty() && views.isEmpty()) {
                throw new TableSelectionException("Le profil " + profileName + " ne contient aucune table ni vue");
            }
            validateSelectedNames(profileName, tables, availableTables, "table");
            validateSelectedNames(profileName, views, availableViews, "vue");
            if (profile.selectedComponents() == null) {
                throw new TableSelectionException("Les composants sont obligatoires pour " + profileName);
            }
            List<String> mappedComponents = metadataService.mapComponents(profile.selectedComponents());
            List<String> normalizedComponents = mappedComponents.stream().map(component -> component.toLowerCase(Locale.ROOT)).toList();
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

    public List<GenerationModeResponse> getModes() {
        return List.of(new GenerationModeResponse(GenerationMode.GENERATE_NEW_PROJECT.name(), "Generate new Project"), new GenerationModeResponse(GenerationMode.ADD_RULE_IN_PROJECT.name(), "Add rule in Project"), new GenerationModeResponse(GenerationMode.SYNC_PROJECT.name(), "Sync Project"));
    }

    public GenerationModeSelectionResponse select(GenerationModeSelectionRequest request) {
        if (request == null || request.mode() == null || request.mode().isBlank()) {
            throw new GenerationModeException("Le mode de génération est obligatoire");
        }
        GenerationMode mode;
        try {
            mode = GenerationMode.valueOf(request.mode().trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new GenerationModeException("Mode de génération inconnu : " + request.mode());
        }
        generationContextStore.setGenerationMode(mode);
        return new GenerationModeSelectionResponse(true, mode.name());
    }
}
