package com.company.audit.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sealed class hierarchy for reconciliation report DTOs using Java 17+ sealed classes.
 * This provides type safety and ensures all possible report types are handled explicitly.
 * 
 * The sealed class allows for different types of reconciliation reports while maintaining
 * compile-time exhaustiveness checking and improved pattern matching capabilities.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "reportType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ReconciliationReportDTO.StandardReconciliationReport.class, name = "STANDARD"),
    @JsonSubTypes.Type(value = ReconciliationReportDTO.DetailedReconciliationReport.class, name = "DETAILED"),
    @JsonSubTypes.Type(value = ReconciliationReportDTO.SummaryReconciliationReport.class, name = "SUMMARY")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
    description = "Base reconciliation report DTO with different report types",
    discriminatorProperty = "reportType",
    subTypes = {
        ReconciliationReportDTO.StandardReconciliationReport.class,
        ReconciliationReportDTO.DetailedReconciliationReport.class,
        ReconciliationReportDTO.SummaryReconciliationReport.class
    }
)
public sealed class ReconciliationReportDTO 
    permits ReconciliationReportDTO.StandardReconciliationReport,
            ReconciliationReportDTO.DetailedReconciliationReport,
            ReconciliationReportDTO.SummaryReconciliationReport {

    @Schema(description = "Type of reconciliation report", example = "STANDARD")
    @JsonProperty("reportType")
    protected final String reportType;

    @Schema(description = "Unique identifier for the pipeline run", example = "550e8400-e29b-41d4-a716-446655440000")
    @JsonProperty("correlationId")
    protected final UUID correlationId;

    @Schema(description = "Source system identifier", example = "MAINFRAME_SYSTEM_A")
    @JsonProperty("sourceSystem")
    protected final String sourceSystem;

    @Schema(description = "Timestamp when the report was generated", example = "2024-01-15T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("reportGeneratedAt")
    protected final LocalDateTime reportGeneratedAt;

    @Schema(description = "Overall status of the pipeline run", example = "SUCCESS")
    @JsonProperty("overallStatus")
    protected final ReportStatus overallStatus;

    /**
     * Protected constructor for sealed class hierarchy.
     */
    protected ReconciliationReportDTO(String reportType, UUID correlationId, String sourceSystem, 
                                    LocalDateTime reportGeneratedAt, ReportStatus overallStatus) {
        this.reportType = reportType;
        this.correlationId = correlationId;
        this.sourceSystem = sourceSystem;
        this.reportGeneratedAt = reportGeneratedAt;
        this.overallStatus = overallStatus;
    }

    // Getters
    public String getReportType() { return reportType; }
    public UUID getCorrelationId() { return correlationId; }
    public String getSourceSystem() { return sourceSystem; }
    public LocalDateTime getReportGeneratedAt() { return reportGeneratedAt; }
    public ReportStatus getOverallStatus() { return overallStatus; }

    /**
     * Standard reconciliation report with essential information.
     * This is the most commonly used report type for regular monitoring and compliance.
     */
    @Schema(description = "Standard reconciliation report with essential pipeline information")
    public static final class StandardReconciliationReport extends ReconciliationReportDTO {

        @Schema(description = "Start timestamp of the pipeline run", example = "2024-01-15T09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("pipelineStartTime")
        private final LocalDateTime pipelineStartTime;

        @Schema(description = "End timestamp of the pipeline run", example = "2024-01-15T10:25:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("pipelineEndTime")
        private final LocalDateTime pipelineEndTime;

        @Schema(description = "Record counts at each checkpoint stage")
        @JsonProperty("checkpointCounts")
        private final Map<String, Long> checkpointCounts;

        @Schema(description = "Control totals for financial reconciliation")
        @JsonProperty("controlTotals")
        private final Map<String, Double> controlTotals;

        @Schema(description = "Number of discrepancies found")
        @JsonProperty("discrepancyCount")
        private final int discrepancyCount;

        @Schema(description = "Basic summary statistics")
        @JsonProperty("summary")
        private final BasicSummary summary;

        public StandardReconciliationReport(UUID correlationId, String sourceSystem, 
                                          LocalDateTime reportGeneratedAt, ReportStatus overallStatus,
                                          LocalDateTime pipelineStartTime, LocalDateTime pipelineEndTime,
                                          Map<String, Long> checkpointCounts, Map<String, Double> controlTotals,
                                          int discrepancyCount, BasicSummary summary) {
            super("STANDARD", correlationId, sourceSystem, reportGeneratedAt, overallStatus);
            this.pipelineStartTime = pipelineStartTime;
            this.pipelineEndTime = pipelineEndTime;
            this.checkpointCounts = checkpointCounts;
            this.controlTotals = controlTotals;
            this.discrepancyCount = discrepancyCount;
            this.summary = summary;
        }

        // Getters
        public LocalDateTime getPipelineStartTime() { return pipelineStartTime; }
        public LocalDateTime getPipelineEndTime() { return pipelineEndTime; }
        public Map<String, Long> getCheckpointCounts() { return checkpointCounts; }
        public Map<String, Double> getControlTotals() { return controlTotals; }
        public int getDiscrepancyCount() { return discrepancyCount; }
        public BasicSummary getSummary() { return summary; }
    }

    /**
     * Detailed reconciliation report with comprehensive information including all discrepancies.
     * Used for in-depth analysis and troubleshooting of pipeline issues.
     */
    @Schema(description = "Detailed reconciliation report with comprehensive pipeline analysis")
    public static final class DetailedReconciliationReport extends ReconciliationReportDTO {

        @Schema(description = "Start timestamp of the pipeline run", example = "2024-01-15T09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("pipelineStartTime")
        private final LocalDateTime pipelineStartTime;

        @Schema(description = "End timestamp of the pipeline run", example = "2024-01-15T10:25:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("pipelineEndTime")
        private final LocalDateTime pipelineEndTime;

        @Schema(description = "Record counts at each checkpoint stage")
        @JsonProperty("checkpointCounts")
        private final Map<String, Long> checkpointCounts;

        @Schema(description = "Control totals for financial reconciliation")
        @JsonProperty("controlTotals")
        private final Map<String, Double> controlTotals;

        @Schema(description = "List of all identified data discrepancies")
        @JsonProperty("discrepancies")
        private final List<DataDiscrepancy> discrepancies;

        @Schema(description = "Detailed summary statistics")
        @JsonProperty("summary")
        private final DetailedSummary summary;

        @Schema(description = "Detailed breakdown by checkpoint stage")
        @JsonProperty("checkpointDetails")
        private final List<CheckpointDetail> checkpointDetails;

        @Schema(description = "Performance metrics for the pipeline run")
        @JsonProperty("performanceMetrics")
        private final PerformanceMetrics performanceMetrics;

        public DetailedReconciliationReport(UUID correlationId, String sourceSystem, 
                                          LocalDateTime reportGeneratedAt, ReportStatus overallStatus,
                                          LocalDateTime pipelineStartTime, LocalDateTime pipelineEndTime,
                                          Map<String, Long> checkpointCounts, Map<String, Double> controlTotals,
                                          List<DataDiscrepancy> discrepancies, DetailedSummary summary,
                                          List<CheckpointDetail> checkpointDetails, PerformanceMetrics performanceMetrics) {
            super("DETAILED", correlationId, sourceSystem, reportGeneratedAt, overallStatus);
            this.pipelineStartTime = pipelineStartTime;
            this.pipelineEndTime = pipelineEndTime;
            this.checkpointCounts = checkpointCounts;
            this.controlTotals = controlTotals;
            this.discrepancies = discrepancies;
            this.summary = summary;
            this.checkpointDetails = checkpointDetails;
            this.performanceMetrics = performanceMetrics;
        }

        // Getters
        public LocalDateTime getPipelineStartTime() { return pipelineStartTime; }
        public LocalDateTime getPipelineEndTime() { return pipelineEndTime; }
        public Map<String, Long> getCheckpointCounts() { return checkpointCounts; }
        public Map<String, Double> getControlTotals() { return controlTotals; }
        public List<DataDiscrepancy> getDiscrepancies() { return discrepancies; }
        public DetailedSummary getSummary() { return summary; }
        public List<CheckpointDetail> getCheckpointDetails() { return checkpointDetails; }
        public PerformanceMetrics getPerformanceMetrics() { return performanceMetrics; }
    }

    /**
     * Summary reconciliation report with high-level metrics only.
     * Used for executive dashboards and high-level monitoring.
     */
    @Schema(description = "Summary reconciliation report with high-level metrics")
    public static final class SummaryReconciliationReport extends ReconciliationReportDTO {

        @Schema(description = "Total processing time in milliseconds", example = "5100000")
        @JsonProperty("totalProcessingTimeMs")
        private final Long totalProcessingTimeMs;

        @Schema(description = "Total number of records processed", example = "150000")
        @JsonProperty("totalRecordsProcessed")
        private final Long totalRecordsProcessed;

        @Schema(description = "Success rate as a percentage", example = "99.0")
        @JsonProperty("successRate")
        private final Double successRate;

        @Schema(description = "Whether data integrity validation passed", example = "true")
        @JsonProperty("dataIntegrityValid")
        private final Boolean dataIntegrityValid;

        @Schema(description = "Number of critical issues found", example = "0")
        @JsonProperty("criticalIssuesCount")
        private final Integer criticalIssuesCount;

        public SummaryReconciliationReport(UUID correlationId, String sourceSystem, 
                                         LocalDateTime reportGeneratedAt, ReportStatus overallStatus,
                                         Long totalProcessingTimeMs, Long totalRecordsProcessed,
                                         Double successRate, Boolean dataIntegrityValid, Integer criticalIssuesCount) {
            super("SUMMARY", correlationId, sourceSystem, reportGeneratedAt, overallStatus);
            this.totalProcessingTimeMs = totalProcessingTimeMs;
            this.totalRecordsProcessed = totalRecordsProcessed;
            this.successRate = successRate;
            this.dataIntegrityValid = dataIntegrityValid;
            this.criticalIssuesCount = criticalIssuesCount;
        }

        // Getters
        public Long getTotalProcessingTimeMs() { return totalProcessingTimeMs; }
        public Long getTotalRecordsProcessed() { return totalRecordsProcessed; }
        public Double getSuccessRate() { return successRate; }
        public Boolean getDataIntegrityValid() { return dataIntegrityValid; }
        public Integer getCriticalIssuesCount() { return criticalIssuesCount; }
    }

    /**
     * Enumeration for report status using Java 17+ enhanced enums.
     */
    @Schema(description = "Overall status of the reconciliation report")
    public enum ReportStatus {
        @Schema(description = "Pipeline completed successfully with no issues")
        SUCCESS("Pipeline completed successfully"),
        
        @Schema(description = "Pipeline completed with warnings or minor issues")
        WARNING("Pipeline completed with warnings"),
        
        @Schema(description = "Pipeline failed or encountered critical errors")
        FAILURE("Pipeline failed"),
        
        @Schema(description = "Pipeline is still in progress")
        IN_PROGRESS("Pipeline in progress"),
        
        @Schema(description = "Pipeline status is unknown or indeterminate")
        UNKNOWN("Status unknown");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Basic summary record for standard reports.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Basic summary statistics for standard reconciliation reports")
    public record BasicSummary(
        @Schema(description = "Total number of successful events", example = "148500")
        @JsonProperty("successfulEvents")
        Long successfulEvents,
        
        @Schema(description = "Total number of failed events", example = "1200")
        @JsonProperty("failedEvents")
        Long failedEvents,
        
        @Schema(description = "Total number of warning events", example = "300")
        @JsonProperty("warningEvents")
        Long warningEvents,
        
        @Schema(description = "Success rate as a percentage", example = "99.0")
        @JsonProperty("successRate")
        Double successRate
    ) {}

    /**
     * Detailed summary record for comprehensive reports.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed summary statistics for comprehensive reconciliation reports")
    public record DetailedSummary(
        @Schema(description = "Total number of records processed", example = "150000")
        @JsonProperty("totalRecordsProcessed")
        Long totalRecordsProcessed,
        
        @Schema(description = "Total number of successful events", example = "148500")
        @JsonProperty("successfulEvents")
        Long successfulEvents,
        
        @Schema(description = "Total number of failed events", example = "1200")
        @JsonProperty("failedEvents")
        Long failedEvents,
        
        @Schema(description = "Total number of warning events", example = "300")
        @JsonProperty("warningEvents")
        Long warningEvents,
        
        @Schema(description = "Success rate as a percentage", example = "99.0")
        @JsonProperty("successRate")
        Double successRate,
        
        @Schema(description = "Total processing time in milliseconds", example = "5100000")
        @JsonProperty("totalProcessingTimeMs")
        Long totalProcessingTimeMs,
        
        @Schema(description = "Whether data integrity validation passed", example = "true")
        @JsonProperty("dataIntegrityValid")
        Boolean dataIntegrityValid,
        
        @Schema(description = "Average processing time per record in milliseconds", example = "34.0")
        @JsonProperty("averageProcessingTimePerRecordMs")
        Double averageProcessingTimePerRecordMs
    ) {}

    /**
     * Checkpoint detail record for detailed reports.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Detailed information for a specific checkpoint stage")
    public record CheckpointDetail(
        @Schema(description = "Checkpoint stage identifier", example = "RHEL_LANDING")
        @JsonProperty("checkpointStage")
        String checkpointStage,
        
        @Schema(description = "Number of records at this checkpoint", example = "150000")
        @JsonProperty("recordCount")
        Long recordCount,
        
        @Schema(description = "Control total for this checkpoint", example = "1250000.50")
        @JsonProperty("controlTotal")
        Double controlTotal,
        
        @Schema(description = "Status of this checkpoint", example = "SUCCESS")
        @JsonProperty("status")
        String status,
        
        @Schema(description = "Processing start time for this checkpoint", example = "2024-01-15T09:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("startTime")
        LocalDateTime startTime,
        
        @Schema(description = "Processing end time for this checkpoint", example = "2024-01-15T09:15:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        @JsonProperty("endTime")
        LocalDateTime endTime,
        
        @Schema(description = "Processing duration in milliseconds", example = "900000")
        @JsonProperty("durationMs")
        Long durationMs
    ) {}

    /**
     * Performance metrics record for detailed reports.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Performance metrics for the pipeline run")
    public record PerformanceMetrics(
        @Schema(description = "Records processed per second", example = "29.41")
        @JsonProperty("recordsPerSecond")
        Double recordsPerSecond,
        
        @Schema(description = "Peak memory usage in MB", example = "512.5")
        @JsonProperty("peakMemoryUsageMB")
        Double peakMemoryUsageMB,
        
        @Schema(description = "Average CPU utilization percentage", example = "75.2")
        @JsonProperty("averageCpuUtilization")
        Double averageCpuUtilization,
        
        @Schema(description = "Database connection pool peak usage", example = "8")
        @JsonProperty("dbConnectionPoolPeakUsage")
        Integer dbConnectionPoolPeakUsage,
        
        @Schema(description = "Number of database queries executed", example = "1250")
        @JsonProperty("totalDbQueries")
        Long totalDbQueries,
        
        @Schema(description = "Average database query time in milliseconds", example = "15.7")
        @JsonProperty("averageDbQueryTimeMs")
        Double averageDbQueryTimeMs
    ) {}

    /**
     * Factory method to create a standard reconciliation report.
     */
    public static StandardReconciliationReport createStandardReport(
            UUID correlationId, String sourceSystem, LocalDateTime reportGeneratedAt,
            ReportStatus overallStatus, LocalDateTime pipelineStartTime, LocalDateTime pipelineEndTime,
            Map<String, Long> checkpointCounts, Map<String, Double> controlTotals,
            int discrepancyCount, BasicSummary summary) {
        return new StandardReconciliationReport(correlationId, sourceSystem, reportGeneratedAt,
                overallStatus, pipelineStartTime, pipelineEndTime, checkpointCounts, controlTotals,
                discrepancyCount, summary);
    }

    /**
     * Factory method to create a detailed reconciliation report.
     */
    public static DetailedReconciliationReport createDetailedReport(
            UUID correlationId, String sourceSystem, LocalDateTime reportGeneratedAt,
            ReportStatus overallStatus, LocalDateTime pipelineStartTime, LocalDateTime pipelineEndTime,
            Map<String, Long> checkpointCounts, Map<String, Double> controlTotals,
            List<DataDiscrepancy> discrepancies, DetailedSummary summary,
            List<CheckpointDetail> checkpointDetails, PerformanceMetrics performanceMetrics) {
        return new DetailedReconciliationReport(correlationId, sourceSystem, reportGeneratedAt,
                overallStatus, pipelineStartTime, pipelineEndTime, checkpointCounts, controlTotals,
                discrepancies, summary, checkpointDetails, performanceMetrics);
    }

    /**
     * Factory method to create a summary reconciliation report.
     */
    public static SummaryReconciliationReport createSummaryReport(
            UUID correlationId, String sourceSystem, LocalDateTime reportGeneratedAt,
            ReportStatus overallStatus, Long totalProcessingTimeMs, Long totalRecordsProcessed,
            Double successRate, Boolean dataIntegrityValid, Integer criticalIssuesCount) {
        return new SummaryReconciliationReport(correlationId, sourceSystem, reportGeneratedAt,
                overallStatus, totalProcessingTimeMs, totalRecordsProcessed, successRate,
                dataIntegrityValid, criticalIssuesCount);
    }
}