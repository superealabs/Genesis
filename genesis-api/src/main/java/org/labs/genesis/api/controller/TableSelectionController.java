package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.MetadataDtos.*;
import org.labs.genesis.api.service.TableSelectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
public class TableSelectionController {

    private final TableSelectionService tableSelectionService;

    public TableSelectionController(TableSelectionService tableSelectionService) {
        this.tableSelectionService = tableSelectionService;
    }

    @PostMapping("/api/tables_metadata/select")
    public TableSelectionResponse select(@RequestBody TableSelectionRequest request) {
        return tableSelectionService.select(request);
    }

    @PostMapping("/api/generator/generation-parameters")
    public GenerationParametersResponse saveGenerationParameters(@RequestBody List<GenerationParameterRequest> parameters) {
        return tableSelectionService.saveGenerationParameters(parameters);
    }

    @GetMapping("/api/generator/generation-parameters")
    public List<GenerationParameterRequest> getGenerationParameters() {
        return tableSelectionService.getGenerationParameters();
    }

    @DeleteMapping("/api/generator/generation-parameters")
    public GenerationParametersResponse clearGenerationParameters() {
        tableSelectionService.clearGenerationParameters();
        return new GenerationParametersResponse(true, "Profils de génération réinitialisés");
    }
}
