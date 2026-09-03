package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.metadata.TableSelectionRequest;
import org.labs.genesis.api.dto.metadata.TableSelectionResponse;
import org.labs.genesis.api.service.TableSelectionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tables_metadata")
public class TableSelectionController {

    private final TableSelectionService tableSelectionService;

    public TableSelectionController(TableSelectionService tableSelectionService) {
        this.tableSelectionService = tableSelectionService;
    }

    @PostMapping("/select")
    public TableSelectionResponse select(@RequestBody TableSelectionRequest request) {
        return tableSelectionService.select(request);
    }
}
