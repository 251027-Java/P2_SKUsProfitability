package com.p2.product_service.exception;

import com.p2.product_service.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private ErrorResponse buildErrorResponse(HttpStatus status, String error, String message) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setError(error);
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        return errorResponse;
    }

    @ExceptionHandler(BrightDataException.class)
    public ResponseEntity<ErrorResponse> handleBrightDataApiExceptions(BrightDataException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage()));
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse response = buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
        if (ex.getResourceType() != null) {
            response.setResourceType(ex.getResourceType());
        }
        if (ex.getResourceId() != null) {
            response.setResourceId(ex.getResourceId());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred"));
    }
}

