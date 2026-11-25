package org.example.siteback.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.siteback.dto.RestResponse;
import org.example.siteback.exception.InternalServerErrorException;
import org.example.siteback.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RestResponse<Void>> handleEntityNotFoundException(ResourceNotFoundException ex) {
        RestResponse<Void> response = RestResponse.buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
        log.info("Resource not found : {}", ex.getMessage(), ex);        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<RestResponse<Void>> handleInternalServerErrorException(InternalServerErrorException ex) {
        RestResponse<Void> response = RestResponse.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), null);
        log.error("An internal error occurred : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        RestResponse<Void> response = RestResponse.buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        log.error("Illegal argument provided : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RestResponse<Void>> handleDataIntegrityViolationException(
    DataIntegrityViolationException ex
    ) {
    RestResponse<Void> response = RestResponse.buildErrorResponse(
        HttpStatus.CONFLICT, "Constraint violation" , null);
        log.error("Database constraint violation: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Void>> handleGenericException(Exception ex) {
        RestResponse<Void> response = RestResponse.buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
        log.error("An unexpected error occurred : {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
