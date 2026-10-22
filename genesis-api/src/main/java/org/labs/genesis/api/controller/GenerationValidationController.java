package org.labs.genesis.api.controller;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;
import org.labs.genesis.api.service.GenerationProfilesStore;
import org.labs.genesis.api.service.GenerationRelationConflictValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
public class GenerationValidationController {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final GenerationRelationConflictValidator validator;

    public GenerationValidationController(GenerationContextStore contextStore, GenerationProfilesStore profilesStore, GenerationRelationConflictValidator validator) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.validator = validator;
    }

    @GetMapping("/validate-relations")
    public ValidationResponse validateRelations() {
        List<GenerationParameterRequest> profiles = profilesStore.getProfiles(contextStore.getContext());
        validator.validate(profiles);
        return new ValidationResponse(true, "Aucun conflit de relations entre les profils");
    }

    public record ValidationResponse(boolean valid, String message) {
    }
}
