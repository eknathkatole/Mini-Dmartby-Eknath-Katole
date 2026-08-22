package edu.demart_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                false, HttpStatus.NOT_FOUND.value(), "RESOURCE_NOT_FOUND",
                ex.getMessage(), request.getRequestURI(), LocalDateTime.now()
        ));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(new ErrorResponse(
                false, HttpStatus.BAD_REQUEST.value(), "BUSINESS_ERROR",
                ex.getMessage(), request.getRequestURI(), LocalDateTime.now()
        ));
    }

    /** Handles @Valid bean validation failures — returns all field errors joined as one message */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(
                false, HttpStatus.BAD_REQUEST.value(), "VALIDATION_ERROR",
                message, request.getRequestURI(), LocalDateTime.now()
        ));
    }

    /** 403 — user is authenticated but lacks the required role */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                false, HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED",
                "You do not have permission to access this resource", request.getRequestURI(), LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", request.getRequestURI(), LocalDateTime.now()
        ));
    }
}
