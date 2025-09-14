package com.company.audit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AuditException sealed class hierarchy.
 * Tests the Java 17+ sealed class functionality and exception behavior.
 */
class AuditExceptionTest {

    @Test
    void testAuditPersistenceException() {
        String message = "Database connection failed";
        Throwable cause = new RuntimeException("Connection timeout");
        
        AuditPersistenceException exception = new AuditPersistenceException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("AUDIT_PERSISTENCE_ERROR", exception.getErrorCode());
        assertEquals(500, exception.getHttpStatusCode());
        assertTrue(exception.isRetryable()); // Should be retryable for connection issues
    }

    @Test
    void testAuditValidationException() {
        String message = "Invalid correlation ID";
        String fieldName = "correlationId";
        Object invalidValue = "invalid-uuid";
        
        AuditValidationException exception = new AuditValidationException(message, fieldName, invalidValue);
        
        assertEquals(message, exception.getMessage());
        assertEquals(fieldName, exception.getFieldName());
        assertEquals(invalidValue, exception.getInvalidValue());
        assertEquals("AUDIT_VALIDATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getHttpStatusCode());
        assertFalse(exception.isRetryable()); // Validation errors are not retryable
    }

    @Test
    void testAuditConfigurationException() {
        String message = "Invalid database configuration";
        String configKey = "spring.datasource.url";
        String configValue = "invalid-url";
        
        AuditConfigurationException exception = new AuditConfigurationException(message, configKey, configValue);
        
        assertEquals(message, exception.getMessage());
        assertEquals(configKey, exception.getConfigurationKey());
        assertEquals(configValue, exception.getConfigurationValue());
        assertEquals("AUDIT_CONFIGURATION_ERROR", exception.getErrorCode());
        assertEquals(500, exception.getHttpStatusCode());
        assertFalse(exception.isRetryable()); // Configuration errors are not retryable
    }

    @Test
    void testAuditCorrelationException() {
        String message = "Correlation ID not found";
        String correlationId = "test-correlation-id";
        String operation = "getCurrentCorrelationId";
        
        AuditCorrelationException exception = new AuditCorrelationException(message, correlationId, operation);
        
        assertEquals(message, exception.getMessage());
        assertEquals(correlationId, exception.getCorrelationId());
        assertEquals(operation, exception.getOperation());
        assertEquals("AUDIT_CORRELATION_ERROR", exception.getErrorCode());
        assertEquals(400, exception.getHttpStatusCode());
        assertTrue(exception.isRetryable()); // Non-validation correlation issues may be retryable
    }

    @Test
    void testAuditCorrelationExceptionNotRetryableForValidation() {
        String message = "Invalid correlation ID format";
        String correlationId = "invalid-uuid";
        String operation = "validation";
        
        AuditCorrelationException exception = new AuditCorrelationException(message, correlationId, operation);
        
        assertFalse(exception.isRetryable()); // Validation operations are not retryable
    }

    @Test
    void testAuditRetryException() {
        String message = "Operation failed after retries";
        String operation = "saveAuditEvent";
        int attemptCount = 3;
        int maxAttempts = 3;
        long totalRetryDuration = 5000L;
        Throwable cause = new RuntimeException("Final failure");
        
        AuditRetryException exception = new AuditRetryException(
            message, operation, attemptCount, maxAttempts, totalRetryDuration, cause);
        
        assertTrue(exception.getMessage().contains(message));
        assertTrue(exception.getMessage().contains(operation));
        assertTrue(exception.getMessage().contains("3/3"));
        assertTrue(exception.getMessage().contains("5000ms"));
        assertEquals(operation, exception.getOperation());
        assertEquals(attemptCount, exception.getAttemptCount());
        assertEquals(maxAttempts, exception.getMaxAttempts());
        assertEquals(totalRetryDuration, exception.getTotalRetryDuration());
        assertEquals(cause, exception.getCause());
        assertEquals("AUDIT_RETRY_EXHAUSTED", exception.getErrorCode());
        assertEquals(503, exception.getHttpStatusCode());
        assertFalse(exception.isRetryable()); // Retry exceptions are not retryable by definition
    }

    @Test
    void testAuditPersistenceExceptionRetryableLogic() {
        // Test retryable scenarios
        AuditPersistenceException connectionException = new AuditPersistenceException(
            "Connection failed", new RuntimeException("connection timeout"));
        assertTrue(connectionException.isRetryable());

        AuditPersistenceException networkException = new AuditPersistenceException(
            "Network error", new RuntimeException("network unavailable"));
        assertTrue(networkException.isRetryable());

        AuditPersistenceException deadlockException = new AuditPersistenceException(
            "Deadlock detected", new RuntimeException("deadlock victim"));
        assertTrue(deadlockException.isRetryable());

        // Test non-retryable scenarios
        AuditPersistenceException constraintException = new AuditPersistenceException(
            "Constraint violation", new RuntimeException("unique constraint violated"));
        assertFalse(constraintException.isRetryable());

        AuditPersistenceException noCauseException = new AuditPersistenceException("Generic error");
        assertFalse(noCauseException.isRetryable());
    }

    @Test
    void testSealedClassExhaustivePatternMatching() {
        // Test that all sealed class subtypes can be handled in pattern matching
        AuditException[] exceptions = {
            new AuditPersistenceException("Persistence error"),
            new AuditValidationException("Validation error"),
            new AuditConfigurationException("Configuration error"),
            new AuditCorrelationException("Correlation error"),
            new AuditRetryException("Retry error")
        };

        for (AuditException exception : exceptions) {
            String errorCode;
            if (exception instanceof AuditPersistenceException ex) {
                errorCode = ex.getErrorCode();
            } else if (exception instanceof AuditValidationException ex) {
                errorCode = ex.getErrorCode();
            } else if (exception instanceof AuditConfigurationException ex) {
                errorCode = ex.getErrorCode();
            } else if (exception instanceof AuditCorrelationException ex) {
                errorCode = ex.getErrorCode();
            } else if (exception instanceof AuditRetryException ex) {
                errorCode = ex.getErrorCode();
            } else {
                errorCode = exception.getErrorCode();
            }
            
            assertNotNull(errorCode);
            assertTrue(errorCode.startsWith("AUDIT_"));
        }
    }

    @Test
    void testExceptionInheritanceHierarchy() {
        AuditPersistenceException persistenceEx = new AuditPersistenceException("Test");
        
        assertTrue(persistenceEx instanceof AuditException);
        assertTrue(persistenceEx instanceof RuntimeException);
        assertTrue(persistenceEx instanceof Exception);
        assertTrue(persistenceEx instanceof Throwable);
    }
}