package org.labs.genesis.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

import org.labs.genesis.api.dto.GenerationDtos.GenerationErrorResponse;
import org.labs.genesis.api.service.GenerationPublicationService;
import org.labs.genesis.api.service.GitLocalService.GitOperationException;
import java.io.IOException;
import org.labs.genesis.api.service.GeneratedFilesPublisher;

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

    @ExceptionHandler(GitOperationException.class)
    public ResponseEntity<GenerationErrorResponse> handleGit(GitOperationException exception) {
        String code = switch (exception.getStatusCode().value()) {
            case 400 -> "INVALID_GIT_CONFIGURATION";
            case 409 -> "GIT_CONFLICT";
            default -> "GIT_OPERATION_FAILED";
        };

        return ResponseEntity.status(exception.getStatusCode()).body(new GenerationErrorResponse(
                Instant.now(), exception.getStatusCode().value(), code, exception.getReason()));
    }

    @ExceptionHandler(GenerationAlreadyRunningException.class)
    public ResponseEntity<GenerationErrorResponse> handleRunning(GenerationAlreadyRunningException exception) {
        return generationError(
                HttpStatus.CONFLICT,
                "GENERATION_ALREADY_RUNNING",
                exception.getMessage()
        );
    }

    @ExceptionHandler(GenerationPublicationService.GenerationCollisionException.class)
    public ResponseEntity<GenerationErrorResponse> handleCollision(GenerationPublicationService.GenerationCollisionException exception) {
        return generationError(
                HttpStatus.CONFLICT,
                "GENERATED_FILE_COLLISION",
                "Des fichiers générés possèdent le même chemin relatif"
        );
    }

    @ExceptionHandler({
            RelationException.class,
            TableSelectionException.class,
            ProjectConfigException.class,
            GenerationModeException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<GenerationErrorResponse> handleConfiguration(RuntimeException exception) {
        return generationError(
                HttpStatus.BAD_REQUEST,
                "INVALID_GENERATION_CONFIGURATION",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<GenerationErrorResponse> handleIo(IOException exception) {
        // Ne pas exposer les chemins internes.
        return generationError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "GENERATION_IO_ERROR",
                "Une erreur de lecture ou d'écriture est survenue"
        );
    }

    private ResponseEntity<GenerationErrorResponse> generationError(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(
                new GenerationErrorResponse(
                        Instant.now(),
                        status.value(),
                        code,
                        message
                )
        );
    }

    @ExceptionHandler(GeneratedFilesPublisher.PartialPublicationException.class)
    public ResponseEntity<GenerationErrorResponse> handlePartialPublication(GeneratedFilesPublisher.PartialPublicationException exception
    ) {
        return generationError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "PARTIAL_PUBLICATION",
                "La publication a échoué. "
                        + exception.getCreatedFiles().size()
                        + " fichier(s) ont été créés avant l'erreur. "
                        + "Le projet cible doit être vérifié."
        );
    }
    
    @ExceptionHandler(DatabaseConfigException.class)
    public ResponseEntity<GenerationErrorResponse> handleDatabaseConfig(DatabaseConfigException exception) {
        return generationError(
                HttpStatus.BAD_REQUEST,
                exception.getCode(),
                exception.getMessage()
        );
    }
}
