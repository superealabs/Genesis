package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.metadata.TableMetadataResponse;
import org.labs.genesis.api.dto.relation.RelationResponse;
import org.labs.genesis.api.dto.relation.RelationUpdateRequest;
import org.labs.genesis.api.dto.relation.RelationUpdateResponse;
import org.labs.genesis.api.service.RelationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RelationController {

    private final RelationService relationService;

    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @GetMapping("/tables_metadata/parents")
    public List<TableMetadataResponse> getParents() {
        return relationService.getParents();
    }

    @GetMapping("/tables_metadata/childs")
    public List<TableMetadataResponse> getChilds() {
        return relationService.getChilds();
    }

    @GetMapping("/relations")
    public List<RelationResponse> getRelations() {
        return relationService.getRelations();
    }

    @PutMapping("/relations")
    public RelationUpdateResponse updateRelations(@RequestBody List<RelationUpdateRequest> requests) {
        return relationService.updateRelations(requests);
    }
}
