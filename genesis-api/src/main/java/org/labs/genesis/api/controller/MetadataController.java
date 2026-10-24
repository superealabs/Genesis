package org.labs.genesis.api.controller;

import java.util.ArrayList;
import java.util.List;
import org.labs.genesis.api.dto.MetadataDtos.*;
import org.labs.genesis.api.dto.RelationDtos.*;
import org.labs.genesis.api.service.MetadataService;
import org.labs.genesis.api.service.ProfileRelationService;
import org.labs.genesis.api.service.RelationService;
import org.springframework.web.bind.annotation.*;

@RestController
public class MetadataController {

    private final MetadataService metadataService;

    private final RelationService relationService;

    private final ProfileRelationService profileRelationService;

    public MetadataController(MetadataService metadataService, RelationService relationService, ProfileRelationService profileRelationService) {
        this.metadataService = metadataService;
        this.relationService = relationService;
        this.profileRelationService = profileRelationService;
    }

    @GetMapping("/api/tables_metadata_loaded")
    public TablesMetadataResponse loadTablesMetadata() {
        return metadataService.loadAll();
    }

    // Nouveau contrat : liste unique attendue par le SDK.
    @GetMapping("/api/tables_metadata_all")
    public List<TableMetadataResponse> loadAllMetadata() {
        TablesMetadataResponse metadata = metadataService.loadAll();
        List<TableMetadataResponse> result = new ArrayList<>();
        result.addAll(metadata.tables());
        result.addAll(metadata.views());
        return List.copyOf(result);
    }

    @GetMapping("/api/tables_metadata/parents")
    public List<TableMetadataResponse> getParents() {
        return relationService.getParents();
    }

    @GetMapping("/api/tables_metadata/childs")
    public List<TableMetadataResponse> getChilds() {
        return relationService.getChildren();
    }

    @GetMapping("/api/relations")
    public List<RelationResponse> getRelations() {
        return relationService.getRelations();
    }

    @PostMapping("/api/relations/detect")
    public List<RelationResponse> detectRelations() {
        return relationService.detectRelations();
    }

    @PutMapping("/api/relations")
    public RelationUpdateResponse updateRelations(@RequestBody List<RelationUpdateRequest> requests) {
        return relationService.updateRelations(requests);
    }

    // Contrat Genesis SDK Web
    @PostMapping("/api/generator/relation-parameters")
    public RelationParametersResponse saveRelationParameters(@RequestBody RelationParametersRequest request) {
        return relationService.saveRelationParameters(request);
    }

    @GetMapping("/api/generator/profiles/{profileName}/relations")
    public List<RelationResponse> getProfileRelations(@PathVariable String profileName) {
        return profileRelationService.getRelations(profileName);
    }

    @PutMapping("/api/generator/profiles/{profileName}/relations")
    public RelationParametersResponse saveProfileRelations(@PathVariable String profileName, @RequestBody RelationParametersRequest request) {
        return profileRelationService.saveRelations(profileName, request);
    }

    @PostMapping("/api/tables_metadata/select")
    public TableSelectionResponse select(@RequestBody TableSelectionRequest request) {
        return metadataService.select(request);
    }
}
