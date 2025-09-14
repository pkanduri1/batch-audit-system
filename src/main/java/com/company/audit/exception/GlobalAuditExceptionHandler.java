package com.company.audit.exception;

import com.company.audit.model.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for audit REST API endpoints.
 * 
 * This class provides centralized exception handling for all audit-related REST API endpoints,
 * ensuring consistent error responses with proper HTTP status codes, error messages, and
 * SpringDoc OpenAPI v2 documentation. It handles both audit-specific exceptions and
 * common Spring framework exceptions.
 * 
 * Features:
 * - Consistent error response format across all endpoints
 * - Proper HTTP status code mapping for different exception types
 * - Field-specific validation error details
 * - Request correlation ID tracking for debugging
 * - Retry guidance for transient failures
 * - Comprehensive logging for troubleshooting
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@RestControllerAdvice
@Hidden // Hide from Swagger documentation
public class GlobalAuditExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalAuditExceptionHandler.class);

    /**
     * Handles audit-specific exceptions using Java 17+ pattern matching.
     * 
     * @param ex the audit exception
     * @param request the HTTP request
     * @return error response with appropriate status and details
     */
    @ExceptionHandler(AuditException.class)
    public ResponseEntity<ErrorResponse> handleAuditException(AuditException ex, HttpServletRequest request) {
        logger.error("Audit exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse;
        
        if (ex instanceof AuditPersistenceException persistenceEx) {
            errorResponse = createErrorResponse(
                persistenceEx.getHttpStatusCode(),
                HttpStatus.valueOf(persistenceEx.getHttpStatusCode()).getReasonPhrase(),
                persistenceEx.getErrorCode(),
                persistenceEx.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
        } else if (ex instanceof AuditValidationException validationEx) {
            errorResponse = createErrorResponse(
                validationEx.getHttpStatusCode(),
                HttpStatus.valueOf(validationEx.getHttpStatusCode()).getReasonPhrase(),
                validationEx.getErrorCode(),
                validationEx.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
            
            // Add field-specific error if available
            if (validationEx.getFieldName() != null) {
                List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
                fieldErrors.add(new ErrorResponse.FieldError(
                    validationEx.getFieldName(),
                    validationEx.getInvalidValue(),
                    validationEx.getMessage(),
                    "VALIDATION_FAILED"
                ));
                errorResponse.setFieldErrors(fieldErrors);
            }
        } else if (ex instanceof AuditConfigurationException configEx) {
            errorResponse = createErrorResponse(
                configEx.getHttpStatusCode(),
                HttpStatus.valueOf(configEx.getHttpStatusCode()).getReasonPhrase(),
                configEx.getErrorCode(),
                configEx.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
            
            // Add configuration details if available
            if (configEx.getConfigurationKey() != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("configurationKey", configEx.getConfigurationKey());
                details.put("configurationValue", configEx.getConfigurationValue());
                errorResponse.setDetails(details);
            }
        } else if (ex instanceof AuditCorrelationException correlationEx) {
            errorResponse = createErrorResponse(
                correlationEx.getHttpStatusCode(),
                HttpStatus.valueOf(correlationEx.getHttpStatusCode()).getReasonPhrase(),
                correlationEx.getErrorCode(),
                correlationEx.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
            
            // Add correlation details if available
            if (correlationEx.getCorrelationId() != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("correlationId", correlationEx.getCorrelationId());
                details.put("operation", correlationEx.getOperation());
                errorResponse.setDetails(details);
            }
        } else if (ex instanceof AuditRetryException retryEx) {
            errorResponse = createErrorResponse(
                retryEx.getHttpStatusCode(),
                HttpStatus.valueOf(retryEx.getHttpStatusCode()).getReasonPhrase(),
                retryEx.getErrorCode(),
                retryEx.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
            
            // Add retry details
            Map<String, Object> details = new HashMap<>();
            details.put("operation", retryEx.getOperation());
            details.put("attemptCount", retryEx.getAttemptCount());
            details.put("maxAttempts", retryEx.getMaxAttempts());
            details.put("totalRetryDuration", retryEx.getTotalRetryDuration());
            errorResponse.setDetails(details);
            errorResponse.setRetryAfter(60); // Suggest retry after 60 seconds
        } else {
            // Fallback for any other AuditException subtype
            errorResponse = createErrorResponse(
                ex.getHttpStatusCode(),
                HttpStatus.valueOf(ex.getHttpStatusCode()).getReasonPhrase(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                request.getMethod()
            );
        }

        errorResponse.setRetryable(ex.isRetryable());
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.status(ex.getHttpStatusCode()).body(errorResponse);
    }

    /**
     * Handles validation exceptions from Spring framework.
     * 
     * @param ex the validation exception
     * @param request the HTTP request
     * @return error response with validation details
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex, HttpServletRequest request) {
        logger.warn("Validation exception occurred: {}", ex.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            for (FieldError fieldError : validationEx.getBindingResult().getFieldErrors()) {
                fieldErrors.add(new ErrorResponse.FieldError(
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage(),
                    fieldError.getCode()
                ));
            }
        } else if (ex instanceof BindException bindEx) {
            for (FieldError fieldError : bindEx.getBindingResult().getFieldErrors()) {
                fieldErrors.add(new ErrorResponse.FieldError(
                    fieldError.getField(),
                    fieldError.getRejectedValue(),
                    fieldError.getDefaultMessage(),
                    fieldError.getCode()
                ));
            }
        }

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "VALIDATION_ERROR",
            "Request validation failed",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setFieldErrors(fieldErrors);
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles missing request parameter exceptions.
     * 
     * @param ex the missing parameter exception
     * @param request the HTTP request
     * @return error response with parameter details
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        logger.warn("Missing request parameter: {}", ex.getParameterName());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorResponse.FieldError(
            ex.getParameterName(),
            null,
            "Required parameter is missing",
            "MISSING_PARAMETER"
        ));

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "MISSING_PARAMETER",
            "Required request parameter '" + ex.getParameterName() + "' is missing",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setFieldErrors(fieldErrors);
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles method argument type mismatch exceptions.
     * 
     * @param ex the type mismatch exception
     * @param request the HTTP request
     * @return error response with type details
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        logger.warn("Method argument type mismatch: {} for parameter {}", ex.getValue(), ex.getName());

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new ErrorResponse.FieldError(
            ex.getName(),
            ex.getValue(),
            "Invalid parameter type. Expected: " + ex.getRequiredType().getSimpleName(),
            "TYPE_MISMATCH"
        ));

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "TYPE_MISMATCH",
            "Invalid parameter type for '" + ex.getName() + "'",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setFieldErrors(fieldErrors);
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles date/time parsing exceptions.
     * 
     * @param ex the date parsing exception
     * @param request the HTTP request
     * @return error response with parsing details
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateTimeParseException(
            DateTimeParseException ex, HttpServletRequest request) {
        logger.warn("Date/time parsing exception: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "DATETIME_PARSE_ERROR",
            "Invalid date/time format: " + ex.getParsedString(),
            request.getRequestURI(),
            request.getMethod()
        );
        
        Map<String, Object> details = new HashMap<>();
        details.put("parsedString", ex.getParsedString());
        details.put("errorIndex", ex.getErrorIndex());
        details.put("expectedFormat", "ISO 8601 format (e.g., 2024-01-15T10:30:00)");
        errorResponse.setDetails(details);
        
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles HTTP message not readable exceptions (malformed JSON).
     * 
     * @param ex the message not readable exception
     * @param request the HTTP request
     * @return error response with JSON parsing details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        logger.warn("HTTP message not readable: {}", ex.getMessage());

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "MALFORMED_JSON",
            "Malformed JSON in request body",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handles HTTP method not supported exceptions.
     * 
     * @param ex the method not supported exception
     * @param request the HTTP request
     * @return error response with method details
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        logger.warn("HTTP method not supported: {} for {}", ex.getMethod(), request.getRequestURI());

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
            "METHOD_NOT_ALLOWED",
            "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
            request.getRequestURI(),
            request.getMethod()
        );
        
        Map<String, Object> details = new HashMap<>();
        details.put("supportedMethods", ex.getSupportedMethods());
        errorResponse.setDetails(details);
        
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * Handles no handler found exceptions (404 errors).
     * 
     * @param ex the no handler found exception
     * @param request the HTTP request
     * @return error response with endpoint details
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "ENDPOINT_NOT_FOUND",
            "No endpoint found for " + ex.getHttpMethod() + " " + ex.getRequestURL(),
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.notFound().build();
    }

    /**
     * Handles database-related exceptions.
     * 
     * @param ex the database exception
     * @param request the HTTP request
     * @return error response with database details
     */
    @ExceptionHandler({DataAccessException.class, SQLException.class})
    public ResponseEntity<ErrorResponse> handleDatabaseException(Exception ex, HttpServletRequest request) {
        logger.error("Database exception occurred: {}", ex.getMessage(), ex);

        boolean isRetryable = isRetryableDatabaseException(ex);
        
        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "DATABASE_ERROR",
            "Database operation failed",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setRetryable(isRetryable);
        if (isRetryable) {
            errorResponse.setRetryAfter(30); // Suggest retry after 30 seconds
        }
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     * 
     * @param ex the unexpected exception
     * @param request the HTTP request
     * @return generic error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            request.getMethod()
        );
        
        errorResponse.setRetryable(false);
        errorResponse.setCorrelationId(generateRequestCorrelationId());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Creates a standardized error response.
     * 
     * @param status HTTP status code
     * @param error HTTP status text
     * @param errorCode application-specific error code
     * @param message error message
     * @param path request path
     * @param method HTTP method
     * @return error response object
     */
    private ErrorResponse createErrorResponse(int status, String error, String errorCode, 
                                            String message, String path, String method) {
        return new ErrorResponse(status, error, errorCode, message, path, method);
    }

    /**
     * Generates a correlation ID for request tracking.
     * 
     * @return correlation ID string
     */
    private String generateRequestCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Determines if a database exception is retryable.
     * 
     * @param ex the database exception
     * @return true if retryable, false otherwise
     */
    private boolean isRetryableDatabaseException(Exception ex) {
        String message = ex.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();
            return lowerMessage.contains("connection") ||
                   lowerMessage.contains("timeout") ||
                   lowerMessage.contains("network") ||
                   lowerMessage.contains("unavailable") ||
                   lowerMessage.contains("deadlock") ||
                   lowerMessage.contains("lock wait timeout");
        }
        return false;
    }
}