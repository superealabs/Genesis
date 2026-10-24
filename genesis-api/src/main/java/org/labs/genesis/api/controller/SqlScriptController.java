package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.SqlScriptDtos.*;
import org.labs.genesis.api.service.SqlScriptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generator/sql")
public class SqlScriptController {

    private final SqlScriptService sqlScriptService;

    public SqlScriptController(SqlScriptService sqlScriptService) {
        this.sqlScriptService = sqlScriptService;
    }

    @PostMapping("/validate")
    public SqlValidationResponse validate(@RequestBody SqlScriptRequest request) {
        return sqlScriptService.validate(request);
    }

    @PostMapping("/execute")
    public SqlExecutionResponse execute(@RequestBody SqlScriptRequest request) throws Exception {
        return sqlScriptService.execute(request);
    }
}
