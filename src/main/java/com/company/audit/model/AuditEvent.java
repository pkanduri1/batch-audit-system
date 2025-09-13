package com.company.audit.model;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Basic AuditEvent model class representing audit events in the pipeline.
 * This is a POJO without JPA annotations for direct database operations using JdbcTemplate.
 */
public class AuditEvent {
    
    private UUID auditId;
    private UUID correlationId;
    private String sourceSystem;
    private String moduleName;
    private String processName;
    private String sourceEntity;
    private String destinationEntity;
    private String keyIdentifier;
    private CheckpointStage checkpointStage;
    private LocalDateTime eventTimestamp;
    private AuditStatus status;
    private String message;
    private String detailsJson;
    
    /**
     * Default constructor
     */
    public AuditEvent() {
    }
    
    /**
     * Constructor with auditId
     * @param auditId the unique identifier for this audit event
     */
    public AuditEvent(UUID auditId) {
        this.auditId = auditId;
    }
    
    /**
     * Constructor with core fields
     * @param auditId the unique identifier for this audit event
     * @param correlationId the correlation ID linking related audit events
     * @param sourceSystem the source system identifier
     * @param moduleName the name of the module generating the audit event
     */
    public AuditEvent(UUID auditId, UUID correlationId, String sourceSystem, String moduleName) {
        this.auditId = auditId;
        this.correlationId = correlationId;
        this.sourceSystem = sourceSystem;
        this.moduleName = moduleName;
    }
    
    /**
     * Gets the audit ID
     * @return the audit ID as UUID
     */
    public UUID getAuditId() {
        return auditId;
    }
    
    /**
     * Sets the audit ID
     * @param auditId the audit ID as UUID
     */
    public void setAuditId(UUID auditId) {
        this.auditId = auditId;
    }
    
    /**
     * Gets the correlation ID
     * @return the correlation ID as UUID
     */
    public UUID getCorrelationId() {
        return correlationId;
    }
    
    /**
     * Sets the correlation ID
     * @param correlationId the correlation ID as UUID
     */
    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }
    
    /**
     * Gets the source system identifier
     * @return the source system identifier
     */
    public String getSourceSystem() {
        return sourceSystem;
    }
    
    /**
     * Sets the source system identifier
     * @param sourceSystem the source system identifier
     */
    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
    
    /**
     * Gets the module name
     * @return the module name
     */
    public String getModuleName() {
        return moduleName;
    }
    
    /**
     * Sets the module name
     * @param moduleName the module name
     */
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    /**
     * Gets the process name
     * @return the process name
     */
    public String getProcessName() {
        return processName;
    }
    
    /**
     * Sets the process name
     * @param processName the process name
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    /**
     * Gets the source entity
     * @return the source entity
     */
    public String getSourceEntity() {
        return sourceEntity;
    }
    
    /**
     * Sets the source entity
     * @param sourceEntity the source entity
     */
    public void setSourceEntity(String sourceEntity) {
        this.sourceEntity = sourceEntity;
    }
    
    /**
     * Gets the destination entity
     * @return the destination entity
     */
    public String getDestinationEntity() {
        return destinationEntity;
    }
    
    /**
     * Sets the destination entity
     * @param destinationEntity the destination entity
     */
    public void setDestinationEntity(String destinationEntity) {
        this.destinationEntity = destinationEntity;
    }
    
    /**
     * Gets the key identifier
     * @return the key identifier
     */
    public String getKeyIdentifier() {
        return keyIdentifier;
    }
    
    /**
     * Sets the key identifier
     * @param keyIdentifier the key identifier
     */
    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }
    
    /**
     * Gets the checkpoint stage
     * @return the checkpoint stage
     */
    public CheckpointStage getCheckpointStage() {
        return checkpointStage;
    }
    
    /**
     * Sets the checkpoint stage
     * @param checkpointStage the checkpoint stage
     */
    public void setCheckpointStage(CheckpointStage checkpointStage) {
        this.checkpointStage = checkpointStage;
    }
    
    /**
     * Gets the event timestamp
     * @return the event timestamp
     */
    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
    
    /**
     * Sets the event timestamp
     * @param eventTimestamp the event timestamp
     */
    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    /**
     * Gets the audit status
     * @return the audit status
     */
    public AuditStatus getStatus() {
        return status;
    }
    
    /**
     * Sets the audit status
     * @param status the audit status
     */
    public void setStatus(AuditStatus status) {
        this.status = status;
    }
    
    /**
     * Gets the message
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the message
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Gets the details JSON
     * @return the details JSON
     */
    public String getDetailsJson() {
        return detailsJson;
    }
    
    /**
     * Sets the details JSON
     * @param detailsJson the details JSON
     */
    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }
    
    @Override
    public String toString() {
        return "AuditEvent{" +
                "auditId=" + auditId +
                ", correlationId=" + correlationId +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", processName='" + processName + '\'' +
                ", sourceEntity='" + sourceEntity + '\'' +
                ", destinationEntity='" + destinationEntity + '\'' +
                ", keyIdentifier='" + keyIdentifier + '\'' +
                ", checkpointStage=" + checkpointStage +
                ", eventTimestamp=" + eventTimestamp +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", detailsJson='" + detailsJson + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(auditId, that.auditId) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(sourceSystem, that.sourceSystem) &&
                Objects.equals(moduleName, that.moduleName) &&
                Objects.equals(processName, that.processName) &&
                Objects.equals(sourceEntity, that.sourceEntity) &&
                Objects.equals(destinationEntity, that.destinationEntity) &&
                Objects.equals(keyIdentifier, that.keyIdentifier) &&
                checkpointStage == that.checkpointStage &&
                Objects.equals(eventTimestamp, that.eventTimestamp) &&
                status == that.status &&
                Objects.equals(message, that.message) &&
                Objects.equals(detailsJson, that.detailsJson);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(auditId, correlationId, sourceSystem, moduleName, 
                processName, sourceEntity, destinationEntity, keyIdentifier, 
                checkpointStage, eventTimestamp, status, message, detailsJson);
    }
    
    /**
     * Creates a new Builder instance for constructing AuditEvent objects
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for constructing AuditEvent instances using the Builder pattern
     */
    public static class Builder {
        private UUID auditId;
        private UUID correlationId;
        private String sourceSystem;
        private String moduleName;
        private String processName;
        private String sourceEntity;
        private String destinationEntity;
        private String keyIdentifier;
        private CheckpointStage checkpointStage;
        private LocalDateTime eventTimestamp;
        private AuditStatus status;
        private String message;
        private String detailsJson;
        
        /**
         * Private constructor to prevent direct instantiation
         */
        private Builder() {
        }
        
        /**
         * Sets the audit ID
         * @param auditId the audit ID as UUID
         * @return this Builder instance for method chaining
         */
        public Builder auditId(UUID auditId) {
            this.auditId = auditId;
            return this;
        }
        
        /**
         * Sets the correlation ID
         * @param correlationId the correlation ID as UUID
         * @return this Builder instance for method chaining
         */
        public Builder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        /**
         * Sets the source system identifier
         * @param sourceSystem the source system identifier
         * @return this Builder instance for method chaining
         */
        public Builder sourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }
        
        /**
         * Sets the module name
         * @param moduleName the module name
         * @return this Builder instance for method chaining
         */
        public Builder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }
        
        /**
         * Sets the process name
         * @param processName the process name
         * @return this Builder instance for method chaining
         */
        public Builder processName(String processName) {
            this.processName = processName;
            return this;
        }
        
        /**
         * Sets the source entity
         * @param sourceEntity the source entity
         * @return this Builder instance for method chaining
         */
        public Builder sourceEntity(String sourceEntity) {
            this.sourceEntity = sourceEntity;
            return this;
        }
        
        /**
         * Sets the destination entity
         * @param destinationEntity the destination entity
         * @return this Builder instance for method chaining
         */
        public Builder destinationEntity(String destinationEntity) {
            this.destinationEntity = destinationEntity;
            return this;
        }
        
        /**
         * Sets the key identifier
         * @param keyIdentifier the key identifier
         * @return this Builder instance for method chaining
         */
        public Builder keyIdentifier(String keyIdentifier) {
            this.keyIdentifier = keyIdentifier;
            return this;
        }
        
        /**
         * Sets the checkpoint stage
         * @param checkpointStage the checkpoint stage
         * @return this Builder instance for method chaining
         */
        public Builder checkpointStage(CheckpointStage checkpointStage) {
            this.checkpointStage = checkpointStage;
            return this;
        }
        
        /**
         * Sets the event timestamp
         * @param eventTimestamp the event timestamp
         * @return this Builder instance for method chaining
         */
        public Builder eventTimestamp(LocalDateTime eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
            return this;
        }
        
        /**
         * Sets the audit status
         * @param status the audit status
         * @return this Builder instance for method chaining
         */
        public Builder status(AuditStatus status) {
            this.status = status;
            return this;
        }
        
        /**
         * Sets the message
         * @param message the message
         * @return this Builder instance for method chaining
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        /**
         * Sets the details JSON
         * @param detailsJson the details JSON
         * @return this Builder instance for method chaining
         */
        public Builder detailsJson(String detailsJson) {
            this.detailsJson = detailsJson;
            return this;
        }
        
        /**
         * Builds and returns a new AuditEvent instance with the configured values
         * @return a new AuditEvent instance
         */
        public AuditEvent build() {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.auditId = this.auditId;
            auditEvent.correlationId = this.correlationId;
            auditEvent.sourceSystem = this.sourceSystem;
            auditEvent.moduleName = this.moduleName;
            auditEvent.processName = this.processName;
            auditEvent.sourceEntity = this.sourceEntity;
            auditEvent.destinationEntity = this.destinationEntity;
            auditEvent.keyIdentifier = this.keyIdentifier;
            auditEvent.checkpointStage = this.checkpointStage;
            auditEvent.eventTimestamp = this.eventTimestamp;
            auditEvent.status = this.status;
            auditEvent.message = this.message;
            auditEvent.detailsJson = this.detailsJson;
            return auditEvent;
        }
    }
}