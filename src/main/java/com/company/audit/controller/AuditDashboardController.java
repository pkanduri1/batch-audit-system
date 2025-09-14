package com.company.audit.controller;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import com.company.audit.model.dto.AuditEventDTO;
import com.company.audit.model.dto.AuditStatistics;
import com.company.audit.model.dto.DataDiscrepancy;
import com.company.audit.model.dto.PagedResponse;
import com.company.audit.model.dto.ReconciliationReport;
import com.company.audit.model.dto.ReconciliationReportDTO;
import com.company.audit.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API controller for audit dashboard and reporting functionality.
 * Provides endpoints for monitoring audit events, generating reports, and accessing statistics.
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit Dashboard", description = "Audit trail monitoring and reporting APIs")
public class AuditDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AuditDashboardController.class);

    private final AuditService auditService;

    @Autowired
    public AuditDashboardController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * Retrieves audit events with optional filtering and pagination.
     * Supports filtering by source system, module name, status, and checkpoint stage.
     * 
     * @param sourceSystem optional source system filter
     * @param moduleName optional module name filter
     * @param status optional audit status filter
     * @param checkpointStage optional checkpoint stage filter
     * @param page page number (0-based, default: 0)
     * @param size number of items per page (default: 20, max: 1000)
     * @return paginated response containing audit events and metadata
     */
    @GetMapping("/events")
    @Operation(
        summary = "Get audit events", 
        description = "Retrieve audit events with optional filtering and pagination. " +
                     "Supports filtering by source system, module name, audit status, and checkpoint stage. " +
                     "Results are ordered by event timestamp (most recent first)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved audit events",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PagedResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid filter parameters or pagination values",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while retrieving audit events",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<PagedResponse<AuditEventDTO>> getAuditEvents(
        @Parameter(
            description = "Filter by source system identifier (e.g., 'MAINFRAME_SYSTEM_A')",
            example = "MAINFRAME_SYSTEM_A",
            schema = @Schema(type = "string")
        )
        @RequestParam(required = false) String sourceSystem,
        
        @Parameter(
            description = "Filter by module name (e.g., 'FILE_TRANSFER', 'SQL_LOADER', 'BUSINESS_RULES')",
            example = "FILE_TRANSFER",
            schema = @Schema(type = "string")
        )
        @RequestParam(required = false) String moduleName,
        
        @Parameter(
            description = "Filter by audit status",
            example = "SUCCESS",
            schema = @Schema(implementation = AuditStatus.class)
        )
        @RequestParam(required = false) AuditStatus status,
        
        @Parameter(
            description = "Filter by checkpoint stage in the data processing pipeline",
            example = "RHEL_LANDING",
            schema = @Schema(implementation = CheckpointStage.class)
        )
        @RequestParam(required = false) CheckpointStage checkpointStage,
        
        @Parameter(
            description = "Page number (0-based)",
            example = "0",
            schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
        )
        @RequestParam(defaultValue = "0") int page,
        
        @Parameter(
            description = "Number of items per page (maximum 1000)",
            example = "20",
            schema = @Schema(type = "integer", minimum = "1", maximum = "1000", defaultValue = "20")
        )
        @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("GET /api/audit/events - sourceSystem: {}, moduleName: {}, status: {}, " +
            "checkpointStage: {}, page: {}, size: {}", 
            sourceSystem, moduleName, status, checkpointStage, page, size);
        
        try {
            // Validate pagination parameters
            if (page < 0) {
                logger.warn("Invalid page number: {}", page);
                return ResponseEntity.badRequest().build();
            }
            
            if (size <= 0 || size > 1000) {
                logger.warn("Invalid page size: {}", size);
                return ResponseEntity.badRequest().build();
            }
            
            // Retrieve audit events with filters
            List<AuditEvent> auditEvents = auditService.getAuditEventsWithFilters(
                sourceSystem, moduleName, status, checkpointStage, page, size);
            
            // Convert to DTOs
            List<AuditEventDTO> auditEventDTOs = auditEvents.stream()
                .map(AuditEventDTO::fromEntity)
                .toList();
            
            // Get total count for pagination metadata
            long totalElements = auditService.countAuditEventsWithFilters(
                sourceSystem, moduleName, status, checkpointStage);
            
            // Create paginated response
            PagedResponse<AuditEventDTO> pagedResponse = PagedResponse.of(auditEventDTOs, page, size, totalElements);
            
            logger.info("Successfully retrieved {} audit events (page {}, size {}, total {})", 
                auditEventDTOs.size(), page, size, totalElements);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(pagedResponse);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for audit events request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error retrieving audit events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves comprehensive audit statistics for a specified date range.
     * Includes event counts by status, source system, module, checkpoint stage,
     * success rates, and trend analysis.
     * 
     * @param startDate start of the date range for statistics (inclusive)
     * @param endDate end of the date range for statistics (inclusive)
     * @return comprehensive audit statistics for the specified period
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Get audit statistics", 
        description = "Retrieve comprehensive audit statistics for a specified date range. " +
                     "Includes event counts by status, source system, module, checkpoint stage, " +
                     "success rates, failure rates, and trend analysis. " +
                     "Useful for monitoring system health and identifying patterns."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved audit statistics",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuditStatistics.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid date parameters (start date after end date, null values, etc.)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while calculating statistics",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<AuditStatistics> getAuditStatistics(
        @Parameter(
            description = "Start date for statistics calculation (ISO 8601 format)",
            example = "2024-01-01T00:00:00",
            required = true,
            schema = @Schema(type = "string", format = "date-time")
        )
        @RequestParam LocalDateTime startDate,
        
        @Parameter(
            description = "End date for statistics calculation (ISO 8601 format)",
            example = "2024-01-31T23:59:59",
            required = true,
            schema = @Schema(type = "string", format = "date-time")
        )
        @RequestParam LocalDateTime endDate
    ) {
        logger.info("GET /api/audit/statistics - startDate: {}, endDate: {}", startDate, endDate);
        
        try {
            // Validate date parameters
            if (startDate == null) {
                logger.warn("Start date parameter is null");
                return ResponseEntity.badRequest().build();
            }
            
            if (endDate == null) {
                logger.warn("End date parameter is null");
                return ResponseEntity.badRequest().build();
            }
            
            if (startDate.isAfter(endDate)) {
                logger.warn("Start date {} is after end date {}", startDate, endDate);
                return ResponseEntity.badRequest().build();
            }
            
            // Generate statistics
            AuditStatistics statistics = auditService.getAuditStatistics(startDate, endDate);
            
            logger.info("Successfully generated audit statistics for period {} to {}: {} total events", 
                startDate, endDate, statistics.getTotalEvents());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(statistics);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for audit statistics request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error generating audit statistics for period {} to {}", startDate, endDate, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves data discrepancies with optional filtering.
     * Identifies inconsistencies such as record count mismatches, missing audit events,
     * control total discrepancies, and processing timeouts.
     * 
     * @param filters optional filters for discrepancy retrieval (sourceSystem, moduleName, severity, etc.)
     * @return list of data discrepancies matching the specified filters
     */
    @GetMapping("/discrepancies")
    @Operation(
        summary = "Get data discrepancies", 
        description = "Retrieve data discrepancies with optional filtering. " +
                     "Identifies inconsistencies such as record count mismatches, missing audit events, " +
                     "control total discrepancies, processing timeouts, and data integrity violations. " +
                     "Supports filtering by source system, module name, severity level, and date range. " +
                     "Results are ordered by severity (high to low) and detection time (recent first)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved data discrepancies",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = DataDiscrepancy.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid filter parameters",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while identifying discrepancies",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<List<DataDiscrepancy>> getDataDiscrepancies(
        @Parameter(
            description = "Optional filters for discrepancy retrieval. " +
                         "Supported filters: sourceSystem, moduleName, severity (LOW/MEDIUM/HIGH/CRITICAL), " +
                         "startDate (ISO 8601), endDate (ISO 8601), status (OPEN/INVESTIGATING/RESOLVED/FALSE_POSITIVE/ACKNOWLEDGED)",
            example = "sourceSystem=MAINFRAME_SYSTEM_A&severity=HIGH&startDate=2024-01-01T00:00:00",
            schema = @Schema(type = "object")
        )
        @RequestParam Map<String, String> filters
    ) {
        logger.info("GET /api/audit/discrepancies - filters: {}", filters);
        
        try {
            // Validate filter parameters
            if (filters.containsKey("severity")) {
                String severity = filters.get("severity");
                try {
                    DataDiscrepancy.DiscrepancySeverity.valueOf(severity.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid severity filter value: {}", severity);
                    return ResponseEntity.badRequest().build();
                }
            }
            
            if (filters.containsKey("status")) {
                String status = filters.get("status");
                try {
                    DataDiscrepancy.DiscrepancyStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid status filter value: {}", status);
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Validate date filters
            if (filters.containsKey("startDate")) {
                try {
                    LocalDateTime.parse(filters.get("startDate"));
                } catch (Exception e) {
                    logger.warn("Invalid startDate filter format: {}", filters.get("startDate"));
                    return ResponseEntity.badRequest().build();
                }
            }
            
            if (filters.containsKey("endDate")) {
                try {
                    LocalDateTime.parse(filters.get("endDate"));
                } catch (Exception e) {
                    logger.warn("Invalid endDate filter format: {}", filters.get("endDate"));
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Retrieve discrepancies
            List<DataDiscrepancy> discrepancies = auditService.getDataDiscrepancies(filters);
            
            logger.info("Successfully retrieved {} data discrepancies with filters: {}", 
                discrepancies.size(), filters);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(discrepancies);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for discrepancies request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error retrieving data discrepancies with filters: {}", filters, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a comprehensive reconciliation report for a specific pipeline run.
     * Analyzes all audit events for the correlation ID to verify data integrity,
     * calculate record counts at each checkpoint, and identify discrepancies.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @return comprehensive reconciliation report with data integrity analysis
     */
    @GetMapping("/reconciliation/{correlationId}")
    @Operation(
        summary = "Get reconciliation report", 
        description = "Retrieve a comprehensive reconciliation report for a specific pipeline run. " +
                     "Analyzes all audit events for the correlation ID to verify data integrity, " +
                     "calculate record counts at each checkpoint, identify discrepancies, and provide " +
                     "end-to-end traceability from source files through Oracle loads to final output files. " +
                     "Essential for compliance reporting and data integrity verification."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved reconciliation report",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReconciliationReport.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid correlation ID format or no audit events found for the specified correlation ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No audit events found for the specified correlation ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while generating reconciliation report",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<ReconciliationReport> getReconciliationReport(
        @Parameter(
            description = "Unique correlation ID for the pipeline run (UUID format)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID correlationId
    ) {
        logger.info("GET /api/audit/reconciliation/{} - generating reconciliation report", correlationId);
        
        try {
            // Validate correlation ID
            if (correlationId == null) {
                logger.warn("Correlation ID is null");
                return ResponseEntity.badRequest().build();
            }
            
            // Generate reconciliation report
            ReconciliationReport report = auditService.generateReconciliationReport(correlationId);
            
            logger.info("Successfully generated reconciliation report for correlation ID: {} - {} events, {} discrepancies, status: {}", 
                correlationId, 
                report.getSummary() != null ? report.getSummary().getSuccessfulEvents() + report.getSummary().getFailedEvents() + report.getSummary().getWarningEvents() : 0,
                report.getDiscrepancies() != null ? report.getDiscrepancies().size() : 0,
                report.getOverallStatus());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(report);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid correlation ID or no audit events found: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error generating reconciliation report for correlation ID: {}", correlationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves reconciliation report with specified detail level using new DTO structure.
     * Supports different report types: STANDARD, DETAILED, and SUMMARY.
     * 
     * @param correlationId unique identifier for the pipeline run
     * @param reportType type of report to generate (STANDARD, DETAILED, SUMMARY)
     * @return reconciliation report DTO with requested detail level
     */
    @GetMapping("/reconciliation/{correlationId}/dto")
    @Operation(
        summary = "Get reconciliation report DTO", 
        description = "Retrieve a reconciliation report using the new DTO structure with different detail levels. " +
                     "Supports STANDARD (essential info), DETAILED (comprehensive analysis), and SUMMARY (high-level metrics) report types. " +
                     "Uses Java 17+ sealed classes for type safety and improved API design."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved reconciliation report DTO",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReconciliationReportDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid correlation ID format or report type",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No audit events found for the specified correlation ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while generating reconciliation report",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<ReconciliationReportDTO> getReconciliationReportDTO(
        @Parameter(
            description = "Unique correlation ID for the pipeline run (UUID format)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            required = true,
            schema = @Schema(type = "string", format = "uuid")
        )
        @PathVariable UUID correlationId,
        
        @Parameter(
            description = "Type of report to generate",
            example = "STANDARD",
            schema = @Schema(type = "string", allowableValues = {"STANDARD", "DETAILED", "SUMMARY"})
        )
        @RequestParam(defaultValue = "STANDARD") String reportType
    ) {
        logger.info("GET /api/audit/reconciliation/{}/dto - generating {} reconciliation report", 
            correlationId, reportType);
        
        try {
            // Validate correlation ID
            if (correlationId == null) {
                logger.warn("Correlation ID is null");
                return ResponseEntity.badRequest().build();
            }
            
            // Validate report type
            if (!reportType.matches("^(STANDARD|DETAILED|SUMMARY)$")) {
                logger.warn("Invalid report type: {}", reportType);
                return ResponseEntity.badRequest().build();
            }
            
            // Generate reconciliation report DTO based on type
            ReconciliationReportDTO reportDTO = switch (reportType.toUpperCase()) {
                case "STANDARD" -> auditService.generateStandardReconciliationReportDTO(correlationId);
                case "DETAILED" -> auditService.generateDetailedReconciliationReportDTO(correlationId);
                case "SUMMARY" -> auditService.generateSummaryReconciliationReportDTO(correlationId);
                default -> throw new IllegalArgumentException("Unsupported report type: " + reportType);
            };
            
            logger.info("Successfully generated {} reconciliation report DTO for correlation ID: {} - status: {}", 
                reportType, correlationId, reportDTO.getOverallStatus());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reportDTO);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid correlation ID or no audit events found: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error generating {} reconciliation report DTO for correlation ID: {}", 
                reportType, correlationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all available reconciliation reports with optional filtering.
     * Supports filtering by source system, date range, status, and other criteria.
     * Results are paginated and ordered by pipeline start time (most recent first).
     * 
     * @param filters optional filters for report retrieval
     * @return list of reconciliation reports matching the specified filters
     */
    @GetMapping("/reconciliation/reports")
    @Operation(
        summary = "Get reconciliation reports", 
        description = "Retrieve all available reconciliation reports with optional filtering. " +
                     "Supports filtering by source system, date range, overall status, and other criteria. " +
                     "Results are ordered by pipeline start time (most recent first) and can be used for " +
                     "compliance reporting, trend analysis, and operational monitoring. " +
                     "Each report provides a comprehensive view of data integrity for a complete pipeline run."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "Successfully retrieved reconciliation reports",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReconciliationReport.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid filter parameters (invalid date format, unknown status values, etc.)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error while retrieving reconciliation reports",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = String.class)
            )
        )
    })
    public ResponseEntity<List<ReconciliationReport>> getReconciliationReports(
        @Parameter(
            description = "Optional filters for report retrieval. " +
                         "Supported filters: sourceSystem (e.g., 'MAINFRAME_SYSTEM_A'), " +
                         "status (SUCCESS/FAILURE/WARNING), " +
                         "startDate (ISO 8601 format, e.g., '2024-01-01T00:00:00'), " +
                         "endDate (ISO 8601 format, e.g., '2024-01-31T23:59:59'). " +
                         "Default date range is last 30 days if not specified.",
            example = "sourceSystem=MAINFRAME_SYSTEM_A&status=SUCCESS&startDate=2024-01-01T00:00:00&endDate=2024-01-31T23:59:59",
            schema = @Schema(type = "object")
        )
        @RequestParam Map<String, String> filters
    ) {
        logger.info("GET /api/audit/reconciliation/reports - filters: {}", filters);
        
        try {
            // Validate filter parameters
            if (filters.containsKey("status")) {
                String status = filters.get("status");
                if (status != null && !status.matches("^(SUCCESS|FAILURE|WARNING)$")) {
                    logger.warn("Invalid status filter value: {}", status);
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Validate date filters
            if (filters.containsKey("startDate")) {
                try {
                    LocalDateTime.parse(filters.get("startDate"));
                } catch (Exception e) {
                    logger.warn("Invalid startDate filter format: {}", filters.get("startDate"));
                    return ResponseEntity.badRequest().build();
                }
            }
            
            if (filters.containsKey("endDate")) {
                try {
                    LocalDateTime.parse(filters.get("endDate"));
                } catch (Exception e) {
                    logger.warn("Invalid endDate filter format: {}", filters.get("endDate"));
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Validate date range consistency
            if (filters.containsKey("startDate") && filters.containsKey("endDate")) {
                try {
                    LocalDateTime startDate = LocalDateTime.parse(filters.get("startDate"));
                    LocalDateTime endDate = LocalDateTime.parse(filters.get("endDate"));
                    if (startDate.isAfter(endDate)) {
                        logger.warn("Start date {} is after end date {}", startDate, endDate);
                        return ResponseEntity.badRequest().build();
                    }
                } catch (Exception e) {
                    logger.warn("Error validating date range: {}", e.getMessage());
                    return ResponseEntity.badRequest().build();
                }
            }
            
            // Retrieve reconciliation reports
            List<ReconciliationReport> reports = auditService.getReconciliationReports(filters);
            
            logger.info("Successfully retrieved {} reconciliation reports with filters: {}", 
                reports.size(), filters);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reports);
                
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for reconciliation reports request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (Exception e) {
            logger.error("Error retrieving reconciliation reports with filters: {}", filters, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}