package org.example.siteback.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.example.siteback.models.TEquipe;
import org.example.siteback.models.dto.TEquipeSearch;
import org.springframework.web.bind.annotation.*;
import org.example.siteback.services.interfaces.TEquipeService;
import org.example.siteback.utils.WebUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;

import org.example.siteback.dto.RestResponse;
import org.example.siteback.exception.ResourceNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/tequipes")
@Tag(name = "T equipe", description = "T equipe Management APIs")
public class TEquipeController  {
	private final TEquipeService tequipeService;

	public TEquipeController(TEquipeService tequipeService) {
	   this.tequipeService = tequipeService;
	}

	@Operation(
	    summary = "Retrieve all t equipe",
	    description = "Get a paginated and sorted list of t equipe items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t equipe",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t equipe found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<TEquipe>>> getAllTEquipes(
	    @Parameter(description = "Page number (0-indexed)", example = "0")
	    @RequestParam(defaultValue = "0") int page,
	
	    @Parameter(description = "Size of the page", example = "10")
	    @RequestParam(defaultValue = "10") int size,
	
	    @Parameter(
	        description = "Sorting criteria in the format: property,(asc|desc). "
	                + "Multiple sort criteria can be passed using semicolon separator. "
	                + "Example: id,asc;name,desc",
	        example = "id,asc"
	    )
	    @RequestParam(defaultValue = "id,asc") String sortParam) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<TEquipe> tequipes = tequipeService.getAllTEquipe(pageable);
	
	    String message = "t equipe retrieved successfully";
	
	    if(!tequipes.hasContent()) {
	            message = "No t equipe found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TEquipe>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tequipes
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all t equipe",
	    description = "Get a paginated and sorted list of t equipe items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t equipe",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t equipe found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<TEquipe>>> getAllTEquipes(
	    @Parameter(description = "Page number (0-indexed)", example = "0")
	    @RequestParam(defaultValue = "0") int page,
	
	    @Parameter(description = "Size of the page", example = "10")
	    @RequestParam(defaultValue = "10") int size,
	
	    @Parameter(
	        description = "Sorting criteria in the format: property,(asc|desc). "
	                + "Multiple sort criteria can be passed using semicolon separator. "
	                + "Example: id,asc;name,desc",
	        example = "id,asc"
	    )
	    @RequestParam(defaultValue = "id,asc") String sortParam,
	    @RequestBody TEquipeSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<TEquipe> tequipes = tequipeService.getAllTEquipe(pageable, object);
	
	    String message = "t equipe retrieved successfully";
	
	    if(!tequipes.hasContent()) {
	            message = "No t equipe found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TEquipe>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tequipes
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get t equipe by ID",
	    description = "Retrieve a specific t equipe item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the t equipe",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T equipe not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<TEquipe>> getTEquipeById(
	    @Parameter(description = "ID of the t equipe to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    TEquipe tequipe = tequipeService.getTEquipeById(id);
	    RestResponse<TEquipe> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t equipe retrieved successfully", tequipe);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export T equipe to CSV",
	     description = "Generate a CSV file from a provided list of T equipe objects."
	)
	@ApiResponses({
	   @ApiResponse(
	   responseCode = "200",
	   description = "CSV file successfully generated",
	   content = @Content(mediaType = "text/csv")
	   ),
	   @ApiResponse(
	   responseCode = "400",
	   description = "Invalid or empty list provided"
	   )
	})
	@PostMapping(value = "/export/csv", consumes = "application/json", produces = "text/csv")
	public ResponseEntity<byte[]> exportTEquipeToCsv(@RequestBody List<TEquipe> tequipe) {
	  
	  String csvContent = tequipeService.exportTEquipeToCSV(tequipe);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new t equipe",
 
	    description = "Create a new t equipe item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "T equipe created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t equipe supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createTEquipe(
	    @Parameter(description = "T equipe object to be created", required = true)
	    @RequestBody @Valid TEquipe tequipe
	    ,BindingResult bindingResult
	) {
	    if (bindingResult.hasErrors()) {
	       HashMap<String, String> errors = new HashMap<>();
	       bindingResult.getFieldErrors().forEach(error -> {
	           errors.put(error.getField(), error.getDefaultMessage());
	       });
	       RestResponse<HashMap<String, String>> errorResponse =
	       RestResponse.buildErrorResponse(HttpStatus.BAD_REQUEST,
	       "Validation failed", errors);
	
	       return ResponseEntity.badRequest().body(errorResponse);
	    }
	    TEquipe newTEquipe = tequipeService.createTEquipe(tequipe);
	    RestResponse<TEquipe> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "t equipe created successfully", newTEquipe);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing t equipe",

	    description = "Update an existing t equipe item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T equipe updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T equipe not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t equipe supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateTEquipe(
	    @Parameter(description = "ID of the t equipe to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated t equipe object", required = true)
	    @RequestBody @Valid TEquipe tequipe
	    ,BindingResult bindingResult
	) {
	    if (bindingResult.hasErrors()) {
	       HashMap<String, String> errors = new HashMap<>();
	       bindingResult.getFieldErrors().forEach(error -> {
	           errors.put(error.getField(), error.getDefaultMessage());
	       });
	       RestResponse<HashMap<String, String>> errorResponse =
	       RestResponse.buildErrorResponse(HttpStatus.BAD_REQUEST,
	       "Validation failed", errors);
	
	       return ResponseEntity.badRequest().body(errorResponse);
	    }
	    TEquipe updateTEquipe = tequipeService.updateTEquipe(id, tequipe);
	    RestResponse<TEquipe> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t equipe updated successfully", updateTEquipe);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete t equipe",

	    description = "Delete a t equipe item by its ID. Returns success even if t equipe was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T equipe deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteTEquipeById(
	    @Parameter(description = "ID of the t equipe to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			tequipeService.getTEquipeById(id);
			tequipeService.deleteTEquipe(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t equipe deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t equipe already deleted or does not exist",
					null
				)
			);
		}
	}



}
