package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.GenerationDtos.GenerationModeResponse;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionRequest;
import org.labs.genesis.api.dto.GenerationDtos.GenerationModeSelectionResponse;
import org.labs.genesis.api.service.GenerationModeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generation/modes")
public class GenerationModeController {

    private final GenerationModeService generationModeService;

    public GenerationModeController(GenerationModeService generationModeService) {
        this.generationModeService = generationModeService;
    }

    @GetMapping
    public List<GenerationModeResponse> getModes() {
        return generationModeService.getModes();
    }

    @PostMapping("/select")
    public GenerationModeSelectionResponse select(@RequestBody GenerationModeSelectionRequest request) {
        return generationModeService.select(request);
    }
}
