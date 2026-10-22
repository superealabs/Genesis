package org.labs.genesis.api.controller;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.GenerationExecutionPlanDtos.ExecutionPlanResponse;
import org.labs.genesis.api.dto.MetadataDtos.GenerationParameterRequest;

import org.labs.genesis.api.service.GenerationExecutionPlanner;
import org.labs.genesis.api.service.GenerationProfilesStore;
import org.labs.genesis.api.service.GenerationRelationConflictValidator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
public class GenerationExecutionPlanController {
    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final GenerationExecutionPlanner executionPlanner;
    private final GenerationRelationConflictValidator relationValidator;

    public GenerationExecutionPlanController(GenerationContextStore contextStore, GenerationProfilesStore profilesStore, GenerationExecutionPlanner executionPlanner, GenerationRelationConflictValidator relationValidator) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.executionPlanner = executionPlanner;
        this.relationValidator = relationValidator;
    }

    @GetMapping("/execution-plan")
    public ExecutionPlanResponse getExecutionPlan() {
        List<GenerationParameterRequest> profiles = profilesStore.getProfiles(contextStore.getContext());
        relationValidator.validate(profiles);
        return executionPlanner.prepare(profiles);
    }
}
