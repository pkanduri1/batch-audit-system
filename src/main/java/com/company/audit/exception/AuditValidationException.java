package com.company.audit.exception;

/**
 * Exception thrown when audit event validation fails.
 * 
 * This exception indicates validation failures in audit data, such as missing required fields,
 * invalid field values, constraint violations, or business rule violations. It extends AuditException
 * and is typically not retryable since validation errors require data correction.
 * 
 * Common validation scenarios:
 * - Missing correlation ID or audit ID
 * - Invalid enum values for status or checkpoint stage
 * - Null or empty required fields
 * - Invalid date ranges or timestamps
 * - Business rule violations
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public final class AuditValidationException extends AuditException {

    private final String fieldName;
    private final Object invalidValue;

    /**
     * Constructs a new AuditValidationException with the specified detail message.
     * 
     * @param message the detail message explaining the validation failure
     */
    public AuditValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.invalidValue = null;
    }

    /**
     * Constructs a new AuditValidationException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the validation failure
     * @param cause the underlying cause of the validation failure
     */
    public AuditValidationException(String message, Throwable cause) {
        super(message, cause);
        this.fieldName = null;
        this.invalidValue = null;
    }

    /**
     * Constructs a new AuditValidationException with field-specific information.
     * 
     * @param message the detail message explaining the validation failure
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value that caused the validation failure
     */
    public AuditValidationException(String message, String fieldName, Object invalidValue) {
        super(message);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Constructs a new AuditValidationException with field-specific information and cause.
     * 
     * @param message the detail message explaining the validation failure
     * @param fieldName the name of the field that failed validation
     * @param invalidValue the invalid value that caused the validation failure
     * @param cause the underlying cause of the validation failure
     */
    public AuditValidationException(String message, String fieldName, Object invalidValue, Throwable cause) {
        super(message, cause);
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    /**
     * Gets the name of the field that failed validation.
     * 
     * @return the field name, or null if not specified
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Gets the invalid value that caused the validation failure.
     * 
     * @return the invalid value, or null if not specified
     */
    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public String getErrorCode() {
        return "AUDIT_VALIDATION_ERROR";
    }

    @Override
    public int getHttpStatusCode() {
        return 400; // Bad Request
    }

    @Override
    public boolean isRetryable() {
        return false; // Validation errors are not retryable
    }
}