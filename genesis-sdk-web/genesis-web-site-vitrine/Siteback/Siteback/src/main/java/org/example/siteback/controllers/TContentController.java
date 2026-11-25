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
import org.example.siteback.models.TContent;
import org.example.siteback.models.dto.TContentSearch;
import org.springframework.web.bind.annotation.*;
import org.example.siteback.services.interfaces.TContentService;
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
@RequestMapping("/tcontents")
@Tag(name = "T content", description = "T content Management APIs")
public class TContentController  {
	private final TContentService tcontentService;

	public TContentController(TContentService tcontentService) {
	   this.tcontentService = tcontentService;
	}

	@Operation(
	    summary = "Retrieve all t content",
	    description = "Get a paginated and sorted list of t content items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t content",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t content found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<TContent>>> getAllTContents(
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
	    Page<TContent> tcontents = tcontentService.getAllTContent(pageable);
	
	    String message = "t content retrieved successfully";
	
	    if(!tcontents.hasContent()) {
	            message = "No t content found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TContent>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tcontents
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all t content",
	    description = "Get a paginated and sorted list of t content items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t content",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t content found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<TContent>>> getAllTContents(
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
	    @RequestBody TContentSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<TContent> tcontents = tcontentService.getAllTContent(pageable, object);
	
	    String message = "t content retrieved successfully";
	
	    if(!tcontents.hasContent()) {
	            message = "No t content found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TContent>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tcontents
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get t content by ID",
	    description = "Retrieve a specific t content item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the t content",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T content not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<TContent>> getTContentById(
	    @Parameter(description = "ID of the t content to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    TContent tcontent = tcontentService.getTContentById(id);
	    RestResponse<TContent> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t content retrieved successfully", tcontent);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export T content to CSV",
	     description = "Generate a CSV file from a provided list of T content objects."
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
	public ResponseEntity<byte[]> exportTContentToCsv(@RequestBody List<TContent> tcontent) {
	  
	  String csvContent = tcontentService.exportTContentToCSV(tcontent);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new t content",
 
	    description = "Create a new t content item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "T content created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t content supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createTContent(
	    @Parameter(description = "T content object to be created", required = true)
	    @RequestBody @Valid TContent tcontent
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
	    TContent newTContent = tcontentService.createTContent(tcontent);
	    RestResponse<TContent> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "t content created successfully", newTContent);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing t content",

	    description = "Update an existing t content item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T content updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T content not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t content supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateTContent(
	    @Parameter(description = "ID of the t content to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated t content object", required = true)
	    @RequestBody @Valid TContent tcontent
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
	    TContent updateTContent = tcontentService.updateTContent(id, tcontent);
	    RestResponse<TContent> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t content updated successfully", updateTContent);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete t content",

	    description = "Delete a t content item by its ID. Returns success even if t content was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T content deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteTContentById(
	    @Parameter(description = "ID of the t content to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			tcontentService.getTContentById(id);
			tcontentService.deleteTContent(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t content deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t content already deleted or does not exist",
					null
				)
			);
		}
	}



}
