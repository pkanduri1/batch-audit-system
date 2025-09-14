package com.company.audit.exception;

/**
 * Base sealed class for all audit-related exceptions in the system.
 * 
 * This sealed class provides a type-safe hierarchy for audit exceptions using Java 17+ features.
 * All audit-specific exceptions must extend this base class, ensuring comprehensive error handling
 * and enabling pattern matching for exception handling scenarios.
 * 
 * The sealed class restricts inheritance to only the permitted subclasses, providing better
 * compile-time safety and enabling exhaustive pattern matching in switch expressions.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public sealed class AuditException extends RuntimeException 
    permits AuditPersistenceException, AuditValidationException, AuditConfigurationException, 
            AuditCorrelationException, AuditRetryException {

    /**
     * Constructs a new AuditException with the specified detail message.
     * 
     * @param message the detail message explaining the audit failure
     */
    public AuditException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuditException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the audit failure
     * @param cause the underlying cause of the audit failure
     */
    public AuditException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AuditException with the specified cause.
     * 
     * @param cause the underlying cause of the audit failure
     */
    public AuditException(Throwable cause) {
        super(cause);
    }

    /**
     * Gets the error code associated with this exception type.
     * Subclasses should override this method to provide specific error codes.
     * 
     * @return the error code for this exception type
     */
    public String getErrorCode() {
        return "AUDIT_ERROR";
    }

    /**
     * Gets the HTTP status code that should be returned for this exception.
     * Subclasses should override this method to provide appropriate HTTP status codes.
     * 
     * @return the HTTP status code for this exception type
     */
    public int getHttpStatusCode() {
        return 500; // Internal Server Error by default
    }

    /**
     * Determines if this exception should be retried.
     * Subclasses should override this method to indicate retry behavior.
     * 
     * @return true if the operation should be retried, false otherwise
     */
    public boolean isRetryable() {
        return false; // Not retryable by default
    }
}