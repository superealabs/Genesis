package org.labs.genesis.api.controller;

import java.util.Map;
import org.labs.genesis.api.service.GitLocalService;
import org.labs.genesis.config.git.GitConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class GitController {

    private final GitLocalService gitLocalService;

    public GitController(GitLocalService gitLocalService) {
        this.gitLocalService = gitLocalService;
    }

    @PostMapping("/api/generator/git/config")
    public ResponseEntity<Map<String, Object>> configure(@RequestBody GitConfiguration configuration) {
        return ResponseEntity.ok(gitLocalService.configure(configuration));
    }

    @PostMapping("/api/generator/git/init")
    public Map<String, Object> init() throws Exception {
        return gitLocalService.init();
    }

    @DeleteMapping("/api/generator/git/config")
    public Map<String, Object> resetGit() {
        gitLocalService.reset();
        return Map.of("success", true, "message", "Configuration Git réinitialisée");
    }

}
