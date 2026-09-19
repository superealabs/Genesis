package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigOptionsResponse;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigRequest;
import org.labs.genesis.api.dto.ProjectDtos.ProjectConfigResponse;
import org.labs.genesis.api.service.ProjectConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project/config")
public class ProjectConfigController {

    private final ProjectConfigService projectConfigService;

    public ProjectConfigController(ProjectConfigService projectConfigService) {
        this.projectConfigService = projectConfigService;
    }

    @GetMapping("/options")
    public ProjectConfigOptionsResponse getOptions() {
        return projectConfigService.getOptions();
    }

    @PostMapping
    public ProjectConfigResponse configure(@RequestBody ProjectConfigRequest request) {
        return projectConfigService.configure(request);
    }
}