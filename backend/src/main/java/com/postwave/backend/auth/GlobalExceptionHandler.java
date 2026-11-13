package com.postwave.backend.auth;

import com.postwave.backend.post.ImageStorageException;
import com.postwave.backend.post.PostAccessDeniedException;
import com.postwave.backend.post.PostNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiError> handlePostNotFound(PostNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiError("POST_NOT_FOUND", exception.getMessage(), Map.of())
        );
    }

    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<ApiError> handlePostAccessDenied(PostAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError("POST_ACCESS_DENIED", exception.getMessage(), Map.of())
        );
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ApiError> handleImageStorage(ImageStorageException exception) {
        return ResponseEntity.badRequest().body(
                new ApiError("INVALID_IMAGE", exception.getMessage(), Map.of())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            fieldErrors.putIfAbsent(field, violation.getMessage());
        });
        return ResponseEntity.badRequest().body(
                new ApiError("VALIDATION_FAILED", "Request validation failed", fieldErrors)
        );
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(
            DuplicateEmailException exception
    ) {
        ApiError error = new ApiError(
                "EMAIL_ALREADY_REGISTERED",
                exception.getMessage(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ApiError error = new ApiError(
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        ApiError error = new ApiError(
                "INVALID_CREDENTIALS",
                exception.getMessage(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }
}
