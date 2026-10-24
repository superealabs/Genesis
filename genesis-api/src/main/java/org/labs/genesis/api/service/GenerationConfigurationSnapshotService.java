package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.config.ProjectGenerationContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationConfigurationSnapshotService {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final ProfileRelationService profileRelationService;

    public GenerationConfigurationSnapshotService(
            GenerationContextStore contextStore,
            GenerationProfilesStore profilesStore,
            ProfileRelationService profileRelationService
    ) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.profileRelationService = profileRelationService;
    }

    public synchronized GenerationConfigurationSnapshot capture() {
        ProjectGenerationContext source = contextStore.getContext();
        List<GenerationParameterRequest> profiles = profilesStore.getProfiles(source);
        if (profiles == null || profiles.isEmpty()) {
            throw new org.labs.genesis.api.exception.ProjectConfigException("Aucun profil de génération disponible");
        }
        return new GenerationConfigurationSnapshot(profiles, profileRelationService.snapshotRelations(profiles));
    }
}
