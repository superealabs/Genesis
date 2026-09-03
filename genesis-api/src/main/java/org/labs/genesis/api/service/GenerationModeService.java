package org.labs.genesis.api.service;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.context.GenerationMode;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeResponse;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionRequest;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionResponse;
import org.labs.genesis.api.exception.GenerationModeException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationModeService {

    private final GenerationContextStore generationContextStore;

    public GenerationModeService(GenerationContextStore generationContextStore) {
        this.generationContextStore = generationContextStore;
    }

    public List<GenerationModeResponse> getModes() {
        return List.of(
                new GenerationModeResponse(GenerationMode.GENERATE_NEW_PROJECT.name(), "Generate new Project"),
                new GenerationModeResponse(GenerationMode.ADD_RULE_IN_PROJECT.name(), "Add rule in Project"),
                new GenerationModeResponse(GenerationMode.SYNC_PROJECT.name(), "Sync Project"
                )
        );
    }

    public GenerationModeSelectionResponse select(GenerationModeSelectionRequest request) {
        if (request == null || request.mode() == null || request.mode().isBlank()) {
            throw new GenerationModeException("Le mode de génération est obligatoire"
            );
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
