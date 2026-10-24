package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.ProjectConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProjectOptionsController {

    private final ProjectConfigService service;

    public ProjectOptionsController(ProjectConfigService service) {
        this.service = service;
    }

    @GetMapping("/logging_levels/{frameworkId}")
    public List<String> loggingLevels(@PathVariable int frameworkId) {
        return service.frameworkOptions(frameworkId, "loggingLevel");
    }

    @GetMapping("/security_types/{frameworkId}")
    public List<String> securityTypes(@PathVariable int frameworkId) {
        return service.frameworkOptions(frameworkId, "securityType");
    }

    @GetMapping("/cache_providers/{frameworkId}")
    public List<String> cacheProviders(@PathVariable int frameworkId) {
        return service.frameworkOptions(frameworkId, "cacheProvider");
    }

    @GetMapping("/hibernate_ddl_auto_options/{frameworkId}")
    public List<String> hibernateOptions(@PathVariable int frameworkId) {
        return service.frameworkOptions(frameworkId, "hibernateDdlAuto");
    }

    @GetMapping("/generator/language-versions/{languageId}")
    public List<String> languageVersions(@PathVariable int languageId) {
        return service.languageVersions(languageId);
    }

    @GetMapping("/generator/framework-versions/{frameworkId}")
    public List<String> frameworkVersions(@PathVariable int frameworkId) {
        return service.frameworkOptions(frameworkId, "frameworkVersion");
    }

    @GetMapping("/generator/build-tools/{frameworkId}")
    public List<String> buildTools(@PathVariable int frameworkId) {
        return service.buildTools(frameworkId);
    }
}
