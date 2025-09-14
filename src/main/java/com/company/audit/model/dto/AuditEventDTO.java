package com.company.audit.model.dto;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for AuditEvent API responses using Java 17+ record.
 * Provides a clean, immutable representation of audit events for REST API consumption
 * with comprehensive Swagger documentation and JSON serialization support.
 * 
 * This record is optimized for API responses and includes all necessary fields
 * for audit event visualization in dashboards and reporting interfaces.
 * 
 * @param auditId Unique identifier for the audit event
 * @param correlationId Correlation ID linking related audit events in a pipeline run
 * @param sourceSystem Source system identifier (e.g., MAINFRAME_SYSTEM_A)
 * @param moduleName Name of the module that generated the audit event
 * @param processName Name of the specific process within the module
 * @param sourceEntity Source entity or table name
 * @param destinationEntity Destination entity or table name
 * @param keyIdentifier Business key or identifier for record-level tracing
 * @param checkpointStage Pipeline checkpoint stage where the event occurred
 * @param eventTimestamp Timestamp when the audit event was created
 * @param status Status of the audit event (SUCCESS, FAILURE, WARNING)
 * @param message Human-readable message describing the audit event
 * @param detailsJson Additional metadata in JSON format
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Audit event data transfer object for API responses")
public record AuditEventDTO(
    
    @Schema(
        description = "Unique identifier for the audit event", 
        example = "550e8400-e29b-41d4-a716-446655440000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("auditId")
    UUID auditId,
    
    @Schema(
        description = "Correlation ID linking related audit events in a pipeline run", 
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("correlationId")
    UUID correlationId,
    
    @Schema(
        description = "Source system identifier", 
        example = "MAINFRAME_SYSTEM_A",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("sourceSystem")
    String sourceSystem,
    
    @Schema(
        description = "Name of the module that generated the audit event", 
        example = "FILE_TRANSFER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("moduleName")
    String moduleName,
    
    @Schema(
        description = "Name of the specific process within the module", 
        example = "DAILY_BATCH_PROCESS"
    )
    @JsonProperty("processName")
    String processName,
    
    @Schema(
        description = "Source entity or table name", 
        example = "CUSTOMER_DATA.csv"
    )
    @JsonProperty("sourceEntity")
    String sourceEntity,
    
    @Schema(
        description = "Destination entity or table name", 
        example = "STAGING.CUSTOMER_STAGING"
    )
    @JsonProperty("destinationEntity")
    String destinationEntity,
    
    @Schema(
        description = "Business key or identifier for record-level tracing", 
        example = "BATCH_20240115_001"
    )
    @JsonProperty("keyIdentifier")
    String keyIdentifier,
    
    @Schema(
        description = "Pipeline checkpoint stage where the event occurred",
        example = "RHEL_LANDING",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("checkpointStage")
    CheckpointStage checkpointStage,
    
    @Schema(
        description = "Timestamp when the audit event was created", 
        example = "2024-01-15T10:30:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("eventTimestamp")
    LocalDateTime eventTimestamp,
    
    @Schema(
        description = "Status of the audit event",
        example = "SUCCESS",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @JsonProperty("status")
    AuditStatus status,
    
    @Schema(
        description = "Human-readable message describing the audit event", 
        example = "File successfully transferred from mainframe to RHEL landing zone"
    )
    @JsonProperty("message")
    String message,
    
    @Schema(
        description = "Additional metadata in JSON format containing detailed information about the audit event",
        example = "{\"fileSize\": 1024000, \"recordCount\": 5000, \"processingTimeMs\": 2500}"
    )
    @JsonProperty("detailsJson")
    String detailsJson
) {
    
    /**
     * Creates an AuditEventDTO from an AuditEvent entity.
     * This static factory method provides a convenient way to convert
     * domain entities to DTOs for API responses.
     * 
     * @param auditEvent the AuditEvent entity to convert
     * @return a new AuditEventDTO instance with data from the entity
     * @throws IllegalArgumentException if auditEvent is null
     */
    public static AuditEventDTO fromEntity(AuditEvent auditEvent) {
        if (auditEvent == null) {
            throw new IllegalArgumentException("AuditEvent cannot be null");
        }
        
        return new AuditEventDTO(
            auditEvent.getAuditId(),
            auditEvent.getCorrelationId(),
            auditEvent.getSourceSystem(),
            auditEvent.getModuleName(),
            auditEvent.getProcessName(),
            auditEvent.getSourceEntity(),
            auditEvent.getDestinationEntity(),
            auditEvent.getKeyIdentifier(),
            auditEvent.getCheckpointStage(),
            auditEvent.getEventTimestamp(),
            auditEvent.getStatus(),
            auditEvent.getMessage(),
            auditEvent.getDetailsJson()
        );
    }
    
    /**
     * Converts this DTO back to an AuditEvent entity.
     * This method is useful when the DTO needs to be persisted
     * or used in business logic operations.
     * 
     * @return a new AuditEvent entity with data from this DTO
     */
    public AuditEvent toEntity() {
        return AuditEvent.builder()
            .auditId(auditId)
            .correlationId(correlationId)
            .sourceSystem(sourceSystem)
            .moduleName(moduleName)
            .processName(processName)
            .sourceEntity(sourceEntity)
            .destinationEntity(destinationEntity)
            .keyIdentifier(keyIdentifier)
            .checkpointStage(checkpointStage)
            .eventTimestamp(eventTimestamp)
            .status(status)
            .message(message)
            .detailsJson(detailsJson)
            .build();
    }
    
    /**
     * Creates a builder for constructing AuditEventDTO instances.
     * While records are immutable, this builder provides a fluent API
     * for creating DTOs with optional fields.
     * 
     * @return a new AuditEventDTOBuilder instance
     */
    public static AuditEventDTOBuilder builder() {
        return new AuditEventDTOBuilder();
    }
    
    /**
     * Builder class for constructing AuditEventDTO instances with a fluent API.
     * This builder is particularly useful when creating DTOs with many optional fields
     * or when converting from other data structures.
     */
    public static class AuditEventDTOBuilder {
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
        
        private AuditEventDTOBuilder() {}
        
        public AuditEventDTOBuilder auditId(UUID auditId) {
            this.auditId = auditId;
            return this;
        }
        
        public AuditEventDTOBuilder correlationId(UUID correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public AuditEventDTOBuilder sourceSystem(String sourceSystem) {
            this.sourceSystem = sourceSystem;
            return this;
        }
        
        public AuditEventDTOBuilder moduleName(String moduleName) {
            this.moduleName = moduleName;
            return this;
        }
        
        public AuditEventDTOBuilder processName(String processName) {
            this.processName = processName;
            return this;
        }
        
        public AuditEventDTOBuilder sourceEntity(String sourceEntity) {
            this.sourceEntity = sourceEntity;
            return this;
        }
        
        public AuditEventDTOBuilder destinationEntity(String destinationEntity) {
            this.destinationEntity = destinationEntity;
            return this;
        }
        
        public AuditEventDTOBuilder keyIdentifier(String keyIdentifier) {
            this.keyIdentifier = keyIdentifier;
            return this;
        }
        
        public AuditEventDTOBuilder checkpointStage(CheckpointStage checkpointStage) {
            this.checkpointStage = checkpointStage;
            return this;
        }
        
        public AuditEventDTOBuilder eventTimestamp(LocalDateTime eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
            return this;
        }
        
        public AuditEventDTOBuilder status(AuditStatus status) {
            this.status = status;
            return this;
        }
        
        public AuditEventDTOBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public AuditEventDTOBuilder detailsJson(String detailsJson) {
            this.detailsJson = detailsJson;
            return this;
        }
        
        /**
         * Builds the AuditEventDTO with the configured values.
         * 
         * @return a new AuditEventDTO instance
         */
        public AuditEventDTO build() {
            return new AuditEventDTO(
                auditId, correlationId, sourceSystem, moduleName, processName,
                sourceEntity, destinationEntity, keyIdentifier, checkpointStage,
                eventTimestamp, status, message, detailsJson
            );
        }
    }
}