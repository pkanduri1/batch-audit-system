package com.company.audit.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Data Transfer Object representing a comprehensive reconciliation report for a pipeline run.
 * Contains data integrity verification results, record count comparisons, and discrepancy analysis
 * across all checkpoints in the data processing pipeline.
 * 
 * This report provides end-to-end traceability from source files through Oracle loads to final output files,
 * enabling compliance officers and data engineers to verify data integrity and identify processing issues.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Comprehensive reconciliation report for a pipeline run with data integrity verification")
public class ReconciliationReport {

    @Schema(description = "Unique identifier for the pipeline run", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("correlationId")
    private UUID correlationId;

    @Schema(description = "Source system identifier", example = "MAINFRAME_SYSTEM_A")
    @JsonProperty("sourceSystem")
    private String sourceSystem;

    @Schema(description = "Timestamp when the report was generated", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("reportGeneratedAt")
    private LocalDateTime reportGeneratedAt;

    @Schema(description = "Start timestamp of the pipeline run", example = "2024-01-15T09:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("pipelineStartTime")
    private LocalDateTime pipelineStartTime;

    @Schema(description = "End timestamp of the pipeline run", example = "2024-01-15T10:25:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("pipelineEndTime")
    private LocalDateTime pipelineEndTime;

    @Schema(description = "Overall status of the pipeline run", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILURE", "WARNING"})
    @JsonProperty("overallStatus")
    private String overallStatus;

    @Schema(description = "Record counts at each checkpoint stage")
    @JsonProperty("checkpointCounts")
    private Map<String, Long> checkpointCounts;

    @Schema(description = "Control totals for financial reconciliation")
    @JsonProperty("controlTotals")
    private Map<String, Double> controlTotals;

    @Schema(description = "List of identified data discrepancies")
    @JsonProperty("discrepancies")
    private List<DataDiscrepancy> discrepancies;

    @Schema(description = "Summary statistics for the pipeline run")
    @JsonProperty("summary")
    private ReconciliationSummary summary;

    @Schema(description = "Detailed breakdown by checkpoint stage")
    @JsonProperty("checkpointDetails")
    private List<CheckpointDetail> checkpointDetails;

    /**
     * Default constructor for JSON deserialization.
     */
    public ReconciliationReport() {
    }

    /**
     * Constructor for creating a reconciliation report with all required fields.
     */
    public ReconciliationReport(UUID correlationId, String sourceSystem, LocalDateTime reportGeneratedAt,
                               LocalDateTime pipelineStartTime, LocalDateTime pipelineEndTime, String overallStatus,
                               Map<String, Long> checkpointCounts, Map<String, Double> controlTotals,
                               List<DataDiscrepancy> discrepancies, ReconciliationSummary summary,
                               List<CheckpointDetail> checkpointDetails) {
        this.correlationId = correlationId;
        this.sourceSystem = sourceSystem;
        this.reportGeneratedAt = reportGeneratedAt;
        this.pipelineStartTime = pipelineStartTime;
        this.pipelineEndTime = pipelineEndTime;
        this.overallStatus = overallStatus;
        this.checkpointCounts = checkpointCounts;
        this.controlTotals = controlTotals;
        this.discrepancies = discrepancies;
        this.summary = summary;
        this.checkpointDetails = checkpointDetails;
    }

    // Getters and setters

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

    public LocalDateTime getReportGeneratedAt() {
        return reportGeneratedAt;
    }

    public void setReportGeneratedAt(LocalDateTime reportGeneratedAt) {
        this.reportGeneratedAt = reportGeneratedAt;
    }

    public LocalDateTime getPipelineStartTime() {
        return pipelineStartTime;
    }

    public void setPipelineStartTime(LocalDateTime pipelineStartTime) {
        this.pipelineStartTime = pipelineStartTime;
    }

    public LocalDateTime getPipelineEndTime() {
        return pipelineEndTime;
    }

    public void setPipelineEndTime(LocalDateTime pipelineEndTime) {
        this.pipelineEndTime = pipelineEndTime;
    }

    public String getOverallStatus() {
        return overallStatus;
    }

    public void setOverallStatus(String overallStatus) {
        this.overallStatus = overallStatus;
    }

    public Map<String, Long> getCheckpointCounts() {
        return checkpointCounts;
    }

    public void setCheckpointCounts(Map<String, Long> checkpointCounts) {
        this.checkpointCounts = checkpointCounts;
    }

    public Map<String, Double> getControlTotals() {
        return controlTotals;
    }

    public void setControlTotals(Map<String, Double> controlTotals) {
        this.controlTotals = controlTotals;
    }

    public List<DataDiscrepancy> getDiscrepancies() {
        return discrepancies;
    }

    public void setDiscrepancies(List<DataDiscrepancy> discrepancies) {
        this.discrepancies = discrepancies;
    }

    public ReconciliationSummary getSummary() {
        return summary;
    }

    public void setSummary(ReconciliationSummary summary) {
        this.summary = summary;
    }

    public List<CheckpointDetail> getCheckpointDetails() {
        return checkpointDetails;
    }

    public void setCheckpointDetails(List<CheckpointDetail> checkpointDetails) {
        this.checkpointDetails = checkpointDetails;
    }

    /**
     * Nested class representing summary statistics for the reconciliation report.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Summary statistics for the reconciliation report")
    public static class ReconciliationSummary {

        @Schema(description = "Total number of records processed", example = "150000")
        @JsonProperty("totalRecordsProcessed")
        private Long totalRecordsProcessed;

        @Schema(description = "Total number of successful events", example = "148500")
        @JsonProperty("successfulEvents")
        private Long successfulEvents;

        @Schema(description = "Total number of failed events", example = "1200")
        @JsonProperty("failedEvents")
        private Long failedEvents;

        @Schema(description = "Total number of warning events", example = "300")
        @JsonProperty("warningEvents")
        private Long warningEvents;

        @Schema(description = "Success rate as a percentage", example = "99.0")
        @JsonProperty("successRate")
        private Double successRate;

        @Schema(description = "Total processing time in milliseconds", example = "5100000")
        @JsonProperty("totalProcessingTimeMs")
        private Long totalProcessingTimeMs;

        @Schema(description = "Whether data integrity validation passed", example = "true")
        @JsonProperty("dataIntegrityValid")
        private Boolean dataIntegrityValid;

        // Constructors, getters, and setters

        public ReconciliationSummary() {
        }

        public ReconciliationSummary(Long totalRecordsProcessed, Long successfulEvents, Long failedEvents,
                                   Long warningEvents, Double successRate, Long totalProcessingTimeMs,
                                   Boolean dataIntegrityValid) {
            this.totalRecordsProcessed = totalRecordsProcessed;
            this.successfulEvents = successfulEvents;
            this.failedEvents = failedEvents;
            this.warningEvents = warningEvents;
            this.successRate = successRate;
            this.totalProcessingTimeMs = totalProcessingTimeMs;
            this.dataIntegrityValid = dataIntegrityValid;
        }

        public Long getTotalRecordsProcessed() {
            return totalRecordsProcessed;
        }

        public void setTotalRecordsProcessed(Long totalRecordsProcessed) {
            this.totalRecordsProcessed = totalRecordsProcessed;
        }

        public Long getSuccessfulEvents() {
            return successfulEvents;
        }

        public void setSuccessfulEvents(Long successfulEvents) {
            this.successfulEvents = successfulEvents;
        }

        public Long getFailedEvents() {
            return failedEvents;
        }

        public void setFailedEvents(Long failedEvents) {
            this.failedEvents = failedEvents;
        }

        public Long getWarningEvents() {
            return warningEvents;
        }

        public void setWarningEvents(Long warningEvents) {
            this.warningEvents = warningEvents;
        }

        public Double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(Double successRate) {
            this.successRate = successRate;
        }

        public Long getTotalProcessingTimeMs() {
            return totalProcessingTimeMs;
        }

        public void setTotalProcessingTimeMs(Long totalProcessingTimeMs) {
            this.totalProcessingTimeMs = totalProcessingTimeMs;
        }

        public Boolean getDataIntegrityValid() {
            return dataIntegrityValid;
        }

        public void setDataIntegrityValid(Boolean dataIntegrityValid) {
            this.dataIntegrityValid = dataIntegrityValid;
        }
    }

    /**
     * Nested class representing detailed information for a specific checkpoint.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed information for a specific checkpoint stage")
    public static class CheckpointDetail {

        @Schema(description = "Checkpoint stage identifier", example = "RHEL_LANDING")
        @JsonProperty("checkpointStage")
        private String checkpointStage;

        @Schema(description = "Number of records at this checkpoint", example = "150000")
        @JsonProperty("recordCount")
        private Long recordCount;

        @Schema(description = "Control total for this checkpoint", example = "1250000.50")
        @JsonProperty("controlTotal")
        private Double controlTotal;

        @Schema(description = "Status of this checkpoint", example = "SUCCESS")
        @JsonProperty("status")
        private String status;

        @Schema(description = "Processing start time for this checkpoint", example = "2024-01-15T09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("startTime")
        private LocalDateTime startTime;

        @Schema(description = "Processing end time for this checkpoint", example = "2024-01-15T09:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("endTime")
        private LocalDateTime endTime;

        @Schema(description = "Processing duration in milliseconds", example = "900000")
        @JsonProperty("durationMs")
        private Long durationMs;

        // Constructors, getters, and setters

        public CheckpointDetail() {
        }

        public CheckpointDetail(String checkpointStage, Long recordCount, Double controlTotal, String status,
                              LocalDateTime startTime, LocalDateTime endTime, Long durationMs) {
            this.checkpointStage = checkpointStage;
            this.recordCount = recordCount;
            this.controlTotal = controlTotal;
            this.status = status;
            this.startTime = startTime;
            this.endTime = endTime;
            this.durationMs = durationMs;
        }

        public String getCheckpointStage() {
            return checkpointStage;
        }

        public void setCheckpointStage(String checkpointStage) {
            this.checkpointStage = checkpointStage;
        }

        public Long getRecordCount() {
            return recordCount;
        }

        public void setRecordCount(Long recordCount) {
            this.recordCount = recordCount;
        }

        public Double getControlTotal() {
            return controlTotal;
        }

        public void setControlTotal(Double controlTotal) {
            this.controlTotal = controlTotal;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(Long durationMs) {
            this.durationMs = durationMs;
        }
    }
}