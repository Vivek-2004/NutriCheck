package com.nutricheck.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("User Not Found")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(ScanNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleScanNotFound(ScanNotFoundException ex) {
        log.error("Scan not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Scan Not Found")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(AiProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleAiProcessing(AiProcessingException ex) {
        log.error("AI processing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("AI Processing Failed")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IngredientProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleIngredientProcessing(IngredientProcessingException ex) {
        log.error("Ingredient processing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Ingredient Processing Failed")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCategory(InvalidCategoryException ex) {
        log.error("Invalid category: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Invalid Category")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.error("File too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("File Too Large")
                        .message("Uploaded file exceeds the maximum allowed size")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Invalid Argument")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex) {
        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message("An unexpected error occurred")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateEmail(DuplicateEmailException ex) {
        log.error("Duplicate email: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)  // 409 Conflict
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("Duplicate Email")
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    // Also add this to catch DB constraint violations generically
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());

        // Check if it's specifically a duplicate email
        if (ex.getMessage().contains("users.UK") || ex.getMessage().contains("email")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponseDTO.builder()
                            .status(HttpStatus.CONFLICT.value())
                            .error("Duplicate Email")
                            .message("An account with this email already exists")
                            .timestamp(LocalDateTime.now())
                            .build());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponseDTO.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("Data Conflict")
                        .message("A record with this data already exists")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}