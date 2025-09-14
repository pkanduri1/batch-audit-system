package com.company.audit.exception;

/**
 * Exception thrown when audit system configuration is invalid or missing.
 * 
 * This exception indicates configuration-related failures in the audit system, such as
 * missing database connection properties, invalid configuration values, or misconfigured
 * audit settings. It extends AuditException and is typically not retryable since
 * configuration errors require system administration intervention.
 * 
 * Common configuration scenarios:
 * - Missing or invalid database connection properties
 * - Invalid audit retention policies
 * - Misconfigured security settings
 * - Invalid batch processing parameters
 * - Missing required configuration files
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public final class AuditConfigurationException extends AuditException {

    private final String configurationKey;
    private final String configurationValue;

    /**
     * Constructs a new AuditConfigurationException with the specified detail message.
     * 
     * @param message the detail message explaining the configuration failure
     */
    public AuditConfigurationException(String message) {
        super(message);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    /**
     * Constructs a new AuditConfigurationException with the specified detail message and cause.
     * 
     * @param message the detail message explaining the configuration failure
     * @param cause the underlying cause of the configuration failure
     */
    public AuditConfigurationException(String message, Throwable cause) {
        super(message, cause);
        this.configurationKey = null;
        this.configurationValue = null;
    }

    /**
     * Constructs a new AuditConfigurationException with configuration-specific information.
     * 
     * @param message the detail message explaining the configuration failure
     * @param configurationKey the configuration key that caused the failure
     * @param configurationValue the invalid configuration value
     */
    public AuditConfigurationException(String message, String configurationKey, String configurationValue) {
        super(message);
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
    }

    /**
     * Constructs a new AuditConfigurationException with configuration-specific information and cause.
     * 
     * @param message the detail message explaining the configuration failure
     * @param configurationKey the configuration key that caused the failure
     * @param configurationValue the invalid configuration value
     * @param cause the underlying cause of the configuration failure
     */
    public AuditConfigurationException(String message, String configurationKey, String configurationValue, Throwable cause) {
        super(message, cause);
        this.configurationKey = configurationKey;
        this.configurationValue = configurationValue;
    }

    /**
     * Gets the configuration key that caused the failure.
     * 
     * @return the configuration key, or null if not specified
     */
    public String getConfigurationKey() {
        return configurationKey;
    }

    /**
     * Gets the invalid configuration value.
     * 
     * @return the configuration value, or null if not specified
     */
    public String getConfigurationValue() {
        return configurationValue;
    }

    @Override
    public String getErrorCode() {
        return "AUDIT_CONFIGURATION_ERROR";
    }

    @Override
    public int getHttpStatusCode() {
        return 500; // Internal Server Error
    }

    @Override
    public boolean isRetryable() {
        return false; // Configuration errors are not retryable
    }
}