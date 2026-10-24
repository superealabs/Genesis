package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.SqlScriptDtos.*;
import org.labs.genesis.api.service.SqlScriptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator")
public class ScriptConfigController {

    private final SqlScriptService service;

    public ScriptConfigController(SqlScriptService service) {
        this.service = service;
    }

    @PostMapping("/script-config")
    public ScriptConfigResponse save(@RequestBody ScriptConfigRequest request) {
        return service.saveScriptConfig(request);
    }
}
