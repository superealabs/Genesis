package org.labs.genesis.api.controller;

import java.util.List;
import org.labs.genesis.api.dto.FrameworkDtos.*;
import org.labs.genesis.api.service.FrameworkService;
import org.springframework.web.bind.annotation.*;

@RestController
public class FrameworkController {

    private final FrameworkService frameworkService;

    public FrameworkController(FrameworkService frameworkService) {
        this.frameworkService = frameworkService;
    }

    @GetMapping("/api/frameworks")
    public List<FrameworkResponse> getAll() {
        return frameworkService.getAll();
    }

    @GetMapping("/api/frameworks/languages")
    public List<LanguageResponse> getLanguages() {
        return frameworkService.getLanguages();
    }

    @GetMapping("/api/frameworks/cores")
    public List<CoreFrameworkResponse> getCoreFrameworks() {
        return frameworkService.getCoreFrameworks();
    }

    @GetMapping("/api/frameworks/templates")
    public List<ViewTemplateResponse> getViewTemplates() {
        return frameworkService.getViewTemplates();
    }

    @PostMapping("/api/frameworks/{id}/select")
    public FrameworkSelectionResponse select(@PathVariable int id) {
        return frameworkService.select(id);
    }

    @PostMapping("/api/generator/frameworks/{id}/select")
    public FrameworkSelectionResponse selectFramework(@PathVariable int id) {
        return frameworkService.select(id);
    }
}
