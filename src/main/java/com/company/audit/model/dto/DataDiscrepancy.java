package com.company.audit.model.dto;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a data discrepancy found during audit analysis.
 * Used to identify inconsistencies in record counts, control totals,
 * or other data integrity issues across pipeline stages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data discrepancy found during audit analysis")
public class DataDiscrepancy {

    @Schema(description = "Unique identifier for the discrepancy", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID discrepancyId;

    @Schema(description = "Correlation ID linking related audit events", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID correlationId;

    @Schema(description = "Source system where discrepancy was detected", example = "MAINFRAME_SYSTEM_A")
    private String sourceSystem;

    @Schema(description = "Module name where discrepancy was detected", example = "SQL_LOADER")
    private String moduleName;

    @Schema(description = "Type of discrepancy detected", example = "RECORD_COUNT_MISMATCH")
    private DiscrepancyType discrepancyType;

    @Schema(description = "Severity level of the discrepancy", example = "HIGH")
    private DiscrepancySeverity severity;

    @Schema(description = "Checkpoint stage where discrepancy was detected")
    private CheckpointStage checkpointStage;

    @Schema(description = "Expected value or count", example = "1000")
    private String expectedValue;

    @Schema(description = "Actual value or count found", example = "995")
    private String actualValue;

    @Schema(description = "Difference between expected and actual values", example = "5")
    private String difference;

    @Schema(description = "Detailed description of the discrepancy")
    private String description;

    @Schema(description = "Business key or identifier related to the discrepancy", example = "BATCH_20240115_001")
    private String keyIdentifier;

    @Schema(description = "When the discrepancy was detected", example = "2024-01-15T14:30:00")
    private LocalDateTime detectedAt;

    @Schema(description = "Status of the discrepancy resolution", example = "OPEN")
    private DiscrepancyStatus status;

    @Schema(description = "Additional metadata about the discrepancy in JSON format")
    private String metadataJson;

    // Default constructor
    public DataDiscrepancy() {
        this.discrepancyId = UUID.randomUUID();
        this.detectedAt = LocalDateTime.now();
        this.status = DiscrepancyStatus.OPEN;
    }

    // Constructor with required fields
    public DataDiscrepancy(UUID correlationId, String sourceSystem, String moduleName,
                          DiscrepancyType discrepancyType, String expectedValue, String actualValue) {
        this();
        this.correlationId = correlationId;
        this.sourceSystem = sourceSystem;
        this.moduleName = moduleName;
        this.discrepancyType = discrepancyType;
        this.expectedValue = expectedValue;
        this.actualValue = actualValue;
        calculateDifference();
        determineSeverity();
    }

    /**
     * Calculates the difference between expected and actual values if they are numeric
     */
    private void calculateDifference() {
        if (expectedValue != null && actualValue != null) {
            try {
                long expected = Long.parseLong(expectedValue);
                long actual = Long.parseLong(actualValue);
                this.difference = String.valueOf(Math.abs(expected - actual));
            } catch (NumberFormatException e) {
                // Values are not numeric, leave difference as null
                this.difference = null;
            }
        }
    }

    /**
     * Determines severity based on discrepancy type and difference magnitude
     */
    private void determineSeverity() {
        if (difference != null) {
            try {
                long diff = Long.parseLong(difference);
                if (diff == 0) {
                    this.severity = DiscrepancySeverity.LOW;
                } else if (diff <= 10) {
                    this.severity = DiscrepancySeverity.MEDIUM;
                } else {
                    this.severity = DiscrepancySeverity.HIGH;
                }
            } catch (NumberFormatException e) {
                this.severity = DiscrepancySeverity.MEDIUM;
            }
        } else {
            this.severity = DiscrepancySeverity.MEDIUM;
        }
    }

    // Getters and setters
    public UUID getDiscrepancyId() {
        return discrepancyId;
    }

    public void setDiscrepancyId(UUID discrepancyId) {
        this.discrepancyId = discrepancyId;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public DiscrepancyType getDiscrepancyType() {
        return discrepancyType;
    }

    public void setDiscrepancyType(DiscrepancyType discrepancyType) {
        this.discrepancyType = discrepancyType;
    }

    public DiscrepancySeverity getSeverity() {
        return severity;
    }

    public void setSeverity(DiscrepancySeverity severity) {
        this.severity = severity;
    }

    public CheckpointStage getCheckpointStage() {
        return checkpointStage;
    }

    public void setCheckpointStage(CheckpointStage checkpointStage) {
        this.checkpointStage = checkpointStage;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
        calculateDifference();
        determineSeverity();
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
        calculateDifference();
        determineSeverity();
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyIdentifier() {
        return keyIdentifier;
    }

    public void setKeyIdentifier(String keyIdentifier) {
        this.keyIdentifier = keyIdentifier;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public DiscrepancyStatus getStatus() {
        return status;
    }

    public void setStatus(DiscrepancyStatus status) {
        this.status = status;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    /**
     * Enumeration of discrepancy types
     */
    @Schema(description = "Type of data discrepancy")
    public enum DiscrepancyType {
        @Schema(description = "Mismatch in record counts between stages")
        RECORD_COUNT_MISMATCH,
        
        @Schema(description = "Mismatch in control totals or checksums")
        CONTROL_TOTAL_MISMATCH,
        
        @Schema(description = "Missing expected audit events")
        MISSING_AUDIT_EVENTS,
        
        @Schema(description = "Data integrity violation")
        DATA_INTEGRITY_VIOLATION,
        
        @Schema(description = "Timeout or processing delay")
        PROCESSING_TIMEOUT,
        
        @Schema(description = "Unexpected data format or structure")
        DATA_FORMAT_ERROR,
        
        @Schema(description = "Other unspecified discrepancy")
        OTHER
    }

    /**
     * Enumeration of discrepancy severity levels
     */
    @Schema(description = "Severity level of the discrepancy")
    public enum DiscrepancySeverity {
        @Schema(description = "Low severity - minor discrepancy")
        LOW,
        
        @Schema(description = "Medium severity - moderate discrepancy")
        MEDIUM,
        
        @Schema(description = "High severity - critical discrepancy")
        HIGH,
        
        @Schema(description = "Critical severity - system-wide impact")
        CRITICAL
    }

    /**
     * Enumeration of discrepancy resolution status
     */
    @Schema(description = "Status of discrepancy resolution")
    public enum DiscrepancyStatus {
        @Schema(description = "Discrepancy is open and unresolved")
        OPEN,
        
        @Schema(description = "Discrepancy is under investigation")
        INVESTIGATING,
        
        @Schema(description = "Discrepancy has been resolved")
        RESOLVED,
        
        @Schema(description = "Discrepancy is a false positive")
        FALSE_POSITIVE,
        
        @Schema(description = "Discrepancy is acknowledged but accepted")
        ACKNOWLEDGED
    }

    @Override
    public String toString() {
        return "DataDiscrepancy{" +
                "discrepancyId=" + discrepancyId +
                ", correlationId=" + correlationId +
                ", sourceSystem='" + sourceSystem + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", discrepancyType=" + discrepancyType +
                ", severity=" + severity +
                ", checkpointStage=" + checkpointStage +
                ", expectedValue='" + expectedValue + '\'' +
                ", actualValue='" + actualValue + '\'' +
                ", difference='" + difference + '\'' +
                ", keyIdentifier='" + keyIdentifier + '\'' +
                ", detectedAt=" + detectedAt +
                ", status=" + status +
                '}';
    }
}