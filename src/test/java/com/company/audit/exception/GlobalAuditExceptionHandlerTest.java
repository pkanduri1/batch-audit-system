package com.company.audit.exception;

import com.company.audit.model.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalAuditExceptionHandler.
 * Tests comprehensive error handling for all exception types.
 */
@ExtendWith(MockitoExtension.class)
class GlobalAuditExceptionHandlerTest {

    private GlobalAuditExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalAuditExceptionHandler();
        
        // Setup common request mock behavior
        when(request.getRequestURI()).thenReturn("/api/audit/events");
        when(request.getMethod()).thenReturn("GET");
    }

    @Test
    void testHandleAuditPersistenceException() {
        AuditPersistenceException exception = new AuditPersistenceException(
            "Database connection failed", new RuntimeException("Connection timeout"));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("AUDIT_PERSISTENCE_ERROR", errorResponse.getErrorCode());
        assertEquals("Database connection failed", errorResponse.getMessage());
        assertEquals("/api/audit/events", errorResponse.getPath());
        assertEquals("GET", errorResponse.getMethod());
        assertTrue(errorResponse.getRetryable());
        assertNotNull(errorResponse.getCorrelationId());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void testHandleAuditValidationException() {
        AuditValidationException exception = new AuditValidationException(
            "Invalid correlation ID", "correlationId", "invalid-uuid");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("AUDIT_VALIDATION_ERROR", errorResponse.getErrorCode());
        assertEquals("Invalid correlation ID", errorResponse.getMessage());
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(1, errorResponse.getFieldErrors().size());
        
        ErrorResponse.FieldError fieldError = errorResponse.getFieldErrors().get(0);
        assertEquals("correlationId", fieldError.getField());
        assertEquals("invalid-uuid", fieldError.getRejectedValue());
        assertEquals("Invalid correlation ID", fieldError.getMessage());
        assertEquals("VALIDATION_FAILED", fieldError.getCode());
    }

    @Test
    void testHandleAuditConfigurationException() {
        AuditConfigurationException exception = new AuditConfigurationException(
            "Invalid database URL", "spring.datasource.url", "invalid-url");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("AUDIT_CONFIGURATION_ERROR", errorResponse.getErrorCode());
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("spring.datasource.url", errorResponse.getDetails().get("configurationKey"));
        assertEquals("invalid-url", errorResponse.getDetails().get("configurationValue"));
    }

    @Test
    void testHandleAuditCorrelationException() {
        AuditCorrelationException exception = new AuditCorrelationException(
            "Correlation ID not found", "test-correlation-id", "getCurrentCorrelationId");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("AUDIT_CORRELATION_ERROR", errorResponse.getErrorCode());
        assertTrue(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("test-correlation-id", errorResponse.getDetails().get("correlationId"));
        assertEquals("getCurrentCorrelationId", errorResponse.getDetails().get("operation"));
    }

    @Test
    void testHandleAuditRetryException() {
        AuditRetryException exception = new AuditRetryException(
            "Operation failed after retries", "saveAuditEvent", 3, 3, 5000L, 
            new RuntimeException("Final failure"));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(503, errorResponse.getStatus());
        assertEquals("AUDIT_RETRY_EXHAUSTED", errorResponse.getErrorCode());
        assertFalse(errorResponse.getRetryable());
        assertEquals(60, errorResponse.getRetryAfter());
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("saveAuditEvent", errorResponse.getDetails().get("operation"));
        assertEquals(3, errorResponse.getDetails().get("attemptCount"));
        assertEquals(3, errorResponse.getDetails().get("maxAttempts"));
        assertEquals(5000L, errorResponse.getDetails().get("totalRetryDuration"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        
        FieldError fieldError = new FieldError("auditEvent", "correlationId", "invalid-uuid", 
            false, new String[]{"NotNull"}, null, "Correlation ID cannot be null");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("VALIDATION_ERROR", errorResponse.getErrorCode());
        assertEquals("Request validation failed", errorResponse.getMessage());
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(1, errorResponse.getFieldErrors().size());
        
        ErrorResponse.FieldError responseFieldError = errorResponse.getFieldErrors().get(0);
        assertEquals("correlationId", responseFieldError.getField());
        assertEquals("invalid-uuid", responseFieldError.getRejectedValue());
        assertEquals("Correlation ID cannot be null", responseFieldError.getMessage());
        assertEquals("NotNull", responseFieldError.getCode());
    }

    @Test
    void testHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(
            "correlationId", "String");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMissingParameterException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("MISSING_PARAMETER", errorResponse.getErrorCode());
        assertTrue(errorResponse.getMessage().contains("correlationId"));
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(1, errorResponse.getFieldErrors().size());
        
        ErrorResponse.FieldError fieldError = errorResponse.getFieldErrors().get(0);
        assertEquals("correlationId", fieldError.getField());
        assertNull(fieldError.getRejectedValue());
        assertEquals("Required parameter is missing", fieldError.getMessage());
        assertEquals("MISSING_PARAMETER", fieldError.getCode());
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException() {
        MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
        
        when(exception.getName()).thenReturn("page");
        when(exception.getValue()).thenReturn("invalid-number");
        when(exception.getRequiredType()).thenReturn((Class) Integer.class);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTypeMismatchException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("TYPE_MISMATCH", errorResponse.getErrorCode());
        assertTrue(errorResponse.getMessage().contains("page"));
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getFieldErrors());
        assertEquals(1, errorResponse.getFieldErrors().size());
        
        ErrorResponse.FieldError fieldError = errorResponse.getFieldErrors().get(0);
        assertEquals("page", fieldError.getField());
        assertEquals("invalid-number", fieldError.getRejectedValue());
        assertTrue(fieldError.getMessage().contains("Integer"));
        assertEquals("TYPE_MISMATCH", fieldError.getCode());
    }

    @Test
    void testHandleDateTimeParseException() {
        DateTimeParseException exception = new DateTimeParseException(
            "Invalid date format", "2024-13-01", 5);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDateTimeParseException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("DATETIME_PARSE_ERROR", errorResponse.getErrorCode());
        assertTrue(errorResponse.getMessage().contains("2024-13-01"));
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getDetails());
        assertEquals("2024-13-01", errorResponse.getDetails().get("parsedString"));
        assertEquals(5, errorResponse.getDetails().get("errorIndex"));
        assertTrue(errorResponse.getDetails().get("expectedFormat").toString().contains("ISO 8601"));
    }

    @Test
    void testHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
        when(exception.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleHttpMessageNotReadableException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("MALFORMED_JSON", errorResponse.getErrorCode());
        assertEquals("Malformed JSON in request body", errorResponse.getMessage());
        assertFalse(errorResponse.getRetryable());
    }

    @Test
    void testHandleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException(
            "POST", Set.of("GET", "PUT"));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupportedException(exception, request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(405, errorResponse.getStatus());
        assertEquals("METHOD_NOT_ALLOWED", errorResponse.getErrorCode());
        assertTrue(errorResponse.getMessage().contains("POST"));
        assertFalse(errorResponse.getRetryable());
        
        assertNotNull(errorResponse.getDetails());
        assertNotNull(errorResponse.getDetails().get("supportedMethods"));
    }

    @Test
    void testHandleDataAccessException() {
        DataAccessException exception = new DataAccessException("Database connection failed") {};

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDatabaseException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("DATABASE_ERROR", errorResponse.getErrorCode());
        assertEquals("Database operation failed", errorResponse.getMessage());
        assertTrue(errorResponse.getRetryable()); // Connection failures are retryable
        assertEquals(30, errorResponse.getRetryAfter());
    }

    @Test
    void testHandleSQLException() {
        SQLException exception = new SQLException("ORA-12170: TNS:Connect timeout occurred");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDatabaseException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("DATABASE_ERROR", errorResponse.getErrorCode());
        assertTrue(errorResponse.getRetryable()); // Timeout errors are retryable
        assertEquals(30, errorResponse.getRetryAfter());
    }

    @Test
    void testHandleGenericException() {
        RuntimeException exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ErrorResponse errorResponse = response.getBody();
        assertEquals(500, errorResponse.getStatus());
        assertEquals("INTERNAL_ERROR", errorResponse.getErrorCode());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
        assertFalse(errorResponse.getRetryable());
        assertNotNull(errorResponse.getCorrelationId());
    }

    @Test
    void testErrorResponseStructure() {
        AuditValidationException exception = new AuditValidationException("Test validation error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuditException(exception, request);

        assertNotNull(response.getBody());
        ErrorResponse errorResponse = response.getBody();
        
        // Verify all required fields are present
        assertNotNull(errorResponse.getTimestamp());
        assertTrue(errorResponse.getStatus() > 0);
        assertNotNull(errorResponse.getError());
        assertNotNull(errorResponse.getErrorCode());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getPath());
        assertNotNull(errorResponse.getMethod());
        assertNotNull(errorResponse.getCorrelationId());
        assertNotNull(errorResponse.getRetryable());
    }
}