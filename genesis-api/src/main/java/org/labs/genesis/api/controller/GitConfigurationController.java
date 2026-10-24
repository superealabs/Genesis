package org.labs.genesis.api.controller;

import org.labs.genesis.api.context.GenerationContextStore;
import org.labs.genesis.config.git.GitConfiguration;
import org.labs.genesis.api.service.GitLocalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/generator/git")
public class GitConfigurationController {

    private final GenerationContextStore contextStore;

    public GitConfigurationController(GenerationContextStore contextStore) {
        this.contextStore = contextStore;
    }

    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> configure(@RequestBody GitConfiguration configuration) {
        GitLocalService.validateConfiguration(configuration);
        contextStore.getContext().setGitConfiguration(configuration);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "useGit", configuration.isUseGit(),
                "separateRepositories",
                configuration.isSeparateRepositories(),
                "createRemoteRepository",
                configuration.isCreateRemoteRepository()
        ));
    }
}
