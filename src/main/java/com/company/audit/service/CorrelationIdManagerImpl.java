package com.company.audit.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ThreadLocal-based implementation of CorrelationIdManager.
 * Uses ThreadLocal storage to maintain correlation IDs per thread,
 * ensuring thread safety and proper isolation in multi-threaded environments.
 * Compatible with virtual threads for future Java versions.
 */
@Service
public class CorrelationIdManagerImpl implements CorrelationIdManager {
    
    /**
     * ThreadLocal storage for correlation IDs.
     * Uses InheritableThreadLocal to support virtual threads and thread inheritance.
     */
    private static final ThreadLocal<UUID> correlationIdHolder = new InheritableThreadLocal<>();
    
    @Override
    public UUID generateCorrelationId() {
        // Use UUID.randomUUID() with Java 17+ improvements for better entropy
        UUID correlationId = UUID.randomUUID();
        setCorrelationId(correlationId);
        return correlationId;
    }
    
    @Override
    public UUID getCurrentCorrelationId() {
        return correlationIdHolder.get();
    }
    
    @Override
    public void setCorrelationId(UUID correlationId) {
        if (correlationId == null) {
            clearCorrelationId();
        } else {
            correlationIdHolder.set(correlationId);
        }
    }
    
    @Override
    public void clearCorrelationId() {
        correlationIdHolder.remove();
    }
    
    /**
     * Utility method to execute a task with a specific correlation ID.
     * Automatically sets and clears the correlation ID to prevent memory leaks.
     * 
     * @param correlationId the correlation ID to use
     * @param task the task to execute
     */
    public void executeWithCorrelationId(UUID correlationId, Runnable task) {
        UUID previousCorrelationId = getCurrentCorrelationId();
        try {
            setCorrelationId(correlationId);
            task.run();
        } finally {
            // Restore previous correlation ID or clear if none existed
            if (previousCorrelationId != null) {
                setCorrelationId(previousCorrelationId);
            } else {
                clearCorrelationId();
            }
        }
    }
    
    /**
     * Utility method to check if a correlation ID is currently set.
     * 
     * @return true if a correlation ID is set for the current thread
     */
    public boolean hasCorrelationId() {
        return getCurrentCorrelationId() != null;
    }
}