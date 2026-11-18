package org.labs.genesis.api.controller;

import java.util.List;

import org.labs.genesis.api.dto.ScriptAiDtos.GenerateSqlRequest;
import org.labs.genesis.api.dto.ScriptAiDtos.GenerateSqlResponse;
import org.labs.genesis.api.dto.ScriptAiDtos.LlmModelResponse;
import org.labs.genesis.api.service.ScriptAiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScriptAiController {

    private final ScriptAiService scriptAiService;

    public ScriptAiController(ScriptAiService scriptAiService) {
        this.scriptAiService = scriptAiService;
    }

    @GetMapping("/api/generator/llm-models")
    public List<LlmModelResponse> models() {
        return scriptAiService.models();
    }

    @PostMapping("/api/generator/script/generate-sql")
    public GenerateSqlResponse generate(@RequestBody GenerateSqlRequest request) {
        return scriptAiService.generate(request);
    }
}