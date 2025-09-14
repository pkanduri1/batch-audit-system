package com.company.audit.exception;

/**
 * Exception thrown when audit events cannot be persisted to or retrieved from the database.
 * 
 * This exception indicates database-related failures in the audit system, such as connection
 * issues, constraint violations, or SQL execution errors. It extends AuditException to
 * allow for graceful degradation where audit failures should not break the main business flow.
 * 
 * This exception is typically retryable for transient database issues like connection timeouts
 * or temporary unavailability, but not for structural issues like constraint violations.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public final class AuditPersistenceException extends AuditException {

    /**
     * Constructs a new AuditPersistenceException with the specified detail message.
     * 
     * @param message the detail message explaining the persistence failure
     */
    public AuditPersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a new AuditPersistenceException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the persistence failure
     * @param cause the underlying cause of the persistence failure
     */
    public AuditPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new AuditPersistenceException with the specified cause.
     * 
     * @param cause the underlying cause of the persistence failure
     */
    public AuditPersistenceException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorCode() {
        return "AUDIT_PERSISTENCE_ERROR";
    }

    @Override
    public int getHttpStatusCode() {
        return 500; // Internal Server Error
    }

    @Override
    public boolean isRetryable() {
        // Determine if the exception is retryable based on the cause
        Throwable cause = getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                // Retryable database issues
                return causeMessage.contains("connection") ||
                       causeMessage.contains("timeout") ||
                       causeMessage.contains("network") ||
                       causeMessage.contains("unavailable") ||
                       causeMessage.contains("deadlock");
            }
        }
        return false; // Not retryable by default
    }
}