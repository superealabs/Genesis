package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.DatabaseDtos.*;
import org.labs.genesis.api.service.DatabaseConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class DatabaseConfigController {

    private final DatabaseConfigService databaseConfigService;

    public DatabaseConfigController(DatabaseConfigService databaseConfigService) {
        this.databaseConfigService = databaseConfigService;
    }

    @GetMapping("/api/databases")
    public List<DatabaseResponse> getDatabases() {
        return databaseConfigService.getDatabases();
    }

    // Catalogue attendu par Genesis SDK Web
    @GetMapping("/api/database/engines")
    public List<DatabaseEngineResponse> getEngines() {
        return databaseConfigService.getEngines();
    }

    // Test JDBC sans sauvegarde du contexte
    @PostMapping("/api/database/test-connection")
    public DatabaseConnectionTestResponse testConnection(@RequestBody DatabaseConfigRequest request) {
        return databaseConfigService.testConnection(request);
    }

    // Ancienne et nouvelle route de sauvegarde
    @PostMapping({
            "/api/databases/config",
            "/api/generator/database-config"
    })
    public DatabaseConfigResponse configure(@RequestBody DatabaseConfigRequest request) {
        return databaseConfigService.configure(request);
    }
}
