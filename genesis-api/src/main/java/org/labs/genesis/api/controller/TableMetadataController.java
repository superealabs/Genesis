package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.MetadataDtos.TablesMetadataResponse;
import org.labs.genesis.api.service.TableMetadataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

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

    // Nouveau contrat : liste unique attendue par le SDK.
    @GetMapping("/tables_metadata_all")
    public List<TableMetadataResponse> loadAllMetadata() {
        TablesMetadataResponse metadata = tableMetadataService.loadAll();
        List<TableMetadataResponse> result = new ArrayList<>();
        result.addAll(metadata.tables());
        result.addAll(metadata.views());
        return List.copyOf(result);
    }
}
