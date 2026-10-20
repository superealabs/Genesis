
package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.FrameworkDtos.FrameworkSelectionResponse;
import org.labs.genesis.api.service.FrameworkService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator/frameworks")
public class GeneratorFrameworkController {

    private final FrameworkService frameworkService;

    public GeneratorFrameworkController(FrameworkService frameworkService) {
        this.frameworkService = frameworkService;
    }

    @PostMapping("/{id}/select")
    public FrameworkSelectionResponse selectFramework(@PathVariable int id) {
        return frameworkService.select(id);
    }
}
