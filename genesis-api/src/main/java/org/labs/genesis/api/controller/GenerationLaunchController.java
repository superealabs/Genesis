package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.GenerationExecutionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/generator")
public class GenerationLaunchController {
    private final GenerationExecutionService executionService;

    public GenerationLaunchController(GenerationExecutionService executionService) {
        this.executionService = executionService;
    }

    @PostMapping("/launch")
    public GenerationExecutionService.LaunchResult launch() throws Exception {
        return executionService.launch();
    }
}
