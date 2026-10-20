package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.FrameworkDtos.*;
import org.labs.genesis.api.service.FrameworkService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/frameworks")
public class FrameworkController {

    private final FrameworkService frameworkService;

    public FrameworkController(FrameworkService frameworkService) {
        this.frameworkService = frameworkService;
    }

    @GetMapping
    public List<FrameworkResponse> getAll() {
        return frameworkService.getAll();
    }

    @GetMapping("/languages")
    public List<LanguageResponse> getLanguages() {
        return frameworkService.getLanguages();
    }

    @GetMapping("/cores")
    public List<CoreFrameworkResponse> getCoreFrameworks() {
        return frameworkService.getCoreFrameworks();
    }

    @GetMapping("/templates")
    public List<ViewTemplateResponse> getViewTemplates() {
        return frameworkService.getViewTemplates();
    }

    @PostMapping("/{id}/select")
    public FrameworkSelectionResponse select(@PathVariable int id) {
        return frameworkService.select(id);
    }
}