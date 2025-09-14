package com.company.audit.exception;

/**
 * Exception thrown when correlation ID management fails.
 * 
 * This exception indicates failures in correlation ID operations, such as missing correlation IDs,
 * invalid correlation ID formats, correlation ID conflicts, or thread-local storage issues.
 * It extends AuditException and may be retryable depending on the specific failure scenario.
 * 
 * Common correlation scenarios:
 * - Missing correlation ID in thread-local storage
 * - Invalid UUID format for correlation ID
 * - Correlation ID conflicts or duplicates
 * - Thread-local storage cleanup failures
 * - Cross-thread correlation ID propagation issues
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public final class AuditCorrelationException extends AuditException {

    private final String correlationId;
    private final String operation;

    /**
     * Constructs a new AuditCorrelationException with the specified detail message.
     * 
     * @param message the detail message explaining the correlation failure
     */
    public AuditCorrelationException(String message) {
        super(message);
        this.correlationId = null;
        this.operation = null;
    }

    /**
     * Constructs a new AuditCorrelationException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the correlation failure
     * @param cause the underlying cause of the correlation failure
     */
    public AuditCorrelationException(String message, Throwable cause) {
        super(message, cause);
        this.correlationId = null;
        this.operation = null;
    }

    /**
     * Constructs a new AuditCorrelationException with correlation-specific information.
     * 
     * @param message the detail message explaining the correlation failure
     * @param correlationId the correlation ID involved in the failure
     * @param operation the operation that failed
     */
    public AuditCorrelationException(String message, String correlationId, String operation) {
        super(message);
        this.correlationId = correlationId;
        this.operation = operation;
    }

    /**
     * Constructs a new AuditCorrelationException with correlation-specific information and cause.
     * 
     * @param message the detail message explaining the correlation failure
     * @param correlationId the correlation ID involved in the failure
     * @param operation the operation that failed
     * @param cause the underlying cause of the correlation failure
     */
    public AuditCorrelationException(String message, String correlationId, String operation, Throwable cause) {
        super(message, cause);
        this.correlationId = correlationId;
        this.operation = operation;
    }

    /**
     * Gets the correlation ID involved in the failure.
     * 
     * @return the correlation ID, or null if not specified
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Gets the operation that failed.
     * 
     * @return the operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    @Override
    public String getErrorCode() {
        return "AUDIT_CORRELATION_ERROR";
    }

    @Override
    public int getHttpStatusCode() {
        return 400; // Bad Request
    }

    @Override
    public boolean isRetryable() {
        // Some correlation issues might be retryable (e.g., thread-local storage issues)
        // but format validation errors are not
        return operation != null && !operation.contains("validation");
    }
}