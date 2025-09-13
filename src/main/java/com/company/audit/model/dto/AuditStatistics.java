package com.company.audit.model.dto;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for audit statistics containing aggregated data about audit events.
 * Used by the REST API to provide statistical information about audit events
 * within a specified date range or for specific filters.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Audit statistics containing aggregated data about audit events")
public class AuditStatistics {

    @Schema(description = "Start date of the statistics period", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "End date of the statistics period", example = "2024-01-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Total number of audit events in the period", example = "1500")
    private long totalEvents;

    @Schema(description = "Number of successful audit events", example = "1350")
    private long successfulEvents;

    @Schema(description = "Number of failed audit events", example = "100")
    private long failedEvents;

    @Schema(description = "Number of warning audit events", example = "50")
    private long warningEvents;

    @Schema(description = "Success rate as a percentage", example = "90.0")
    private double successRate;

    @Schema(description = "Failure rate as a percentage", example = "6.67")
    private double failureRate;

    @Schema(description = "Warning rate as a percentage", example = "3.33")
    private double warningRate;

    @Schema(description = "Count of events by source system")
    private Map<String, Long> eventsBySourceSystem;

    @Schema(description = "Count of events by module name")
    private Map<String, Long> eventsByModule;

    @Schema(description = "Count of events by checkpoint stage")
    private Map<CheckpointStage, Long> eventsByCheckpointStage;

    @Schema(description = "Count of events by status")
    private Map<AuditStatus, Long> eventsByStatus;

    @Schema(description = "Average events per day in the period", example = "48.39")
    private double averageEventsPerDay;

    @Schema(description = "Peak events in a single day", example = "75")
    private long peakEventsPerDay;

    @Schema(description = "Date with peak events", example = "2024-01-15T00:00:00")
    private LocalDateTime peakDate;

    // Default constructor
    public AuditStatistics() {}

    // Constructor with basic statistics
    public AuditStatistics(LocalDateTime startDate, LocalDateTime endDate, long totalEvents,
                          long successfulEvents, long failedEvents, long warningEvents) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalEvents = totalEvents;
        this.successfulEvents = successfulEvents;
        this.failedEvents = failedEvents;
        this.warningEvents = warningEvents;
        calculateRates();
    }

    /**
     * Calculates success, failure, and warning rates based on event counts
     */
    private void calculateRates() {
        if (totalEvents > 0) {
            this.successRate = (double) successfulEvents / totalEvents * 100.0;
            this.failureRate = (double) failedEvents / totalEvents * 100.0;
            this.warningRate = (double) warningEvents / totalEvents * 100.0;
        } else {
            this.successRate = 0.0;
            this.failureRate = 0.0;
            this.warningRate = 0.0;
        }
    }

    // Getters and setters
    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public long getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
        calculateRates();
    }

    public long getSuccessfulEvents() {
        return successfulEvents;
    }

    public void setSuccessfulEvents(long successfulEvents) {
        this.successfulEvents = successfulEvents;
        calculateRates();
    }

    public long getFailedEvents() {
        return failedEvents;
    }

    public void setFailedEvents(long failedEvents) {
        this.failedEvents = failedEvents;
        calculateRates();
    }

    public long getWarningEvents() {
        return warningEvents;
    }

    public void setWarningEvents(long warningEvents) {
        this.warningEvents = warningEvents;
        calculateRates();
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public double getWarningRate() {
        return warningRate;
    }

    public void setWarningRate(double warningRate) {
        this.warningRate = warningRate;
    }

    public Map<String, Long> getEventsBySourceSystem() {
        return eventsBySourceSystem;
    }

    public void setEventsBySourceSystem(Map<String, Long> eventsBySourceSystem) {
        this.eventsBySourceSystem = eventsBySourceSystem;
    }

    public Map<String, Long> getEventsByModule() {
        return eventsByModule;
    }

    public void setEventsByModule(Map<String, Long> eventsByModule) {
        this.eventsByModule = eventsByModule;
    }

    public Map<CheckpointStage, Long> getEventsByCheckpointStage() {
        return eventsByCheckpointStage;
    }

    public void setEventsByCheckpointStage(Map<CheckpointStage, Long> eventsByCheckpointStage) {
        this.eventsByCheckpointStage = eventsByCheckpointStage;
    }

    public Map<AuditStatus, Long> getEventsByStatus() {
        return eventsByStatus;
    }

    public void setEventsByStatus(Map<AuditStatus, Long> eventsByStatus) {
        this.eventsByStatus = eventsByStatus;
    }

    public double getAverageEventsPerDay() {
        return averageEventsPerDay;
    }

    public void setAverageEventsPerDay(double averageEventsPerDay) {
        this.averageEventsPerDay = averageEventsPerDay;
    }

    public long getPeakEventsPerDay() {
        return peakEventsPerDay;
    }

    public void setPeakEventsPerDay(long peakEventsPerDay) {
        this.peakEventsPerDay = peakEventsPerDay;
    }

    public LocalDateTime getPeakDate() {
        return peakDate;
    }

    public void setPeakDate(LocalDateTime peakDate) {
        this.peakDate = peakDate;
    }

    @Override
    public String toString() {
        return "AuditStatistics{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalEvents=" + totalEvents +
                ", successfulEvents=" + successfulEvents +
                ", failedEvents=" + failedEvents +
                ", warningEvents=" + warningEvents +
                ", successRate=" + successRate +
                ", failureRate=" + failureRate +
                ", warningRate=" + warningRate +
                ", averageEventsPerDay=" + averageEventsPerDay +
                ", peakEventsPerDay=" + peakEventsPerDay +
                ", peakDate=" + peakDate +
                '}';
    }
}