package com.company.audit.service;

import java.util.UUID;

/**
 * Interface for managing correlation IDs throughout the audit pipeline.
 * Provides thread-safe correlation ID generation, storage, and retrieval
 * to ensure proper audit event correlation across pipeline stages.
 */
public interface CorrelationIdManager {
    
    /**
     * Generates a new correlation ID using UUID.
     * 
     * @return a new UUID correlation ID
     */
    UUID generateCorrelationId();
    
    /**
     * Retrieves the current correlation ID for the current thread.
     * 
     * @return the current correlation ID, or null if none is set
     */
    UUID getCurrentCorrelationId();
    
    /**
     * Sets the correlation ID for the current thread.
     * 
     * @param correlationId the correlation ID to set
     */
    void setCorrelationId(UUID correlationId);
    
    /**
     * Clears the correlation ID for the current thread.
     * This should be called to prevent memory leaks when processing is complete.
     */
    void clearCorrelationId();
}