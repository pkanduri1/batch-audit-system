package com.company.audit.exception;

/**
 * Exception thrown when audit events cannot be persisted to or retrieved from the database.
 * 
 * This exception indicates database-related failures in the audit system, such as connection
 * issues, constraint violations, or SQL execution errors. It extends RuntimeException to
 * allow for graceful degradation where audit failures should not break the main business flow.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public class AuditPersistenceException extends RuntimeException {

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
}