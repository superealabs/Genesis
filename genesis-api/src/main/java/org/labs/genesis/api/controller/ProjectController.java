package org.labs.genesis.api.controller;

import java.util.List;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigOptionsResponse;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigRequest;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigResponse;
import org.labs.genesis.api.service.ProjectConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProjectController {

    private final ProjectConfigService projectConfigService;

    public ProjectController(ProjectConfigService projectConfigService) {
        this.projectConfigService = projectConfigService;
    }

    @GetMapping({ "/api/project/config/options", "/api/generator/project-config/options" })
    public ProjectConfigOptionsResponse getOptions() {
        return projectConfigService.getOptions();
    }

    @PostMapping({ "/api/project/config", "/api/generator/project-config" })
    public ProjectConfigResponse configure(@RequestBody ProjectConfigRequest request) {
        return projectConfigService.configure(request);
    }

    @GetMapping("/api/logging_levels/{frameworkId}")
    public List<String> loggingLevels(@PathVariable int frameworkId) {
        return projectConfigService.frameworkOptions(frameworkId, "loggingLevel");
    }

    @GetMapping("/api/security_types/{frameworkId}")
    public List<String> securityTypes(@PathVariable int frameworkId) {
        return projectConfigService.frameworkOptions(frameworkId, "securityType");
    }

    @GetMapping("/api/cache_providers/{frameworkId}")
    public List<String> cacheProviders(@PathVariable int frameworkId) {
        return projectConfigService.frameworkOptions(frameworkId, "cacheProvider");
    }

    @GetMapping("/api/hibernate_ddl_auto_options/{frameworkId}")
    public List<String> hibernateOptions(@PathVariable int frameworkId) {
        return projectConfigService.frameworkOptions(frameworkId, "hibernateDdlAuto");
    }

    @GetMapping("/api/generator/language-versions/{languageId}")
    public List<String> languageVersions(@PathVariable int languageId) {
        return projectConfigService.languageVersions(languageId);
    }

    @GetMapping("/api/generator/framework-versions/{frameworkId}")
    public List<String> frameworkVersions(@PathVariable int frameworkId) {
        return projectConfigService.frameworkOptions(frameworkId, "frameworkVersion");
    }

    @GetMapping("/api/generator/build-tools/{frameworkId}")
    public List<String> buildTools(@PathVariable int frameworkId) {
        return projectConfigService.buildTools(frameworkId);
    }
}
