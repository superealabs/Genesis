package org.labs.genesis.api.controller;

import org.labs.genesis.api.service.GitLocalService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/generator/git")
public class GitLocalController {

    private final GitLocalService gitLocalService;

    public GitLocalController(GitLocalService gitLocalService) {
        this.gitLocalService = gitLocalService;
    }

    @PostMapping("/init")
    public Map<String, Object> init() throws Exception {
        return gitLocalService.init();
    }
}
