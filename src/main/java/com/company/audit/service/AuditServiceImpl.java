package com.company.audit.service;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.exception.AuditPersistenceException;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.AuditEvent;
import com.company.audit.model.dto.AuditStatistics;
import com.company.audit.model.dto.DataDiscrepancy;
import com.company.audit.repository.AuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Isolation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation of AuditService providing comprehensive audit trail management with Oracle database persistence.
 * 
 * This service handles all audit operations for the data processing pipeline, including checkpoint-specific
 * logging methods for file transfers, SQL*Loader operations, business rule applications, and file generation.
 * All audit events are persisted to Oracle database using JdbcTemplate with proper error handling and validation.
 * 
 * Features:
 * - Automatic UUID generation for audit events
 * - JSON serialization of audit details using Jackson
 * - Comprehensive validation of input parameters
 * - Graceful error handling with specific exceptions
 * - Transaction management for data consistency
 * - Logging for troubleshooting and monitoring
 * 
 * @author Audit Team
 * @version 1.0
 * @since 1.0
 */
@Service
@Transactional(
    propagation = Propagation.REQUIRED,
    isolation = Isolation.READ_COMMITTED,
    rollbackFor = {AuditPersistenceException.class, RuntimeException.class}
)
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection
     * 
     * @param auditRepository the repository for audit event persistence
     * @param objectMapper Jackson ObjectMapper for JSON serialization
     */
    public AuditServiceImpl(AuditRepository auditRepository, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void logAuditEvent(AuditEvent auditEvent) {
        logger.debug("Logging audit event: {}", auditEvent);
        
        // Validate input
        validateAuditEvent(auditEvent);
        
        // Set audit ID if not provided
        if (auditEvent.getAuditId() == null) {
            auditEvent.setAuditId(UUID.randomUUID());
        }
        
        // Set event timestamp if not provided
        if (auditEvent.getEventTimestamp() == null) {
            auditEvent.setEventTimestamp(LocalDateTime.now());
        }
        
        try {
            auditRepository.save(auditEvent);
            logger.info("Successfully logged audit event with ID: {} for correlation ID: {}", 
                auditEvent.getAuditId(), auditEvent.getCorrelationId());
        } catch (Exception e) {
            logger.error("Failed to persist audit event with ID: {} for correlation ID: {}", 
                auditEvent.getAuditId(), auditEvent.getCorrelationId(), e);
            throw new AuditPersistenceException("Failed to persist audit event", e);
        }
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {AuditPersistenceException.class, RuntimeException.class}
    )
    public void logFileTransfer(UUID correlationId, String sourceSystem, String fileName, String processName,
                               String sourceEntity, String destinationEntity, String keyIdentifier,
                               AuditStatus status, String message, AuditDetails auditDetails) {
        
        logger.debug("Logging file transfer for correlation ID: {}, source system: {}, file: {}", 
            correlationId, sourceSystem, fileName);
        
        // Validate required parameters
        validateRequiredParameters(correlationId, sourceSystem, fileName, processName, status);
        
        // Convert AuditDetails to JSON
        String detailsJson = convertAuditDetailsToJson(auditDetails);
        
        // Use consistent module name for file transfer operations
        String moduleName = "FILE_TRANSFER";
        
        // Enhanced message formatting using Java 17+ text blocks for complex messages
        String enhancedMessage = message != null ? message : 
            switch (status) {
                case SUCCESS -> """
                    File transfer completed successfully.
                    Source: %s
                    Destination: %s
                    File: %s
                    """.formatted(sourceEntity != null ? sourceEntity : "Unknown", 
                                 destinationEntity != null ? destinationEntity : "Unknown", 
                                 fileName);
                case FAILURE -> """
                    File transfer failed.
                    Source: %s
                    File: %s
                    Process: %s
                    """.formatted(sourceEntity != null ? sourceEntity : "Unknown", 
                                 fileName, processName);
                case WARNING -> """
                    File transfer completed with warnings.
                    File: %s
                    Process: %s
                    """.formatted(fileName, processName);
            };
        
        // Create audit event
        AuditEvent auditEvent = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(correlationId)
            .sourceSystem(sourceSystem)
            .moduleName(moduleName)
            .processName(processName)
            .sourceEntity(sourceEntity)
            .destinationEntity(destinationEntity)
            .keyIdentifier(keyIdentifier)
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(LocalDateTime.now())
            .status(status)
            .message(enhancedMessage)
            .detailsJson(detailsJson)
            .build();
        
        logAuditEvent(auditEvent);
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {AuditPersistenceException.class, RuntimeException.class},
        timeout = 30
    )
    public void logSqlLoaderOperation(UUID correlationId, String sourceSystem, String tableName, String processName,
                                     String sourceEntity, String destinationEntity, String keyIdentifier,
                                     AuditStatus status, String message, AuditDetails auditDetails) {
        
        logger.debug("Logging SQL*Loader operation for correlation ID: {}, source system: {}, table: {}", 
            correlationId, sourceSystem, tableName);
        
        try {
            // Enhanced validation with detailed error messages
            validateSqlLoaderParameters(correlationId, sourceSystem, tableName, processName, status, auditDetails);
            
            // Convert AuditDetails to JSON with enhanced error handling
            String detailsJson = convertAuditDetailsToJsonWithValidation(auditDetails, "SQL*Loader");
            
            // Determine checkpoint stage based on process name or status
            CheckpointStage checkpointStage = determineLoaderCheckpointStage(processName, status);
            
            // Use consistent module name for SQL*Loader operations
            String moduleName = "SQL_LOADER";
            
            // Enhanced message with load statistics if available
            String enhancedMessage = buildSqlLoaderMessage(message, auditDetails, status, tableName);
            
            // Create audit event with enhanced error context
            AuditEvent auditEvent = AuditEvent.builder()
                .auditId(UUID.randomUUID())
                .correlationId(correlationId)
                .sourceSystem(sourceSystem)
                .moduleName(moduleName)
                .processName(processName)
                .sourceEntity(sourceEntity)
                .destinationEntity(destinationEntity != null ? destinationEntity : tableName)
                .keyIdentifier(keyIdentifier)
                .checkpointStage(checkpointStage)
                .eventTimestamp(LocalDateTime.now())
                .status(status)
                .message(enhancedMessage)
                .detailsJson(detailsJson)
                .build();
            
            logAuditEvent(auditEvent);
            
            // Log additional metrics for monitoring
            logSqlLoaderMetrics(correlationId, sourceSystem, tableName, status, auditDetails);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for SQL*Loader audit logging: correlation={}, source={}, table={}", 
                correlationId, sourceSystem, tableName, e);
            throw e;
        } catch (AuditPersistenceException e) {
            logger.error("Failed to persist SQL*Loader audit event: correlation={}, source={}, table={}", 
                correlationId, sourceSystem, tableName, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during SQL*Loader audit logging: correlation={}, source={}, table={}", 
                correlationId, sourceSystem, tableName, e);
            throw new AuditPersistenceException("Unexpected error during SQL*Loader audit logging", e);
        }
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {AuditPersistenceException.class, RuntimeException.class},
        timeout = 30
    )
    public void logBusinessRuleApplication(UUID correlationId, String sourceSystem, String moduleName, String processName,
                                          String sourceEntity, String destinationEntity, String keyIdentifier,
                                          AuditStatus status, String message, AuditDetails auditDetails) {
        
        logger.debug("Logging business rule application for correlation ID: {}, source system: {}, module: {}", 
            correlationId, sourceSystem, moduleName);
        
        try {
            // Enhanced validation with detailed error messages for business rule operations
            validateBusinessRuleParameters(correlationId, sourceSystem, moduleName, processName, status, auditDetails);
            
            // Convert AuditDetails to JSON with enhanced error handling
            String detailsJson = convertAuditDetailsToJsonWithValidation(auditDetails, "Business Rule Application");
            
            // Enhanced message with business rule details if available
            String enhancedMessage = buildBusinessRuleMessage(message, auditDetails, status, moduleName, keyIdentifier);
            
            // Create audit event with enhanced error context
            AuditEvent auditEvent = AuditEvent.builder()
                .auditId(UUID.randomUUID())
                .correlationId(correlationId)
                .sourceSystem(sourceSystem)
                .moduleName(moduleName)
                .processName(processName)
                .sourceEntity(sourceEntity)
                .destinationEntity(destinationEntity)
                .keyIdentifier(keyIdentifier)
                .checkpointStage(CheckpointStage.LOGIC_APPLIED)
                .eventTimestamp(LocalDateTime.now())
                .status(status)
                .message(enhancedMessage)
                .detailsJson(detailsJson)
                .build();
            
            logAuditEvent(auditEvent);
            
            // Log additional metrics for business rule monitoring
            logBusinessRuleMetrics(correlationId, sourceSystem, moduleName, status, auditDetails);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for business rule audit logging: correlation={}, source={}, module={}", 
                correlationId, sourceSystem, moduleName, e);
            throw e;
        } catch (AuditPersistenceException e) {
            logger.error("Failed to persist business rule audit event: correlation={}, source={}, module={}", 
                correlationId, sourceSystem, moduleName, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during business rule audit logging: correlation={}, source={}, module={}", 
                correlationId, sourceSystem, moduleName, e);
            throw new AuditPersistenceException("Unexpected error during business rule audit logging", e);
        }
    }

    @Override
    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.READ_COMMITTED,
        rollbackFor = {AuditPersistenceException.class, RuntimeException.class},
        timeout = 30
    )
    public void logFileGeneration(UUID correlationId, String sourceSystem, String fileName, String processName,
                                 String sourceEntity, String destinationEntity, String keyIdentifier,
                                 AuditStatus status, String message, AuditDetails auditDetails) {
        
        logger.debug("Logging file generation for correlation ID: {}, source system: {}, file: {}", 
            correlationId, sourceSystem, fileName);
        
        try {
            // Enhanced validation with detailed error messages for file generation operations
            validateFileGenerationParameters(correlationId, sourceSystem, fileName, processName, status, auditDetails);
            
            // Convert AuditDetails to JSON with enhanced error handling
            String detailsJson = convertAuditDetailsToJsonWithValidation(auditDetails, "File Generation");
            
            // Enhanced message with file generation details if available
            String enhancedMessage = buildFileGenerationMessage(message, auditDetails, status, fileName, sourceEntity);
            
            // Create audit event with enhanced error context
            AuditEvent auditEvent = AuditEvent.builder()
                .auditId(UUID.randomUUID())
                .correlationId(correlationId)
                .sourceSystem(sourceSystem)
                .moduleName("FILE_GENERATOR")
                .processName(processName)
                .sourceEntity(sourceEntity)
                .destinationEntity(destinationEntity)
                .keyIdentifier(keyIdentifier)
                .checkpointStage(CheckpointStage.FILE_GENERATED)
                .eventTimestamp(LocalDateTime.now())
                .status(status)
                .message(enhancedMessage)
                .detailsJson(detailsJson)
                .build();
            
            logAuditEvent(auditEvent);
            
            // Log additional metrics for file generation monitoring
            logFileGenerationMetrics(correlationId, sourceSystem, fileName, status, auditDetails);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for file generation audit logging: correlation={}, source={}, file={}", 
                correlationId, sourceSystem, fileName, e);
            throw e;
        } catch (AuditPersistenceException e) {
            logger.error("Failed to persist file generation audit event: correlation={}, source={}, file={}", 
                correlationId, sourceSystem, fileName, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during file generation audit logging: correlation={}, source={}, file={}", 
                correlationId, sourceSystem, fileName, e);
            throw new AuditPersistenceException("Unexpected error during file generation audit logging", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditTrail(UUID correlationId) {
        logger.debug("Retrieving audit trail for correlation ID: {}", correlationId);
        
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation ID cannot be null");
        }
        
        try {
            List<AuditEvent> auditTrail = auditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId);
            logger.info("Retrieved {} audit events for correlation ID: {}", auditTrail.size(), correlationId);
            return auditTrail;
        } catch (Exception e) {
            logger.error("Failed to retrieve audit trail for correlation ID: {}", correlationId, e);
            throw new AuditPersistenceException("Failed to retrieve audit trail", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventsBySourceAndStage(String sourceSystem, CheckpointStage checkpointStage) {
        logger.debug("Retrieving audit events for source system: {} and checkpoint stage: {}", 
            sourceSystem, checkpointStage);
        
        if (sourceSystem == null || sourceSystem.trim().isEmpty()) {
            throw new IllegalArgumentException("Source system cannot be null or empty");
        }
        if (checkpointStage == null) {
            throw new IllegalArgumentException("Checkpoint stage cannot be null");
        }
        
        try {
            List<AuditEvent> auditEvents = auditRepository.findBySourceSystemAndCheckpointStage(sourceSystem, checkpointStage);
            logger.info("Retrieved {} audit events for source system: {} and checkpoint stage: {}", 
                auditEvents.size(), sourceSystem, checkpointStage);
            return auditEvents;
        } catch (Exception e) {
            logger.error("Failed to retrieve audit events for source system: {} and checkpoint stage: {}", 
                sourceSystem, checkpointStage, e);
            throw new AuditPersistenceException("Failed to retrieve audit events by source and stage", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventsByModuleAndStatus(String moduleName, AuditStatus status) {
        logger.debug("Retrieving audit events for module: {} and status: {}", moduleName, status);
        
        if (moduleName == null || moduleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Module name cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        try {
            List<AuditEvent> auditEvents = auditRepository.findByModuleNameAndStatus(moduleName, status);
            logger.info("Retrieved {} audit events for module: {} and status: {}", 
                auditEvents.size(), moduleName, status);
            return auditEvents;
        } catch (Exception e) {
            logger.error("Failed to retrieve audit events for module: {} and status: {}", 
                moduleName, status, e);
            throw new AuditPersistenceException("Failed to retrieve audit events by module and status", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuditEventsByCorrelationAndStatus(UUID correlationId, AuditStatus status) {
        logger.debug("Counting audit events for correlation ID: {} and status: {}", correlationId, status);
        
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation ID cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        try {
            long count = auditRepository.countByCorrelationIdAndStatus(correlationId, status);
            logger.info("Found {} audit events for correlation ID: {} and status: {}", 
                count, correlationId, status);
            return count;
        } catch (Exception e) {
            logger.error("Failed to count audit events for correlation ID: {} and status: {}", 
                correlationId, status, e);
            throw new AuditPersistenceException("Failed to count audit events", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventsWithFilters(String sourceSystem, String moduleName, AuditStatus status, 
                                                      CheckpointStage checkpointStage, int page, int size) {
        logger.debug("Retrieving audit events with filters - sourceSystem: {}, moduleName: {}, status: {}, " +
            "checkpointStage: {}, page: {}, size: {}", sourceSystem, moduleName, status, checkpointStage, page, size);
        
        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > 1000) {
            throw new IllegalArgumentException("Page size cannot exceed 1000");
        }
        
        int offset = page * size;
        
        try {
            List<AuditEvent> auditEvents = auditRepository.findWithFiltersAndPagination(
                sourceSystem, moduleName, status, checkpointStage, offset, size);
            logger.info("Retrieved {} audit events with filters (page: {}, size: {})", 
                auditEvents.size(), page, size);
            return auditEvents;
        } catch (Exception e) {
            logger.error("Failed to retrieve audit events with filters - sourceSystem: {}, moduleName: {}, " +
                "status: {}, checkpointStage: {}, page: {}, size: {}", 
                sourceSystem, moduleName, status, checkpointStage, page, size, e);
            throw new AuditPersistenceException("Failed to retrieve audit events with filters", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuditEventsWithFilters(String sourceSystem, String moduleName, AuditStatus status, 
                                           CheckpointStage checkpointStage) {
        logger.debug("Counting audit events with filters - sourceSystem: {}, moduleName: {}, status: {}, " +
            "checkpointStage: {}", sourceSystem, moduleName, status, checkpointStage);
        
        try {
            long count = auditRepository.countWithFilters(sourceSystem, moduleName, status, checkpointStage);
            logger.info("Found {} audit events matching filters", count);
            return count;
        } catch (Exception e) {
            logger.error("Failed to count audit events with filters - sourceSystem: {}, moduleName: {}, " +
                "status: {}, checkpointStage: {}", sourceSystem, moduleName, status, checkpointStage, e);
            throw new AuditPersistenceException("Failed to count audit events with filters", e);
        }
    }

    /**
     * Validates that an AuditEvent has all required fields
     * 
     * @param auditEvent the audit event to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateAuditEvent(AuditEvent auditEvent) {
        if (auditEvent == null) {
            throw new IllegalArgumentException("Audit event cannot be null");
        }
        if (auditEvent.getCorrelationId() == null) {
            throw new IllegalArgumentException("Correlation ID cannot be null");
        }
        if (auditEvent.getSourceSystem() == null || auditEvent.getSourceSystem().trim().isEmpty()) {
            throw new IllegalArgumentException("Source system cannot be null or empty");
        }
        if (auditEvent.getStatus() == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Validates required parameters for checkpoint-specific logging methods
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param identifier the identifier (file name, table name, module name)
     * @param processName the process name
     * @param status the audit status
     * @throws IllegalArgumentException if any required parameter is invalid
     */
    private void validateRequiredParameters(UUID correlationId, String sourceSystem, String identifier, 
                                          String processName, AuditStatus status) {
        if (correlationId == null) {
            throw new IllegalArgumentException("Correlation ID cannot be null");
        }
        if (sourceSystem == null || sourceSystem.trim().isEmpty()) {
            throw new IllegalArgumentException("Source system cannot be null or empty");
        }
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        if (processName == null || processName.trim().isEmpty()) {
            throw new IllegalArgumentException("Process name cannot be null or empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Converts AuditDetails object to JSON string using Jackson ObjectMapper
     * 
     * @param auditDetails the audit details to convert
     * @return JSON string representation, or null if auditDetails is null
     * @throws AuditPersistenceException if JSON serialization fails
     */
    private String convertAuditDetailsToJson(AuditDetails auditDetails) {
        if (auditDetails == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(auditDetails);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AuditDetails to JSON", e);
            throw new AuditPersistenceException("Failed to serialize audit details to JSON", e);
        }
    }

    /**
     * Determines the appropriate checkpoint stage for SQL*Loader operations
     * based on process name or status using Java 17+ enhanced switch expressions
     * 
     * @param processName the process name
     * @param status the audit status
     * @return the appropriate CheckpointStage
     */
    private CheckpointStage determineLoaderCheckpointStage(String processName, AuditStatus status) {
        if (processName != null) {
            String lowerProcessName = processName.toLowerCase();
            
            // Use enhanced if-else with Java 17+ features
            if (lowerProcessName.contains("start") || lowerProcessName.contains("begin") || lowerProcessName.contains("init")) {
                return CheckpointStage.SQLLOADER_START;
            } else if (lowerProcessName.contains("complete") || lowerProcessName.contains("finish") || 
                       lowerProcessName.contains("end") || lowerProcessName.contains("done")) {
                return CheckpointStage.SQLLOADER_COMPLETE;
            }
        }
        
        // Default based on status
        return status == AuditStatus.SUCCESS ? CheckpointStage.SQLLOADER_COMPLETE : CheckpointStage.SQLLOADER_START;
    }

    /**
     * Enhanced validation specifically for SQL*Loader parameters with detailed error messages
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param tableName the table name
     * @param processName the process name
     * @param status the audit status
     * @param auditDetails the audit details
     * @throws IllegalArgumentException if any required parameter is invalid
     */
    private void validateSqlLoaderParameters(UUID correlationId, String sourceSystem, String tableName, 
                                           String processName, AuditStatus status, AuditDetails auditDetails) {
        // Basic validation
        validateRequiredParameters(correlationId, sourceSystem, tableName, processName, status);
        
        // SQL*Loader specific validation
        if (tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be empty for SQL*Loader operations");
        }
        
        // Validate table name format (basic Oracle table name validation)
        if (!tableName.matches("^[A-Za-z][A-Za-z0-9_$#]*$")) {
            logger.warn("Table name '{}' may not be a valid Oracle table name", tableName);
        }
        
        // Validate audit details for SQL*Loader operations
        if (auditDetails != null) {
            if (auditDetails.getRowsRead() != null && auditDetails.getRowsRead() < 0) {
                throw new IllegalArgumentException("Rows read cannot be negative");
            }
            if (auditDetails.getRowsLoaded() != null && auditDetails.getRowsLoaded() < 0) {
                throw new IllegalArgumentException("Rows loaded cannot be negative");
            }
            if (auditDetails.getRowsRejected() != null && auditDetails.getRowsRejected() < 0) {
                throw new IllegalArgumentException("Rows rejected cannot be negative");
            }
        }
    }

    /**
     * Enhanced JSON conversion with validation and operation context
     * 
     * @param auditDetails the audit details to convert
     * @param operationType the type of operation for error context
     * @return JSON string representation, or null if auditDetails is null
     * @throws AuditPersistenceException if JSON serialization fails
     */
    private String convertAuditDetailsToJsonWithValidation(AuditDetails auditDetails, String operationType) {
        if (auditDetails == null) {
            return null;
        }
        
        try {
            String json = objectMapper.writeValueAsString(auditDetails);
            
            // Validate JSON size (prevent extremely large JSON from causing issues)
            if (json.length() > 10000) { // 10KB limit
                logger.warn("Large audit details JSON ({} characters) for {} operation", json.length(), operationType);
            }
            
            return json;
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize AuditDetails to JSON for {} operation", operationType, e);
            throw new AuditPersistenceException(
                String.format("Failed to serialize audit details to JSON for %s operation", operationType), e);
        }
    }

    /**
     * Builds enhanced message for SQL*Loader operations with load statistics
     * 
     * @param originalMessage the original message
     * @param auditDetails the audit details containing load statistics
     * @param status the audit status
     * @param tableName the target table name
     * @return enhanced message with load statistics
     */
    private String buildSqlLoaderMessage(String originalMessage, AuditDetails auditDetails, 
                                       AuditStatus status, String tableName) {
        if (originalMessage != null && !originalMessage.trim().isEmpty()) {
            return originalMessage;
        }
        
        // Build message using Java 17+ text blocks and switch expressions
        return switch (status) {
            case SUCCESS -> auditDetails != null ? """
                SQL*Loader operation completed successfully.
                Target Table: %s
                Rows Read: %d
                Rows Loaded: %d
                Rows Rejected: %d
                """.formatted(
                    tableName,
                    auditDetails.getRowsRead() != null ? auditDetails.getRowsRead() : 0,
                    auditDetails.getRowsLoaded() != null ? auditDetails.getRowsLoaded() : 0,
                    auditDetails.getRowsRejected() != null ? auditDetails.getRowsRejected() : 0
                ) : "SQL*Loader operation completed successfully for table: " + tableName;
                
            case FAILURE -> """
                SQL*Loader operation failed.
                Target Table: %s
                Check logs for detailed error information.
                """.formatted(tableName);
                
            case WARNING -> auditDetails != null ? """
                SQL*Loader operation completed with warnings.
                Target Table: %s
                Rows Rejected: %d
                Review rejected records for data quality issues.
                """.formatted(
                    tableName,
                    auditDetails.getRowsRejected() != null ? auditDetails.getRowsRejected() : 0
                ) : "SQL*Loader operation completed with warnings for table: " + tableName;
        };
    }

    /**
     * Logs additional metrics for SQL*Loader operations for monitoring purposes
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param tableName the target table name
     * @param status the audit status
     * @param auditDetails the audit details
     */
    private void logSqlLoaderMetrics(UUID correlationId, String sourceSystem, String tableName, 
                                   AuditStatus status, AuditDetails auditDetails) {
        if (auditDetails == null) {
            return;
        }
        
        // Log metrics for monitoring and alerting
        if (auditDetails.getRowsRejected() != null && auditDetails.getRowsRejected() > 0) {
            logger.warn("SQL*Loader rejected {} rows for table {} (correlation: {}, source: {})", 
                auditDetails.getRowsRejected(), tableName, correlationId, sourceSystem);
        }
        
        if (auditDetails.getRowsLoaded() != null && auditDetails.getRowsRead() != null) {
            long loadedRows = auditDetails.getRowsLoaded();
            long readRows = auditDetails.getRowsRead();
            
            if (readRows > 0) {
                double successRate = (double) loadedRows / readRows * 100;
                logger.info("SQL*Loader success rate: {}% ({}/{} rows) for table {} (correlation: {})", 
                    String.format("%.2f", successRate), loadedRows, readRows, tableName, correlationId);
                
                // Alert on low success rates
                if (successRate < 95.0 && status == AuditStatus.SUCCESS) {
                    logger.warn("Low SQL*Loader success rate: {}% for table {} (correlation: {})", 
                        String.format("%.2f", successRate), tableName, correlationId);
                }
            }
        }
    }

    /**
     * Enhanced validation specifically for business rule parameters with detailed error messages
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param moduleName the module name
     * @param processName the process name
     * @param status the audit status
     * @param auditDetails the audit details
     * @throws IllegalArgumentException if any required parameter is invalid
     */
    private void validateBusinessRuleParameters(UUID correlationId, String sourceSystem, String moduleName, 
                                               String processName, AuditStatus status, AuditDetails auditDetails) {
        // Basic validation
        validateRequiredParameters(correlationId, sourceSystem, moduleName, processName, status);
        
        // Business rule specific validation
        if (moduleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Module name cannot be empty for business rule operations");
        }
        
        // Validate module name format (basic Java class/module name validation)
        if (!moduleName.matches("^[A-Za-z][A-Za-z0-9_]*$")) {
            logger.warn("Module name '{}' may not be a valid Java module name", moduleName);
        }
        
        // Validate audit details for business rule operations
        if (auditDetails != null) {
            if (auditDetails.getRecordCount() != null && auditDetails.getRecordCount() < 0) {
                throw new IllegalArgumentException("Record count cannot be negative");
            }
            
            // Validate rule input/output data structure
            if (auditDetails.getRuleInput() != null && auditDetails.getRuleInput().isEmpty()) {
                logger.warn("Rule input data is empty for business rule operation in module: {}", moduleName);
            }
            if (auditDetails.getRuleOutput() != null && auditDetails.getRuleOutput().isEmpty()) {
                logger.warn("Rule output data is empty for business rule operation in module: {}", moduleName);
            }
        }
    }

    /**
     * Enhanced validation specifically for file generation parameters with detailed error messages
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param fileName the file name
     * @param processName the process name
     * @param status the audit status
     * @param auditDetails the audit details
     * @throws IllegalArgumentException if any required parameter is invalid
     */
    private void validateFileGenerationParameters(UUID correlationId, String sourceSystem, String fileName, 
                                                 String processName, AuditStatus status, AuditDetails auditDetails) {
        // Basic validation
        validateRequiredParameters(correlationId, sourceSystem, fileName, processName, status);
        
        // File generation specific validation
        if (fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty for file generation operations");
        }
        
        // Validate file name format (basic file name validation)
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("File name contains invalid characters: " + fileName);
        }
        
        // Validate audit details for file generation operations
        if (auditDetails != null) {
            if (auditDetails.getFileSizeBytes() != null && auditDetails.getFileSizeBytes() < 0) {
                throw new IllegalArgumentException("File size cannot be negative");
            }
            if (auditDetails.getRecordCount() != null && auditDetails.getRecordCount() < 0) {
                throw new IllegalArgumentException("Record count cannot be negative");
            }
            
            // Validate file hash if provided
            if (auditDetails.getFileHashSha256() != null && !auditDetails.getFileHashSha256().trim().isEmpty()) {
                String hash = auditDetails.getFileHashSha256().trim();
                if (!hash.matches("^[a-fA-F0-9]{64}$")) {
                    logger.warn("File hash '{}' may not be a valid SHA-256 hash for file: {}", hash, fileName);
                }
            }
        }
    }

    /**
     * Builds enhanced message for business rule operations with rule details
     * 
     * @param originalMessage the original message
     * @param auditDetails the audit details containing rule information
     * @param status the audit status
     * @param moduleName the module name
     * @param keyIdentifier the key identifier
     * @return enhanced message with business rule details
     */
    private String buildBusinessRuleMessage(String originalMessage, AuditDetails auditDetails, 
                                           AuditStatus status, String moduleName, String keyIdentifier) {
        if (originalMessage != null && !originalMessage.trim().isEmpty()) {
            return originalMessage;
        }
        
        // Build message using Java 17+ text blocks and switch expressions
        return switch (status) {
            case SUCCESS -> auditDetails != null && auditDetails.getRecordCount() != null ? """
                Business rule application completed successfully.
                Module: %s
                Key Identifier: %s
                Records Processed: %d
                Rule Input Available: %s
                Rule Output Available: %s
                """.formatted(
                    moduleName,
                    keyIdentifier != null ? keyIdentifier : "N/A",
                    auditDetails.getRecordCount(),
                    auditDetails.getRuleInput() != null ? "Yes" : "No",
                    auditDetails.getRuleOutput() != null ? "Yes" : "No"
                ) : String.format("Business rule application completed successfully in module: %s", moduleName);
                
            case FAILURE -> """
                Business rule application failed.
                Module: %s
                Key Identifier: %s
                Check logs for detailed error information.
                """.formatted(
                    moduleName,
                    keyIdentifier != null ? keyIdentifier : "N/A"
                );
                
            case WARNING -> """
                Business rule application completed with warnings.
                Module: %s
                Key Identifier: %s
                Review rule output for potential data quality issues.
                """.formatted(
                    moduleName,
                    keyIdentifier != null ? keyIdentifier : "N/A"
                );
        };
    }

    /**
     * Builds enhanced message for file generation operations with file details
     * 
     * @param originalMessage the original message
     * @param auditDetails the audit details containing file information
     * @param status the audit status
     * @param fileName the file name
     * @param sourceEntity the source entity
     * @return enhanced message with file generation details
     */
    private String buildFileGenerationMessage(String originalMessage, AuditDetails auditDetails, 
                                             AuditStatus status, String fileName, String sourceEntity) {
        if (originalMessage != null && !originalMessage.trim().isEmpty()) {
            return originalMessage;
        }
        
        // Build message using Java 17+ text blocks and switch expressions
        return switch (status) {
            case SUCCESS -> auditDetails != null ? """
                File generation completed successfully.
                File Name: %s
                Source Entity: %s
                File Size: %s bytes
                Record Count: %d
                File Hash: %s
                """.formatted(
                    fileName,
                    sourceEntity != null ? sourceEntity : "N/A",
                    auditDetails.getFileSizeBytes() != null ? auditDetails.getFileSizeBytes().toString() : "Unknown",
                    auditDetails.getRecordCount() != null ? auditDetails.getRecordCount() : 0,
                    auditDetails.getFileHashSha256() != null ? auditDetails.getFileHashSha256() : "Not calculated"
                ) : String.format("File generation completed successfully: %s", fileName);
                
            case FAILURE -> """
                File generation failed.
                File Name: %s
                Source Entity: %s
                Check logs for detailed error information.
                """.formatted(
                    fileName,
                    sourceEntity != null ? sourceEntity : "N/A"
                );
                
            case WARNING -> """
                File generation completed with warnings.
                File Name: %s
                Source Entity: %s
                Review generated file for potential issues.
                """.formatted(
                    fileName,
                    sourceEntity != null ? sourceEntity : "N/A"
                );
        };
    }

    /**
     * Logs additional metrics for business rule operations for monitoring purposes
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param moduleName the module name
     * @param status the audit status
     * @param auditDetails the audit details
     */
    private void logBusinessRuleMetrics(UUID correlationId, String sourceSystem, String moduleName, 
                                       AuditStatus status, AuditDetails auditDetails) {
        if (auditDetails == null) {
            return;
        }
        
        // Log metrics for monitoring and alerting
        if (auditDetails.getRecordCount() != null) {
            logger.info("Business rule processed {} records in module {} (correlation: {}, source: {})", 
                auditDetails.getRecordCount(), moduleName, correlationId, sourceSystem);
        }
        
        // Log rule input/output availability for debugging
        boolean hasRuleInput = auditDetails.getRuleInput() != null && !auditDetails.getRuleInput().isEmpty();
        boolean hasRuleOutput = auditDetails.getRuleOutput() != null && !auditDetails.getRuleOutput().isEmpty();
        
        if (!hasRuleInput && status == AuditStatus.SUCCESS) {
            logger.warn("Business rule operation succeeded but no rule input data captured for module {} (correlation: {})", 
                moduleName, correlationId);
        }
        
        if (!hasRuleOutput && status == AuditStatus.SUCCESS) {
            logger.warn("Business rule operation succeeded but no rule output data captured for module {} (correlation: {})", 
                moduleName, correlationId);
        }
        
        // Log control totals if available
        if (auditDetails.getControlTotalDebits() != null) {
            logger.info("Business rule control total debits: {} for module {} (correlation: {})", 
                auditDetails.getControlTotalDebits(), moduleName, correlationId);
        }
    }

    /**
     * Logs additional metrics for file generation operations for monitoring purposes
     * 
     * @param correlationId the correlation ID
     * @param sourceSystem the source system
     * @param fileName the file name
     * @param status the audit status
     * @param auditDetails the audit details
     */
    private void logFileGenerationMetrics(UUID correlationId, String sourceSystem, String fileName, 
                                         AuditStatus status, AuditDetails auditDetails) {
        if (auditDetails == null) {
            return;
        }
        
        // Log metrics for monitoring and alerting
        if (auditDetails.getFileSizeBytes() != null) {
            long fileSizeKB = auditDetails.getFileSizeBytes() / 1024;
            logger.info("Generated file {} size: {} KB (correlation: {}, source: {})", 
                fileName, fileSizeKB, correlationId, sourceSystem);
            
            // Alert on unusually large files (>100MB)
            if (auditDetails.getFileSizeBytes() > 100 * 1024 * 1024) {
                logger.warn("Large file generated: {} ({} MB) for correlation: {}", 
                    fileName, auditDetails.getFileSizeBytes() / (1024 * 1024), correlationId);
            }
            
            // Alert on empty files when expecting data
            if (auditDetails.getFileSizeBytes() == 0 && status == AuditStatus.SUCCESS) {
                logger.warn("Empty file generated: {} for correlation: {}", fileName, correlationId);
            }
        }
        
        if (auditDetails.getRecordCount() != null) {
            logger.info("Generated file {} contains {} records (correlation: {}, source: {})", 
                fileName, auditDetails.getRecordCount(), correlationId, sourceSystem);
            
            // Alert on zero record count when expecting data
            if (auditDetails.getRecordCount() == 0 && status == AuditStatus.SUCCESS) {
                logger.warn("File generated with zero records: {} for correlation: {}", fileName, correlationId);
            }
        }
        
        // Log file integrity information
        if (auditDetails.getFileHashSha256() != null && !auditDetails.getFileHashSha256().trim().isEmpty()) {
            logger.debug("Generated file {} hash: {} (correlation: {})", 
                fileName, auditDetails.getFileHashSha256(), correlationId);
        } else if (status == AuditStatus.SUCCESS) {
            logger.warn("File generated without hash calculation: {} for correlation: {}", fileName, correlationId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuditStatistics getAuditStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Generating audit statistics for period: {} to {}", startDate, endDate);
        
        // Validate input parameters
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        try {
            // Get all audit events in the date range
            List<AuditEvent> events = auditRepository.findByEventTimestampBetween(startDate, endDate);
            
            // Calculate basic statistics
            long totalEvents = events.size();
            long successfulEvents = events.stream().mapToLong(e -> e.getStatus() == AuditStatus.SUCCESS ? 1 : 0).sum();
            long failedEvents = events.stream().mapToLong(e -> e.getStatus() == AuditStatus.FAILURE ? 1 : 0).sum();
            long warningEvents = events.stream().mapToLong(e -> e.getStatus() == AuditStatus.WARNING ? 1 : 0).sum();
            
            // Create statistics object
            AuditStatistics statistics = new AuditStatistics(startDate, endDate, totalEvents, 
                successfulEvents, failedEvents, warningEvents);
            
            // Calculate additional statistics
            Map<String, Long> eventsBySourceSystem = events.stream()
                .filter(e -> e.getSourceSystem() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    AuditEvent::getSourceSystem, 
                    java.util.stream.Collectors.counting()));
            statistics.setEventsBySourceSystem(eventsBySourceSystem);
            
            Map<String, Long> eventsByModule = events.stream()
                .filter(e -> e.getModuleName() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    AuditEvent::getModuleName, 
                    java.util.stream.Collectors.counting()));
            statistics.setEventsByModule(eventsByModule);
            
            Map<CheckpointStage, Long> eventsByCheckpointStage = events.stream()
                .filter(e -> e.getCheckpointStage() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    AuditEvent::getCheckpointStage, 
                    java.util.stream.Collectors.counting()));
            statistics.setEventsByCheckpointStage(eventsByCheckpointStage);
            
            Map<AuditStatus, Long> eventsByStatus = events.stream()
                .filter(e -> e.getStatus() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    AuditEvent::getStatus, 
                    java.util.stream.Collectors.counting()));
            statistics.setEventsByStatus(eventsByStatus);
            
            // Calculate average events per day
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
            double averageEventsPerDay = daysBetween > 0 ? (double) totalEvents / daysBetween : 0.0;
            statistics.setAverageEventsPerDay(averageEventsPerDay);
            
            logger.info("Generated audit statistics for period {} to {}: {} total events, {} successful, {} failed, {} warnings", 
                startDate, endDate, totalEvents, successfulEvents, failedEvents, warningEvents);
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("Failed to generate audit statistics for period {} to {}", startDate, endDate, e);
            throw new AuditPersistenceException("Failed to generate audit statistics", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataDiscrepancy> identifyDataDiscrepancies(Map<String, String> filters) {
        logger.debug("Identifying data discrepancies with filters: {}", filters);
        
        try {
            List<DataDiscrepancy> discrepancies = new java.util.ArrayList<>();
            
            // Apply filters if provided
            String sourceSystem = filters != null ? filters.get("sourceSystem") : null;
            String moduleName = filters != null ? filters.get("moduleName") : null;
            String severityFilter = filters != null ? filters.get("severity") : null;
            
            // Get recent audit events for analysis (last 7 days by default)
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7);
            
            if (filters != null && filters.containsKey("startDate")) {
                try {
                    startDate = LocalDateTime.parse(filters.get("startDate"));
                } catch (Exception e) {
                    logger.warn("Invalid startDate filter format, using default: {}", e.getMessage());
                }
            }
            
            if (filters != null && filters.containsKey("endDate")) {
                try {
                    endDate = LocalDateTime.parse(filters.get("endDate"));
                } catch (Exception e) {
                    logger.warn("Invalid endDate filter format, using default: {}", e.getMessage());
                }
            }
            
            List<AuditEvent> events = auditRepository.findByEventTimestampBetween(startDate, endDate);
            
            // Group events by correlation ID for analysis
            Map<UUID, List<AuditEvent>> eventsByCorrelation = events.stream()
                .filter(e -> e.getCorrelationId() != null)
                .collect(java.util.stream.Collectors.groupingBy(AuditEvent::getCorrelationId));
            
            // Analyze each correlation group for discrepancies
            for (Map.Entry<UUID, List<AuditEvent>> entry : eventsByCorrelation.entrySet()) {
                UUID correlationId = entry.getKey();
                List<AuditEvent> correlationEvents = entry.getValue();
                
                // Apply source system filter
                if (sourceSystem != null && !sourceSystem.trim().isEmpty()) {
                    correlationEvents = correlationEvents.stream()
                        .filter(e -> sourceSystem.equals(e.getSourceSystem()))
                        .collect(java.util.stream.Collectors.toList());
                }
                
                // Apply module name filter
                if (moduleName != null && !moduleName.trim().isEmpty()) {
                    correlationEvents = correlationEvents.stream()
                        .filter(e -> moduleName.equals(e.getModuleName()))
                        .collect(java.util.stream.Collectors.toList());
                }
                
                if (correlationEvents.isEmpty()) {
                    continue;
                }
                
                // Check for missing checkpoint stages
                discrepancies.addAll(checkMissingCheckpoints(correlationId, correlationEvents));
                
                // Check for record count mismatches
                discrepancies.addAll(checkRecordCountMismatches(correlationId, correlationEvents));
                
                // Check for processing timeouts
                discrepancies.addAll(checkProcessingTimeouts(correlationId, correlationEvents));
                
                // Check for excessive failures
                discrepancies.addAll(checkExcessiveFailures(correlationId, correlationEvents));
            }
            
            // Apply severity filter
            if (severityFilter != null && !severityFilter.trim().isEmpty()) {
                try {
                    DataDiscrepancy.DiscrepancySeverity severity = DataDiscrepancy.DiscrepancySeverity.valueOf(severityFilter.toUpperCase());
                    discrepancies = discrepancies.stream()
                        .filter(d -> d.getSeverity() == severity)
                        .collect(java.util.stream.Collectors.toList());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid severity filter: {}", severityFilter);
                }
            }
            
            // Sort by severity and detection time
            discrepancies.sort((d1, d2) -> {
                int severityCompare = d2.getSeverity().compareTo(d1.getSeverity()); // High to Low
                if (severityCompare != 0) {
                    return severityCompare;
                }
                return d2.getDetectedAt().compareTo(d1.getDetectedAt()); // Recent first
            });
            
            logger.info("Identified {} data discrepancies with filters: {}", discrepancies.size(), filters);
            return discrepancies;
            
        } catch (Exception e) {
            logger.error("Failed to identify data discrepancies with filters: {}", filters, e);
            throw new AuditPersistenceException("Failed to identify data discrepancies", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DataDiscrepancy> getDataDiscrepancies(Map<String, String> filters) {
        logger.debug("Retrieving data discrepancies with filters: {}", filters);
        
        // For now, delegate to identifyDataDiscrepancies since we don't have a separate discrepancy storage
        // In a full implementation, this would query a dedicated discrepancy table
        return identifyDataDiscrepancies(filters);
    }

    /**
     * Checks for missing checkpoint stages in a correlation group
     */
    private List<DataDiscrepancy> checkMissingCheckpoints(UUID correlationId, List<AuditEvent> events) {
        List<DataDiscrepancy> discrepancies = new java.util.ArrayList<>();
        
        Set<CheckpointStage> presentStages = events.stream()
            .map(AuditEvent::getCheckpointStage)
            .filter(java.util.Objects::nonNull)
            .collect(java.util.stream.Collectors.toSet());
        
        // Expected stages for a complete pipeline run
        Set<CheckpointStage> expectedStages = Set.of(
            CheckpointStage.RHEL_LANDING,
            CheckpointStage.SQLLOADER_START,
            CheckpointStage.SQLLOADER_COMPLETE,
            CheckpointStage.LOGIC_APPLIED,
            CheckpointStage.FILE_GENERATED
        );
        
        for (CheckpointStage expectedStage : expectedStages) {
            if (!presentStages.contains(expectedStage)) {
                String sourceSystem = events.stream()
                    .map(AuditEvent::getSourceSystem)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse("UNKNOWN");
                
                DataDiscrepancy discrepancy = new DataDiscrepancy(
                    correlationId, sourceSystem, "PIPELINE_MONITOR",
                    DataDiscrepancy.DiscrepancyType.MISSING_AUDIT_EVENTS,
                    expectedStage.name(), "MISSING"
                );
                discrepancy.setCheckpointStage(expectedStage);
                discrepancy.setDescription("Missing audit event for checkpoint stage: " + expectedStage);
                discrepancy.setSeverity(DataDiscrepancy.DiscrepancySeverity.HIGH);
                discrepancies.add(discrepancy);
            }
        }
        
        return discrepancies;
    }

    /**
     * Checks for record count mismatches between stages
     */
    private List<DataDiscrepancy> checkRecordCountMismatches(UUID correlationId, List<AuditEvent> events) {
        List<DataDiscrepancy> discrepancies = new java.util.ArrayList<>();
        
        // Extract record counts from audit details
        Map<CheckpointStage, Long> recordCounts = new java.util.HashMap<>();
        
        for (AuditEvent event : events) {
            if (event.getDetailsJson() != null && event.getCheckpointStage() != null) {
                try {
                    AuditDetails details = objectMapper.readValue(event.getDetailsJson(), AuditDetails.class);
                    Long recordCount = details.getRecordCount();
                    if (recordCount != null) {
                        recordCounts.put(event.getCheckpointStage(), recordCount);
                    }
                } catch (Exception e) {
                    logger.debug("Could not parse audit details for record count analysis: {}", e.getMessage());
                }
            }
        }
        
        // Compare record counts between stages
        Long inputCount = recordCounts.get(CheckpointStage.RHEL_LANDING);
        Long loadedCount = recordCounts.get(CheckpointStage.SQLLOADER_COMPLETE);
        Long processedCount = recordCounts.get(CheckpointStage.LOGIC_APPLIED);
        Long outputCount = recordCounts.get(CheckpointStage.FILE_GENERATED);
        
        if (inputCount != null && loadedCount != null && !inputCount.equals(loadedCount)) {
            String sourceSystem = events.stream()
                .map(AuditEvent::getSourceSystem)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse("UNKNOWN");
            
            DataDiscrepancy discrepancy = new DataDiscrepancy(
                correlationId, sourceSystem, "SQL_LOADER",
                DataDiscrepancy.DiscrepancyType.RECORD_COUNT_MISMATCH,
                inputCount.toString(), loadedCount.toString()
            );
            discrepancy.setCheckpointStage(CheckpointStage.SQLLOADER_COMPLETE);
            discrepancy.setDescription("Record count mismatch between file input and SQL*Loader output");
            discrepancies.add(discrepancy);
        }
        
        return discrepancies;
    }

    /**
     * Checks for processing timeouts based on time gaps between stages
     */
    private List<DataDiscrepancy> checkProcessingTimeouts(UUID correlationId, List<AuditEvent> events) {
        List<DataDiscrepancy> discrepancies = new java.util.ArrayList<>();
        
        // Sort events by timestamp
        List<AuditEvent> sortedEvents = events.stream()
            .filter(e -> e.getEventTimestamp() != null)
            .sorted((e1, e2) -> e1.getEventTimestamp().compareTo(e2.getEventTimestamp()))
            .collect(java.util.stream.Collectors.toList());
        
        // Check for large time gaps (more than 1 hour between consecutive stages)
        for (int i = 1; i < sortedEvents.size(); i++) {
            AuditEvent prevEvent = sortedEvents.get(i - 1);
            AuditEvent currentEvent = sortedEvents.get(i);
            
            long minutesBetween = java.time.temporal.ChronoUnit.MINUTES.between(
                prevEvent.getEventTimestamp(), currentEvent.getEventTimestamp());
            
            if (minutesBetween > 60) { // More than 1 hour
                String sourceSystem = currentEvent.getSourceSystem() != null ? 
                    currentEvent.getSourceSystem() : "UNKNOWN";
                
                DataDiscrepancy discrepancy = new DataDiscrepancy(
                    correlationId, sourceSystem, currentEvent.getModuleName(),
                    DataDiscrepancy.DiscrepancyType.PROCESSING_TIMEOUT,
                    "< 60 minutes", minutesBetween + " minutes"
                );
                discrepancy.setCheckpointStage(currentEvent.getCheckpointStage());
                discrepancy.setDescription("Processing timeout detected between stages: " + 
                    prevEvent.getCheckpointStage() + " -> " + currentEvent.getCheckpointStage());
                discrepancy.setSeverity(DataDiscrepancy.DiscrepancySeverity.MEDIUM);
                discrepancies.add(discrepancy);
            }
        }
        
        return discrepancies;
    }

    /**
     * Checks for excessive failures in a correlation group
     */
    private List<DataDiscrepancy> checkExcessiveFailures(UUID correlationId, List<AuditEvent> events) {
        List<DataDiscrepancy> discrepancies = new java.util.ArrayList<>();
        
        long failureCount = events.stream()
            .mapToLong(e -> e.getStatus() == AuditStatus.FAILURE ? 1 : 0)
            .sum();
        
        long totalCount = events.size();
        
        if (failureCount > 0 && totalCount > 0) {
            double failureRate = (double) failureCount / totalCount;
            
            if (failureRate > 0.5) { // More than 50% failures
                String sourceSystem = events.stream()
                    .map(AuditEvent::getSourceSystem)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse("UNKNOWN");
                
                DataDiscrepancy discrepancy = new DataDiscrepancy(
                    correlationId, sourceSystem, "PIPELINE_MONITOR",
                    DataDiscrepancy.DiscrepancyType.DATA_INTEGRITY_VIOLATION,
                    "< 50% failures", String.format("%.1f%% failures", failureRate * 100)
                );
                discrepancy.setDescription("Excessive failure rate detected in pipeline run");
                discrepancy.setSeverity(DataDiscrepancy.DiscrepancySeverity.HIGH);
                discrepancies.add(discrepancy);
            }
        }
        
        return discrepancies;
    }
}