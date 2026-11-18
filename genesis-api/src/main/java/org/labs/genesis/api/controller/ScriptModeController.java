package org.labs.genesis.api.controller;

import java.util.List;

import org.labs.genesis.api.dto.RelationDtos.RelationResponse;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptDatabaseConfigRequest;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptGenerationRequest;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptGenerationResponse;
import org.labs.genesis.api.dto.ScriptModeDtos.ScriptTablesResponse;
import org.labs.genesis.api.service.ScriptModeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Génération sans base de données (à partir d'un script SQL).
 *
 * Ordre d'appel :
 *   1. POST /api/generator/frameworks/{id}/select
 *   2. POST /api/generator/script-database-config   { databaseId, script }
 *   3. POST /api/generator/project-config           (route existante)
 *   4. POST /api/generator/frontend-config          (optionnel, route existante)
 *   5. GET  /api/generator/script/relations         (optionnel : relations détectées)
 *   6. POST /api/generator/script/launch            { selectedTables, selectedViews, selectedComponents, relations }
 */
@RestController
public class ScriptModeController {

    private final ScriptModeService scriptModeService;

    public ScriptModeController(ScriptModeService scriptModeService) {
        this.scriptModeService = scriptModeService;
    }

    @PostMapping("/api/generator/script-database-config")
    public ScriptTablesResponse configure(@RequestBody ScriptDatabaseConfigRequest request) {
        return scriptModeService.configure(request);
    }

    @GetMapping("/api/generator/script/tables")
    public ScriptTablesResponse tables() {
        return scriptModeService.tables();
    }

    @GetMapping("/api/generator/script/relations")
    public List<RelationResponse> relations() {
        return scriptModeService.relations();
    }

    @PostMapping("/api/generator/script/launch")
    public ScriptGenerationResponse launch(@RequestBody(required = false) ScriptGenerationRequest request) {
        return scriptModeService.launch(request);
    }
}
