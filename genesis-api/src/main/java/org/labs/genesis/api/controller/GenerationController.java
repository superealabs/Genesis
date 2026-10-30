package org.labs.genesis.api.controller;

import java.util.List;
import java.util.Map;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.GenerationDtos;
import org.labs.genesis.api.dto.GenerationDtos.*;
import org.labs.genesis.api.dto.MetadataDtos.*;
import org.labs.genesis.api.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class GenerationController {

    private final GenerationProfilesService generationProfilesService;

    private final GenerationContextStore generationContextStore;

    private final GenerationProfilesStore generationProfilesStore;

    private final GenerationPlanningService generationPlanningService;

    private final GenerationRelationService generationRelationService;

    private final GenerationExecutionService generationExecutionService;

    private final GenerationResetService generationResetService;

    public GenerationController(GenerationProfilesService generationProfilesService, GenerationContextStore generationContextStore, GenerationProfilesStore generationProfilesStore, GenerationPlanningService generationPlanningService, GenerationRelationService generationRelationService, GenerationExecutionService generationExecutionService, GenerationResetService generationResetService) {
        this.generationProfilesService = generationProfilesService;
        this.generationContextStore = generationContextStore;
        this.generationProfilesStore = generationProfilesStore;
        this.generationPlanningService = generationPlanningService;
        this.generationRelationService = generationRelationService;
        this.generationExecutionService = generationExecutionService;
        this.generationResetService = generationResetService;
    }

    @GetMapping("/api/generation/modes")
    public List<GenerationModeResponse> getModes() {
        return generationProfilesService.getModes();
    }

    @PostMapping("/api/generation/modes/select")
    public GenerationModeSelectionResponse select(@RequestBody GenerationModeSelectionRequest request) {
        return generationProfilesService.select(request);
    }

    @GetMapping("/api/generator/plan")
    public GenerationPlanResponse getPlan() {
        return generationPlanningService.plan(generationProfilesStore.getProfiles(generationContextStore.getContext()));
    }

    @GetMapping("/api/generator/execution-plan")
    public ExecutionPlanResponse getExecutionPlan() {
        List<GenerationParameterRequest> profiles = generationProfilesStore.getProfiles(generationContextStore.getContext());
        generationRelationService.validate(profiles);
        return generationPlanningService.prepare(profiles);
    }

    @GetMapping("/api/generator/validate-relations")
    public ValidationResponse validateRelations() {
        List<GenerationParameterRequest> profiles = generationProfilesStore.getProfiles(generationContextStore.getContext());
        generationRelationService.validate(profiles);
        return new ValidationResponse(true, "Aucun conflit de relations entre les profils");
    }

    @PostMapping("/api/generator/launch")
    public GenerationDtos.LaunchResult launch() throws Exception {
        return generationExecutionService.launch();
    }

    @PostMapping("/api/generator/generation-parameters")
    public GenerationParametersResponse saveGenerationParameters(@RequestBody List<GenerationParameterRequest> parameters) {
        return generationProfilesService.saveGenerationParameters(parameters);
    }

    @GetMapping("/api/generator/generation-parameters")
    public List<GenerationParameterRequest> getGenerationParameters() {
        return generationProfilesService.getGenerationParameters();
    }

    @DeleteMapping("/api/generator/generation-parameters")
    public GenerationParametersResponse clearGenerationParameters() {
        generationProfilesService.clearGenerationParameters();
        return new GenerationParametersResponse(true, "Profils de génération réinitialisés");
    }

    @DeleteMapping("/api/generator/reset")
    public Map<String, Object> resetAll() throws Exception {
        generationResetService.resetAll();
        return Map.of("success", true, "message", "Contexte Genesis réinitialisé avec succès");
    }

}
