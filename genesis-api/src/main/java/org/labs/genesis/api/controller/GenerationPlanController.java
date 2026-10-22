package org.labs.genesis.api.controller;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.api.dto.GenerationPlanDtos.GenerationPlanResponse;
import org.labs.genesis.api.service.GenerationProfilePlanner;
import org.labs.genesis.api.service.GenerationProfilesStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generator")
public class GenerationPlanController {

    private final GenerationContextStore contextStore;
    private final GenerationProfilesStore profilesStore;
    private final GenerationProfilePlanner planner;

    public GenerationPlanController(GenerationContextStore contextStore, GenerationProfilesStore profilesStore, GenerationProfilePlanner planner) {
        this.contextStore = contextStore;
        this.profilesStore = profilesStore;
        this.planner = planner;
    }

    @GetMapping("/plan")
    public GenerationPlanResponse getPlan() {
        return planner.plan(profilesStore.getProfiles(contextStore.getContext()));
    }
}
