package com.company.audit.exception;

/**
 * Exception thrown when retry operations fail after exhausting all retry attempts.
 * 
 * This exception indicates that a retryable operation has failed multiple times and
 * all retry attempts have been exhausted. It extends AuditException and contains
 * information about the retry attempts and the final failure cause.
 * 
 * Common retry scenarios:
 * - Database connection failures after multiple retry attempts
 * - Network timeouts that persist across retries
 * - Transient service unavailability that doesn't resolve
 * - Resource contention that continues beyond retry window
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public final class AuditRetryException extends AuditException {

    private final int attemptCount;
    private final int maxAttempts;
    private final long totalRetryDuration;
    private final String operation;

    /**
     * Constructs a new AuditRetryException with the specified detail message.
     * 
     * @param message the detail message explaining the retry failure
     */
    public AuditRetryException(String message) {
        super(message);
        this.attemptCount = 0;
        this.maxAttempts = 0;
        this.totalRetryDuration = 0;
        this.operation = null;
    }

    /**
     * Constructs a new AuditRetryException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the retry failure
     * @param cause the underlying cause of the final retry failure
     */
    public AuditRetryException(String message, Throwable cause) {
        super(message, cause);
        this.attemptCount = 0;
        this.maxAttempts = 0;
        this.totalRetryDuration = 0;
        this.operation = null;
    }

    /**
     * Constructs a new AuditRetryException with retry-specific information.
     * 
     * @param message the detail message explaining the retry failure
     * @param operation the operation that failed after retries
     * @param attemptCount the number of attempts made
     * @param maxAttempts the maximum number of attempts allowed
     * @param totalRetryDuration the total time spent retrying (in milliseconds)
     * @param cause the underlying cause of the final retry failure
     */
    public AuditRetryException(String message, String operation, int attemptCount, int maxAttempts, 
                              long totalRetryDuration, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.attemptCount = attemptCount;
        this.maxAttempts = maxAttempts;
        this.totalRetryDuration = totalRetryDuration;
    }

    /**
     * Gets the number of attempts made before giving up.
     * 
     * @return the attempt count
     */
    public int getAttemptCount() {
        return attemptCount;
    }

    /**
     * Gets the maximum number of attempts that were allowed.
     * 
     * @return the maximum attempts
     */
    public int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Gets the total time spent retrying the operation.
     * 
     * @return the total retry duration in milliseconds
     */
    public long getTotalRetryDuration() {
        return totalRetryDuration;
    }

    /**
     * Gets the operation that failed after retries.
     * 
     * @return the operation name, or null if not specified
     */
    public String getOperation() {
        return operation;
    }

    @Override
    public String getErrorCode() {
        return "AUDIT_RETRY_EXHAUSTED";
    }

    @Override
    public int getHttpStatusCode() {
        return 503; // Service Unavailable
    }

    @Override
    public boolean isRetryable() {
        return false; // Retry exceptions are not retryable by definition
    }

    @Override
    public String getMessage() {
        String baseMessage = super.getMessage();
        if (operation != null && attemptCount > 0) {
            return String.format("%s (Operation: %s, Attempts: %d/%d, Duration: %dms)", 
                baseMessage, operation, attemptCount, maxAttempts, totalRetryDuration);
        }
        return baseMessage;
    }
}