package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.metadata.TablesMetadataResponse;
import org.labs.genesis.api.service.TableMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TableMetadataController {

    private final TableMetadataService tableMetadataService;

    public TableMetadataController(TableMetadataService tableMetadataService) {
        this.tableMetadataService = tableMetadataService;
    }

    @GetMapping("/tables_metadata_loaded")
    public TablesMetadataResponse loadTablesMetadata() {
        return tableMetadataService.loadAll();
    }
}
