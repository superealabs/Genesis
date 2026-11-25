package org.example.siteback.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.example.siteback.dto.RestResponse;
import org.example.siteback.services.interfaces.ColumnVisibilityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config/columns")
@Tag(name = "Column Visibility", description = "APIs for managing global column visibility configurations")
public class ColumnVisibilityController {

    private final ColumnVisibilityService columnVisibilityService;

    public ColumnVisibilityController(ColumnVisibilityService columnVisibilityService) {
        this.columnVisibilityService = columnVisibilityService;
    }

    @Operation(
        summary = "Get visible fields for an entity and view type",
        description = "Retrieve the list of fields that should be visible for a specific entity and component type."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the field list")
    })
    @GetMapping("/{entity}/{componentType}")
    public ResponseEntity<RestResponse<List<String>>> getVisibleFields(
            @Parameter(description = "Name of the entity", example = "personne")
            @PathVariable String entity,
            
            @Parameter(description = "Type of view", example = "list")
            @PathVariable String componentType) {
        
        List<String> fields = columnVisibilityService.getVisibleFields(entity, componentType);
        
        // On enveloppe la liste dans un RestResponse pour que le frontend le reconnaisse
        RestResponse<List<String>> response = RestResponse.buildSuccessResponse(
            HttpStatus.OK,
            fields.isEmpty() ? "No configuration found, frontend should use defaults" : "Configuration retrieved successfully",
            fields
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update visible fields for an entity and view type",
        description = "Save a new configuration of visible fields for a specific entity and component type."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Configuration successfully saved"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PutMapping("/{entity}/{componentType}")
    public ResponseEntity<RestResponse<Void>> updateVisibleFields(
            @Parameter(description = "Name of the entity (e.g., 'personne')", example = "personne")
            @PathVariable String entity,
            
            @Parameter(description = "Type of view (e.g., 'list', 'detail')", example = "list")
            @PathVariable String componentType,
            
            @Parameter(description = "List of field keys to display", required = true)
            @RequestBody List<String> fields) {
        
        if (fields == null || fields.isEmpty()) {
            // Retourner une erreur formatée proprement
            RestResponse<Void> errorResponse = RestResponse.buildErrorResponse(
                HttpStatus.BAD_REQUEST, 
                "La liste des champs ne peut pas être vide", 
                null
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Sauvegarde effective
        columnVisibilityService.updateVisibleFields(entity, componentType, fields);
        
        // On renvoie un RestResponse de succès avec un corps valide
        RestResponse<Void> successResponse = RestResponse.buildSuccessResponse(
            HttpStatus.OK,
            "Configuration des colonnes mise à jour avec succès",
            null
        );
        
        return ResponseEntity.ok(successResponse);
    }
}
