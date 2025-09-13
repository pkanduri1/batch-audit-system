package com.company.audit.enums;

/**
 * Enumeration representing the status of an audit event.
 * Used to categorize the outcome of pipeline operations and business logic execution.
 */
public enum AuditStatus {
    /**
     * Indicates successful completion of an operation
     */
    SUCCESS,
    
    /**
     * Indicates an operation failed with an error
     */
    FAILURE,
    
    /**
     * Indicates an operation completed but with warnings or discrepancies
     */
    WARNING
}