package org.labs.genesis.api.controller;

import org.labs.genesis.api.dto.MetadataDtos.TableMetadataResponse;
import org.labs.genesis.api.dto.RelationDtos.*;
import org.labs.genesis.api.service.RelationService;
import org.labs.genesis.api.service.ProfileRelationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RelationController {

    private final RelationService relationService;
    private final ProfileRelationService profileRelationService;


    public RelationController(RelationService relationService, ProfileRelationService profileRelationService) {
        this.relationService = relationService;
        this.profileRelationService = profileRelationService;
    }


    @GetMapping("/tables_metadata/parents")
    public List<TableMetadataResponse> getParents() {
        return relationService.getParents();
    }

    @GetMapping("/tables_metadata/childs")
    public List<TableMetadataResponse> getChilds() {
        return relationService.getChildren();
    }

    @GetMapping("/relations")
    public List<RelationResponse> getRelations() {
        return relationService.getRelations();
    }

    @PostMapping("/relations/detect")
    public List<RelationResponse> detectRelations() {
        return relationService.detectRelations();
    }

    @PutMapping("/relations")
    public RelationUpdateResponse updateRelations(@RequestBody List<RelationUpdateRequest> requests) {
        return relationService.updateRelations(requests);
    }

    // Contrat Genesis SDK Web
    @PostMapping("/generator/relation-parameters")
    public RelationParametersResponse saveRelationParameters(@RequestBody RelationParametersRequest request) {
        return relationService.saveRelationParameters(request);
    }


    @GetMapping("/generator/profiles/{profileName}/relations")
    public List<RelationResponse> getProfileRelations(@PathVariable String profileName) {
        return profileRelationService.getRelations(profileName);
    }

    @PutMapping("/generator/profiles/{profileName}/relations")
    public RelationParametersResponse saveProfileRelations(@PathVariable String profileName, @RequestBody RelationParametersRequest request) {
        return profileRelationService.saveRelations(profileName, request);
    }

}
