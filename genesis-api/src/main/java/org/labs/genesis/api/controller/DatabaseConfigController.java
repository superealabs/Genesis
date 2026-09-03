package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.DatabaseDtos.DatabaseConfigRequest;
import org.labs.genesis.api.dto.DatabaseDtos.DatabaseConfigResponse;
import org.labs.genesis.api.dto.DatabaseDtos.DatabaseResponse;
import org.labs.genesis.api.service.DatabaseConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/databases")
public class DatabaseConfigController {

    private final DatabaseConfigService databaseConfigService;

    public DatabaseConfigController(DatabaseConfigService databaseConfigService) {
        this.databaseConfigService = databaseConfigService;
    }

    @GetMapping
    public List<DatabaseResponse> getDatabases() {
        return databaseConfigService.getDatabases();
    }

    @PostMapping("/config")
    public DatabaseConfigResponse configure(@RequestBody DatabaseConfigRequest request) {
        return databaseConfigService.configure(request);
    }
}