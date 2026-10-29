package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.SqlScriptDtos.*;
import org.labs.genesis.api.service.SqlScriptService;
import org.springframework.web.bind.annotation.*;

@RestController
public class SqlController {

    private final SqlScriptService sqlScriptService;

    public SqlController(SqlScriptService sqlScriptService) {
        this.sqlScriptService = sqlScriptService;
    }

    @PostMapping("/api/generator/sql/validate")
    public SqlValidationResponse validate(@RequestBody SqlScriptRequest request) {
        return sqlScriptService.validate(request);
    }

    @PostMapping("/api/generator/sql/execute")
    public SqlExecutionResponse execute(@RequestBody SqlScriptRequest request) throws Exception {
        return sqlScriptService.execute(request);
    }

    @PostMapping("/api/generator/script-config")
    public ScriptConfigResponse save(@RequestBody ScriptConfigRequest request) {
        return sqlScriptService.saveScriptConfig(request);
    }
}
