
package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MetadataException.class)
    public ResponseEntity<Map<String, Object>> handleMetadataException(
            MetadataException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", 400,
                        "error", "Bad Request",
                        "message", exception.getMessage()
                ));
    }
}
