package com.company.audit.service;

import com.company.audit.model.AuditEvent;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.dto.AuditStatistics;
import com.company.audit.model.dto.DataDiscrepancy;
import com.company.audit.model.dto.ReconciliationReport;
import com.company.audit.model.dto.ReconciliationReportDTO;
import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Core audit service interface for managing audit trail operations across the data processing pipeline.
 * 
 * This service provides comprehensive audit logging capabilities for tracking data lineage from multiple
 * source systems through Oracle staging tables, Java module transformations, and final output file generation.
 * All audit events are persisted to Oracle database with correlation ID tracking for complete traceability.
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
public interface AuditService {

    /**
     * Logs a generic audit event for any checkpoint stage in the data processing pipeline.
     * 
     * @param auditEvent the complete audit event to be logged
     * @throws IllegalArgumentException if auditEvent is null or missing required fields
     * @throws AuditPersistenceException if the audit event cannot be persisted to the database
     */
    void logAuditEvent(AuditEvent auditEvent);

    /**
     * Logs file transfer events for Checkpoint 1 when files arrive from mainframe systems to RHEL.
     * Captures file metadata including size, hash, and transfer details for data integrity verification.
     * 
     * @param correlationId unique identifier linking all related audit events in the pipeline run
     * @param sourceSystem identifier of the source system (e.g., "MAINFRAME_SYSTEM_A")
     * @param fileName name of the transferred file
     * @param processName name of the file transfer process
     * @param sourceEntity source location or system identifier
     * @param destinationEntity destination location or system identifier
     * @param keyIdentifier business key or identifier for the data being transferred
     * @param status success, failure, or warning status of the file transfer
     * @param message descriptive message about the file transfer operation
     * @param auditDetails additional metadata including file size, hash, and transfer statistics
     * @throws IllegalArgumentException if any required parameter is null or empty
     * @throws AuditPersistenceException if the audit event cannot be persisted to the database
     */
    void logFileTransfer(UUID correlationId, String sourceSystem, String fileName, String processName,
                        String sourceEntity, String destinationEntity, String keyIdentifier,
                        AuditStatus status, String message, AuditDetails auditDetails);

    /**
     * Logs SQL*Loader operations for Checkpoint 2 when data is ingested from files to Oracle staging tables.
     * Captures load statistics including rows read, loaded, and rejected for data integrity verification.
     * 
     * @param correlationId unique identifier linking all related audit events in the pipeline run
     * @param sourceSystem identifier of the source system providing the data
     * @param tableName name of the Oracle table being loaded
     * @param processName name of the SQL*Loader process
     * @param sourceEntity source file or data location
     * @param destinationEntity destination Oracle table identifier
     * @param keyIdentifier business key or identifier for the data being loaded
     * @param status success, failure, or warning status of the load operation
     * @param message descriptive message about the load operation
     * @param auditDetails load statistics including rows read, loaded, rejected, and control totals
     * @throws IllegalArgumentException if any required parameter is null or empty
     * @throws AuditPersistenceException if the audit event cannot be persisted to the database
     */
    void logSqlLoaderOperation(UUID correlationId, String sourceSystem, String tableName, String processName,
                              String sourceEntity, String destinationEntity, String keyIdentifier,
                              AuditStatus status, String message, AuditDetails auditDetails);

    /**
     * Logs business rule application events for Checkpoint 3 when Java modules apply transformations.
     * Captures rule input/output data and transformation details for business logic traceability.
     * 
     * @param correlationId unique identifier linking all related audit events in the pipeline run
     * @param sourceSystem identifier of the source system providing the data
     * @param moduleName name of the Java module applying business rules
     * @param processName name of the business rule process
     * @param sourceEntity source data entity being transformed
     * @param destinationEntity destination data entity after transformation
     * @param keyIdentifier business key or identifier for the record being processed
     * @param status success, failure, or warning status of the business rule application
     * @param message descriptive message about the business rule operation
     * @param auditDetails rule input/output data and transformation metadata
     * @throws IllegalArgumentException if any required parameter is null or empty
     * @throws AuditPersistenceException if the audit event cannot be persisted to the database
     */
    void logBusinessRuleApplication(UUID correlationId, String sourceSystem, String moduleName, String processName,
                                   String sourceEntity, String destinationEntity, String keyIdentifier,
                                   AuditStatus status, String message, AuditDetails auditDetails);

    /**
     * Logs file generation events for Checkpoint 4 when final output files are created.
     * Captures output file metadata and record counts for end-to-end reconciliation.
     * 
     * @param correlationId unique identifier linking all related audit events in the pipeline run
     * @param sourceSystem identifier of the source system that originated the data
     * @param fileName name of the generated output file
     * @param processName name of the file generation process
     * @param sourceEntity source data location or table
     * @param destinationEntity destination file location
     * @param keyIdentifier business key or identifier for the data being output
     * @param status success, failure, or warning status of the file generation
     * @param message descriptive message about the file generation operation
     * @param auditDetails output file metadata including record counts and control totals
     * @throws IllegalArgumentException if any required parameter is null or empty
     * @throws AuditPersistenceException if the audit event cannot be persisted to the database
     */
    void logFileGeneration(UUID correlationId, String sourceSystem, String fileName, String processName,
                          String sourceEntity, String destinationEntity, String keyIdentifier,
                          AuditStatus status, String message, AuditDetails auditDetails);

    /**
     * Retrieves the complete audit trail for a specific correlation ID, ordered by event timestamp.
     * Used for tracing all events related to a single pipeline run across all checkpoints.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return list of audit events ordered by event timestamp, empty list if no events found
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if the audit events cannot be retrieved from the database
     */
    List<AuditEvent> getAuditTrail(UUID correlationId);

    /**
     * Retrieves audit events filtered by source system and checkpoint stage.
     * Used for monitoring specific source systems at particular pipeline stages.
     * 
     * @param sourceSystem identifier of the source system to filter by
     * @param checkpointStage specific checkpoint stage to filter by
     * @return list of matching audit events ordered by event timestamp
     * @throws IllegalArgumentException if sourceSystem or checkpointStage is null or empty
     * @throws AuditPersistenceException if the audit events cannot be retrieved from the database
     */
    List<AuditEvent> getAuditEventsBySourceAndStage(String sourceSystem, CheckpointStage checkpointStage);

    /**
     * Retrieves audit events filtered by module name and status.
     * Used for monitoring specific Java modules and identifying failed operations.
     * 
     * @param moduleName name of the module to filter by
     * @param status audit status to filter by (SUCCESS, FAILURE, WARNING)
     * @return list of matching audit events ordered by event timestamp
     * @throws IllegalArgumentException if moduleName or status is null or empty
     * @throws AuditPersistenceException if the audit events cannot be retrieved from the database
     */
    List<AuditEvent> getAuditEventsByModuleAndStatus(String moduleName, AuditStatus status);

    /**
     * Counts the number of audit events for a specific correlation ID and status.
     * Used for generating statistics and reconciliation reports.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @param status audit status to count (SUCCESS, FAILURE, WARNING)
     * @return count of matching audit events
     * @throws IllegalArgumentException if correlationId or status is null
     * @throws AuditPersistenceException if the count cannot be retrieved from the database
     */
    long countAuditEventsByCorrelationAndStatus(UUID correlationId, AuditStatus status);

    /**
     * Retrieves audit events with pagination and optional filtering.
     * Used by the REST API dashboard for displaying audit events with filtering capabilities.
     * 
     * @param sourceSystem optional source system filter (null for no filter)
     * @param moduleName optional module name filter (null for no filter)
     * @param status optional audit status filter (null for no filter)
     * @param checkpointStage optional checkpoint stage filter (null for no filter)
     * @param page page number (0-based)
     * @param size number of items per page
     * @return list of audit events matching the criteria with pagination
     * @throws IllegalArgumentException if page or size parameters are invalid
     * @throws AuditPersistenceException if the audit events cannot be retrieved from the database
     */
    List<AuditEvent> getAuditEventsWithFilters(String sourceSystem, String moduleName, AuditStatus status, 
                                              CheckpointStage checkpointStage, int page, int size);

    /**
     * Counts the total number of audit events matching the specified filters.
     * Used for pagination metadata in REST API responses.
     * 
     * @param sourceSystem optional source system filter (null for no filter)
     * @param moduleName optional module name filter (null for no filter)
     * @param status optional audit status filter (null for no filter)
     * @param checkpointStage optional checkpoint stage filter (null for no filter)
     * @return total count of audit events matching the criteria
     * @throws AuditPersistenceException if the count cannot be retrieved from the database
     */
    long countAuditEventsWithFilters(String sourceSystem, String moduleName, AuditStatus status, 
                                    CheckpointStage checkpointStage);

    /**
     * Generates comprehensive audit statistics for a specified date range.
     * Includes event counts by status, source system, module, and checkpoint stage,
     * as well as success rates and trend analysis.
     * 
     * @param startDate start of the date range for statistics (inclusive)
     * @param endDate end of the date range for statistics (inclusive)
     * @return comprehensive audit statistics for the specified period
     * @throws IllegalArgumentException if startDate or endDate is null, or if startDate is after endDate
     * @throws AuditPersistenceException if statistics cannot be calculated due to database errors
     */
    AuditStatistics getAuditStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Identifies data discrepancies by analyzing audit events for inconsistencies.
     * Detects issues such as record count mismatches, missing audit events,
     * control total discrepancies, and processing timeouts.
     * 
     * @param filters optional filters to limit discrepancy analysis (e.g., sourceSystem, moduleName, severity)
     * @return list of identified data discrepancies ordered by severity and detection time
     * @throws AuditPersistenceException if discrepancy analysis cannot be performed due to database errors
     */
    List<DataDiscrepancy> identifyDataDiscrepancies(Map<String, String> filters);

    /**
     * Retrieves existing data discrepancies with optional filtering.
     * Supports filtering by source system, module name, severity, status, and date range.
     * 
     * @param filters optional filters for discrepancy retrieval
     * @return list of data discrepancies matching the specified filters
     * @throws AuditPersistenceException if discrepancies cannot be retrieved from the database
     */
    List<DataDiscrepancy> getDataDiscrepancies(Map<String, String> filters);

    /**
     * Generates a comprehensive reconciliation report for a specific pipeline run.
     * Analyzes all audit events for the correlation ID to verify data integrity,
     * calculate record counts at each checkpoint, and identify discrepancies.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return comprehensive reconciliation report with data integrity analysis
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if the report cannot be generated due to database errors
     */
    ReconciliationReport generateReconciliationReport(UUID correlationId);

    /**
     * Retrieves all available reconciliation reports with optional filtering.
     * Supports filtering by source system, date range, status, and other criteria.
     * 
     * @param filters optional filters for report retrieval (e.g., sourceSystem, startDate, endDate, status)
     * @return list of reconciliation reports matching the specified filters
     * @throws AuditPersistenceException if reports cannot be retrieved from the database
     */
    List<ReconciliationReport> getReconciliationReports(Map<String, String> filters);

    /**
     * Validates data integrity for a specific pipeline run by comparing record counts
     * and control totals across all checkpoints.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return true if data integrity validation passes, false otherwise
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if validation cannot be performed due to database errors
     */
    boolean validateDataIntegrity(UUID correlationId);

    /**
     * Retrieves record counts by source system for a specific pipeline run.
     * Used for reconciliation analysis and reporting.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return map of source system identifiers to record counts
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if counts cannot be retrieved from the database
     */
    Map<String, Long> getRecordCountsBySourceSystem(UUID correlationId);

    /**
     * Generates a standard reconciliation report DTO for a specific pipeline run.
     * Provides essential information including checkpoint counts, control totals, and basic summary.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return standard reconciliation report DTO with essential pipeline information
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if the report cannot be generated due to database errors
     */
    ReconciliationReportDTO.StandardReconciliationReport generateStandardReconciliationReportDTO(UUID correlationId);

    /**
     * Generates a detailed reconciliation report DTO for a specific pipeline run.
     * Provides comprehensive analysis including all discrepancies, checkpoint details, and performance metrics.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return detailed reconciliation report DTO with comprehensive pipeline analysis
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if the report cannot be generated due to database errors
     */
    ReconciliationReportDTO.DetailedReconciliationReport generateDetailedReconciliationReportDTO(UUID correlationId);

    /**
     * Generates a summary reconciliation report DTO for a specific pipeline run.
     * Provides high-level metrics including processing time, success rate, and critical issues count.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return summary reconciliation report DTO with high-level pipeline metrics
     * @throws IllegalArgumentException if correlationId is null
     * @throws AuditPersistenceException if the report cannot be generated due to database errors
     */
    ReconciliationReportDTO.SummaryReconciliationReport generateSummaryReconciliationReportDTO(UUID correlationId);
}