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
import org.example.siteback.models.TNavbar;
import org.example.siteback.models.dto.TNavbarSearch;
import org.springframework.web.bind.annotation.*;
import org.example.siteback.services.interfaces.TNavbarService;
import org.example.siteback.services.interfaces.TContentService;
import org.example.siteback.models.dto.TContentSearch;
import org.example.siteback.models.TContent;

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
@RequestMapping("/tnavbars")
@Tag(name = "T navbar", description = "T navbar Management APIs")
public class TNavbarController  {
	private final TNavbarService tnavbarService;
private final TContentService tcontentService;


	public TNavbarController(TNavbarService tnavbarService, TContentService tcontentService) {
	   this.tnavbarService = tnavbarService;
	this.tcontentService = tcontentService;
	
	}

	@Operation(
	    summary = "Retrieve all t navbar",
	    description = "Get a paginated and sorted list of t navbar items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t navbar",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t navbar found"
	    )
	})
	@GetMapping
	public ResponseEntity<RestResponse<Page<TNavbar>>> getAllTNavbars(
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
	    Page<TNavbar> tnavbars = tnavbarService.getAllTNavbar(pageable);
	
	    String message = "t navbar retrieved successfully";
	
	    if(!tnavbars.hasContent()) {
	            message = "No t navbar found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TNavbar>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tnavbars
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Retrieve all t navbar",
	    description = "Get a paginated and sorted list of t navbar items. Returns an empty list if no data is found."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the paginated list of t navbar",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "204",
	        description = "No t navbar found"
	    )
	})
	@PostMapping("/search")
	public ResponseEntity<RestResponse<Page<TNavbar>>> getAllTNavbars(
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
	    @RequestBody TNavbarSearch object) {
	
	    Sort sortObj = WebUtils.createSortObject(sortParam);
	    Pageable pageable = PageRequest.of(page, size, sortObj);
	    Page<TNavbar> tnavbars = tnavbarService.getAllTNavbar(pageable, object);
	
	    String message = "t navbar retrieved successfully";
	
	    if(!tnavbars.hasContent()) {
	            message = "No t navbar found";
	            throw new ResourceNotFoundException(message);
	    }
	
	    RestResponse<Page<TNavbar>> response = RestResponse.buildSuccessResponse(
	            HttpStatus.OK,
	            message,
	            tnavbars
	    );
	    return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@Operation(
	    summary = "Get t navbar by ID",
	    description = "Retrieve a specific t navbar item by its ID"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "Successfully retrieved the t navbar",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T navbar not found with the provided ID"
	    )
	})
	@GetMapping("/{id}")
	public ResponseEntity<RestResponse<TNavbar>> getTNavbarById(
	    @Parameter(description = "ID of the t navbar to retrieve", required = true)
	    @PathVariable Integer id
	) {
	    TNavbar tnavbar = tnavbarService.getTNavbarById(id);
	    RestResponse<TNavbar> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t navbar retrieved successfully", tnavbar);
	    return ResponseEntity.ok(response);
	}
	@Operation(
	     summary = "Export T navbar to CSV",
	     description = "Generate a CSV file from a provided list of T navbar objects."
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
	public ResponseEntity<byte[]> exportTNavbarToCsv(@RequestBody List<TNavbar> tnavbar) {
	  
	  String csvContent = tnavbarService.exportTNavbarToCSV(tnavbar);
	  
	  return ResponseEntity.ok()
	   .header("Content-Disposition", "attachment; filename=data.csv")
	   .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
	   .body(csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
 

	@Operation(
	    summary = "Create new t navbar",
 
	    description = "Create a new t navbar item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "201",
	        description = "T navbar created successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t navbar supplied"
	    )
	})
	@PostMapping
	public ResponseEntity<?> createTNavbar(
	    @Parameter(description = "T navbar object to be created", required = true)
	    @RequestBody @Valid TNavbar tnavbar
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
	    TNavbar newTNavbar = tnavbarService.createTNavbar(tnavbar);
	    RestResponse<TNavbar> response = RestResponse.buildSuccessResponse(HttpStatus.CREATED,
	            "t navbar created successfully", newTNavbar);
	    return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

 
	@Operation(
	    summary = "Update existing t navbar",

	    description = "Update an existing t navbar item with the provided information"
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T navbar updated successfully",
	        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
	    ),
	    @ApiResponse(
	        responseCode = "404",
	        description = "T navbar not found with the provided ID"
	    ),
	    @ApiResponse(
	        responseCode = "400",
	        description = "Invalid t navbar supplied"
	    )
	})
	@PutMapping("/{id}")
	public ResponseEntity<?> updateTNavbar(
	    @Parameter(description = "ID of the t navbar to update", required = true)
	    @PathVariable Integer id,
	    @Parameter(description = "Updated t navbar object", required = true)
	    @RequestBody @Valid TNavbar tnavbar
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
	    TNavbar updateTNavbar = tnavbarService.updateTNavbar(id, tnavbar);
	    RestResponse<TNavbar> response = RestResponse.buildSuccessResponse(HttpStatus.OK,
	            "t navbar updated successfully", updateTNavbar);
	    return ResponseEntity.ok(response);
	}


	@Operation(
	    summary = "Delete t navbar",

	    description = "Delete a t navbar item by its ID. Returns success even if t navbar was already deleted."
	)
	@ApiResponses({
	    @ApiResponse(
	        responseCode = "200",
	        description = "T navbar deleted successfully or already deleted"
	    )
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<RestResponse<Void>> deleteTNavbarById(
	    @Parameter(description = "ID of the t navbar to delete", required = true)
	    @PathVariable Integer id
	) {
		try {
			tnavbarService.getTNavbarById(id);
			tnavbarService.deleteTNavbar(id);
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t navbar deleted successfully",
					null
				)
			);
		}catch (ResourceNotFoundException ex) {
			return ResponseEntity.ok(
				RestResponse.buildSuccessResponse(
					HttpStatus.OK,
					"t navbar already deleted or does not exist",
					null
				)
			);
		}
	}

@Operation(
  summary = "Get tContents attached with a tNavbar ID",
  description = "Retrieve a list of tContents from a tNavbar item ID"
)
@ApiResponses({
  @ApiResponse(
    responseCode = "200",
    description = "TContents retrieved successfully",
    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestResponse.class))
  ),
  @ApiResponse(
    responseCode = "404",
    description = "TNavbar not found with the provided ID"
  ),
  @ApiResponse(
    responseCode = "500",
    description = "Internal server error"
  )
})
@GetMapping("/{id}/tcontents")
public ResponseEntity<?> getTContentsByTNavbar(
  @Parameter(description = "ID of the tNavbar to retrieve", required = true)
  @PathVariable Integer id,
  @Parameter(description = "Page number (0-indexed)", required = false)
  @RequestParam(defaultValue = "0") int page,
  @Parameter(description = "Size of the page", required = false)
  @RequestParam(defaultValue = "10") int size,
  @Parameter(description = "Sort order", required = false)
  @RequestParam(defaultValue = "id,asc", required = false) String sortParam
) {
try {
  TNavbar tNavbar = tnavbarService.getTNavbarById(id);
  if (tNavbar == null) {
    RestResponse<String> errorResponse = RestResponse.buildErrorResponse(
      HttpStatus.NOT_FOUND,
      String.format("TNavbar not found with id: %s", id),
      null
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }
  Sort sortObj = WebUtils.createSortObject(sortParam);
  Pageable pageable = PageRequest.of(page, size, sortObj);
  TContentSearch object = new TContentSearch();
  object.setNavbaridTNavbar(tNavbar);
  
  Page<TContent> tContentsData = tcontentService.getAllTContent(pageable, object);
  
  RestResponse<Page<TContent>> response = RestResponse.buildSuccessResponse(
    HttpStatus.OK,
    "TContents retrieved successfully",
    tContentsData
  );
  return ResponseEntity.ok(response);
} catch (ResourceNotFoundException ex) {
  RestResponse<String> errorResponse = RestResponse.buildErrorResponse(
    HttpStatus.NOT_FOUND,
    ex.getMessage(),
    null
  );
  return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
} catch (Exception ex) {
  RestResponse<String> errorResponse = RestResponse.buildErrorResponse(
    HttpStatus.INTERNAL_SERVER_ERROR,
    String.format("Error while retrieving tNavbar: %s", ex.getMessage()),
    null
  );
  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
}
}


}
