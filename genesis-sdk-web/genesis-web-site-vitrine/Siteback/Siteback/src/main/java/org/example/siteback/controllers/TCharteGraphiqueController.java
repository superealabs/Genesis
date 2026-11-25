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
import org.example.siteback.models.TCharteGraphique;
import org.example.siteback.models.dto.TCharteGraphiqueSearch;
import org.springframework.web.bind.annotation.*;
import org.example.siteback.services.interfaces.TCharteGraphiqueService;
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
@RequestMapping("/tchartegraphiques")
@Tag(name = "T charte graphique", description = "T charte graphique Management APIs")
public class TCharteGraphiqueController  {
	private final TCharteGraphiqueService tchartegraphiqueService;

	public TCharteGraphiqueController(TCharteGraphiqueService tchartegraphiqueService) {
	   this.tchartegraphiqueService = tchartegraphiqueService;
	}

	@Operation(
	    summary = "Retrieve all t charte graphique",
	    description = "Get a paginated and sorted list of t charte graphique items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t charte graphique",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t charte graphique found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<TCharteGraphique>>> getAllTCharteGraphiques(
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
	    Page<TCharteGraphique> tchartegraphiques = tchartegraphiqueService.getAllTCharteGraphique(pageable);
	
	    String message = "t charte graphique retrieved successfully";
	
	    if(!tchartegraphiques.hasContent()) {
	            message = "No t charte graphique found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TCharteGraphique>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tchartegraphiques
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all t charte graphique",
	    description = "Get a paginated and sorted list of t charte graphique items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t charte graphique",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t charte graphique found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<TCharteGraphique>>> getAllTCharteGraphiques(
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
	    @RequestBody TCharteGraphiqueSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<TCharteGraphique> tchartegraphiques = tchartegraphiqueService.getAllTCharteGraphique(pageable, object);
	
	    String message = "t charte graphique retrieved successfully";
	
	    if(!tchartegraphiques.hasContent()) {
	            message = "No t charte graphique found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TCharteGraphique>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tchartegraphiques
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get t charte graphique by ID",
	    description = "Retrieve a specific t charte graphique item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the t charte graphique",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T charte graphique not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<TCharteGraphique>> getTCharteGraphiqueById(
	    @Parameter(description = "ID of the t charte graphique to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    TCharteGraphique tchartegraphique = tchartegraphiqueService.getTCharteGraphiqueById(id);
	    RestResponse<TCharteGraphique> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t charte graphique retrieved successfully", tchartegraphique);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export T charte graphique to CSV",
	     description = "Generate a CSV file from a provided list of T charte graphique objects."
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
	public ResponseEntity<byte[]> exportTCharteGraphiqueToCsv(@RequestBody List<TCharteGraphique> tchartegraphique) {
	  
	  String csvContent = tchartegraphiqueService.exportTCharteGraphiqueToCSV(tchartegraphique);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new t charte graphique",
 
	    description = "Create a new t charte graphique item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "T charte graphique created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t charte graphique supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createTCharteGraphique(
	    @Parameter(description = "T charte graphique object to be created", required = true)
	    @RequestBody @Valid TCharteGraphique tchartegraphique
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
	    TCharteGraphique newTCharteGraphique = tchartegraphiqueService.createTCharteGraphique(tchartegraphique);
	    RestResponse<TCharteGraphique> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "t charte graphique created successfully", newTCharteGraphique);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing t charte graphique",

	    description = "Update an existing t charte graphique item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T charte graphique updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T charte graphique not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t charte graphique supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateTCharteGraphique(
	    @Parameter(description = "ID of the t charte graphique to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated t charte graphique object", required = true)
	    @RequestBody @Valid TCharteGraphique tchartegraphique
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
	    TCharteGraphique updateTCharteGraphique = tchartegraphiqueService.updateTCharteGraphique(id, tchartegraphique);
	    RestResponse<TCharteGraphique> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t charte graphique updated successfully", updateTCharteGraphique);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete t charte graphique",

	    description = "Delete a t charte graphique item by its ID. Returns success even if t charte graphique was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T charte graphique deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteTCharteGraphiqueById(
	    @Parameter(description = "ID of the t charte graphique to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			tchartegraphiqueService.getTCharteGraphiqueById(id);
			tchartegraphiqueService.deleteTCharteGraphique(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t charte graphique deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t charte graphique already deleted or does not exist",
					null
				)
			);
		}
	}



}
