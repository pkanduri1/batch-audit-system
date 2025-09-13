package com.company.audit.repository;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository class for managing AuditEvent persistence using JdbcTemplate.
 * Provides basic CRUD operations for audit events in the Oracle database.
 */
@Repository
public class AuditRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AuditEvent> auditEventRowMapper;

    /**
     * Constructor with JdbcTemplate dependency injection
     * @param jdbcTemplate the JdbcTemplate instance for database operations
     */
    public AuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditEventRowMapper = new AuditEventRowMapper();
    }

    /**
     * Saves an audit event to the database
     * @param auditEvent the audit event to save
     * @throws RuntimeException if the save operation fails
     */
    public void save(AuditEvent auditEvent) {
        String sql = """
            INSERT INTO PIPELINE_AUDIT_LOG (
                AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME, 
                PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            jdbcTemplate.update(sql,
                auditEvent.getAuditId() != null ? auditEvent.getAuditId().toString() : null,
                auditEvent.getCorrelationId() != null ? auditEvent.getCorrelationId().toString() : null,
                auditEvent.getSourceSystem(),
                auditEvent.getModuleName(),
                auditEvent.getProcessName(),
                auditEvent.getSourceEntity(),
                auditEvent.getDestinationEntity(),
                auditEvent.getKeyIdentifier(),
                auditEvent.getCheckpointStage() != null ? auditEvent.getCheckpointStage().name() : null,
                auditEvent.getEventTimestamp() != null ? Timestamp.valueOf(auditEvent.getEventTimestamp()) : null,
                auditEvent.getStatus() != null ? auditEvent.getStatus().name() : null,
                auditEvent.getMessage(),
                auditEvent.getDetailsJson()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to save audit event with ID: " + auditEvent.getAuditId(), e);
        }
    }

    /**
     * Finds an audit event by its ID
     * @param auditId the audit ID to search for
     * @return Optional containing the audit event if found, empty otherwise
     */
    public Optional<AuditEvent> findById(UUID auditId) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE AUDIT_ID = ?
            """;

        try {
            AuditEvent auditEvent = jdbcTemplate.queryForObject(sql, auditEventRowMapper, auditId.toString());
            return Optional.ofNullable(auditEvent);
        } catch (Exception e) {
            // Return empty Optional if no record found or query fails
            return Optional.empty();
        }
    }

    /**
     * Finds all audit events for a given correlation ID, ordered by event timestamp
     * @param correlationId the correlation ID to search for
     * @return List of audit events ordered by event timestamp (ascending)
     */
    public List<AuditEvent> findByCorrelationIdOrderByEventTimestamp(UUID correlationId) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE CORRELATION_ID = ?
            ORDER BY EVENT_TIMESTAMP ASC
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, correlationId.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events for correlation ID: " + correlationId, e);
        }
    }

    /**
     * Counts the number of audit events for a given correlation ID
     * @param correlationId the correlation ID to count events for
     * @return the number of audit events with the specified correlation ID
     */
    public long countByCorrelationId(UUID correlationId) {
        String sql = """
            SELECT COUNT(*) 
            FROM PIPELINE_AUDIT_LOG 
            WHERE CORRELATION_ID = ?
            """;

        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, correlationId.toString());
            return count != null ? count : 0L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to count audit events for correlation ID: " + correlationId, e);
        }
    }

    /**
     * Finds audit events by source system and checkpoint stage
     * @param sourceSystem the source system to filter by
     * @param checkpointStage the checkpoint stage to filter by
     * @return List of audit events matching the criteria, ordered by event timestamp
     */
    public List<AuditEvent> findBySourceSystemAndCheckpointStage(String sourceSystem, CheckpointStage checkpointStage) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE SOURCE_SYSTEM = ? AND CHECKPOINT_STAGE = ?
            ORDER BY EVENT_TIMESTAMP ASC
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, sourceSystem, checkpointStage.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events for source system: " + sourceSystem + 
                " and checkpoint stage: " + checkpointStage, e);
        }
    }

    /**
     * Finds audit events by module name and status
     * @param moduleName the module name to filter by
     * @param status the audit status to filter by
     * @return List of audit events matching the criteria, ordered by event timestamp
     */
    public List<AuditEvent> findByModuleNameAndStatus(String moduleName, AuditStatus status) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE MODULE_NAME = ? AND STATUS = ?
            ORDER BY EVENT_TIMESTAMP ASC
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, moduleName, status.name());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events for module name: " + moduleName + 
                " and status: " + status, e);
        }
    }

    /**
     * Finds audit events within a specific date range
     * @param startDateTime the start of the date range (inclusive)
     * @param endDateTime the end of the date range (inclusive)
     * @return List of audit events within the specified date range, ordered by event timestamp
     */
    public List<AuditEvent> findByEventTimestampBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE EVENT_TIMESTAMP >= ? AND EVENT_TIMESTAMP <= ?
            ORDER BY EVENT_TIMESTAMP ASC
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, 
                Timestamp.valueOf(startDateTime), 
                Timestamp.valueOf(endDateTime));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events between " + startDateTime + 
                " and " + endDateTime, e);
        }
    }

    /**
     * Finds audit events within a specific date range with pagination support
     * @param startDateTime the start of the date range (inclusive)
     * @param endDateTime the end of the date range (inclusive)
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return List of audit events within the specified date range with pagination
     */
    public List<AuditEvent> findByEventTimestampBetween(LocalDateTime startDateTime, LocalDateTime endDateTime, 
                                                       int offset, int limit) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE EVENT_TIMESTAMP >= ? AND EVENT_TIMESTAMP <= ?
            ORDER BY EVENT_TIMESTAMP ASC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, 
                Timestamp.valueOf(startDateTime), 
                Timestamp.valueOf(endDateTime),
                offset,
                limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events between " + startDateTime + 
                " and " + endDateTime + " with pagination (offset: " + offset + ", limit: " + limit + ")", e);
        }
    }

    /**
     * Counts audit events for a specific correlation ID and status
     * @param correlationId the correlation ID to filter by
     * @param status the audit status to filter by
     * @return the number of audit events matching the criteria
     */
    public long countByCorrelationIdAndStatus(UUID correlationId, AuditStatus status) {
        String sql = """
            SELECT COUNT(*) 
            FROM PIPELINE_AUDIT_LOG 
            WHERE CORRELATION_ID = ? AND STATUS = ?
            """;

        try {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, correlationId.toString(), status.name());
            return count != null ? count : 0L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to count audit events for correlation ID: " + correlationId + 
                " and status: " + status, e);
        }
    }

    /**
     * Finds all audit events with pagination support
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return List of audit events with pagination, ordered by event timestamp (descending for recent first)
     */
    public List<AuditEvent> findAllWithPagination(int offset, int limit) {
        String sql = """
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            ORDER BY EVENT_TIMESTAMP DESC
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        try {
            return jdbcTemplate.query(sql, auditEventRowMapper, offset, limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events with pagination (offset: " + offset + 
                ", limit: " + limit + ")", e);
        }
    }

    /**
     * Finds audit events with optional filters and pagination support
     * @param sourceSystem optional source system filter (null for no filter)
     * @param moduleName optional module name filter (null for no filter)
     * @param status optional audit status filter (null for no filter)
     * @param checkpointStage optional checkpoint stage filter (null for no filter)
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return List of audit events matching the criteria with pagination
     */
    public List<AuditEvent> findWithFiltersAndPagination(String sourceSystem, String moduleName, AuditStatus status,
                                                        CheckpointStage checkpointStage, int offset, int limit) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("""
            SELECT AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME,
                   PROCESS_NAME, SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER,
                   CHECKPOINT_STAGE, EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON
            FROM PIPELINE_AUDIT_LOG 
            WHERE 1=1
            """);

        List<Object> parameters = new java.util.ArrayList<>();

        if (sourceSystem != null && !sourceSystem.trim().isEmpty()) {
            sqlBuilder.append(" AND SOURCE_SYSTEM = ?");
            parameters.add(sourceSystem);
        }

        if (moduleName != null && !moduleName.trim().isEmpty()) {
            sqlBuilder.append(" AND MODULE_NAME = ?");
            parameters.add(moduleName);
        }

        if (status != null) {
            sqlBuilder.append(" AND STATUS = ?");
            parameters.add(status.name());
        }

        if (checkpointStage != null) {
            sqlBuilder.append(" AND CHECKPOINT_STAGE = ?");
            parameters.add(checkpointStage.name());
        }

        sqlBuilder.append(" ORDER BY EVENT_TIMESTAMP DESC");
        sqlBuilder.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        parameters.add(offset);
        parameters.add(limit);

        try {
            return jdbcTemplate.query(sqlBuilder.toString(), auditEventRowMapper, parameters.toArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit events with filters and pagination", e);
        }
    }

    /**
     * Counts audit events with optional filters
     * @param sourceSystem optional source system filter (null for no filter)
     * @param moduleName optional module name filter (null for no filter)
     * @param status optional audit status filter (null for no filter)
     * @param checkpointStage optional checkpoint stage filter (null for no filter)
     * @return count of audit events matching the criteria
     */
    public long countWithFilters(String sourceSystem, String moduleName, AuditStatus status,
                                CheckpointStage checkpointStage) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT COUNT(*) FROM PIPELINE_AUDIT_LOG WHERE 1=1");

        List<Object> parameters = new java.util.ArrayList<>();

        if (sourceSystem != null && !sourceSystem.trim().isEmpty()) {
            sqlBuilder.append(" AND SOURCE_SYSTEM = ?");
            parameters.add(sourceSystem);
        }

        if (moduleName != null && !moduleName.trim().isEmpty()) {
            sqlBuilder.append(" AND MODULE_NAME = ?");
            parameters.add(moduleName);
        }

        if (status != null) {
            sqlBuilder.append(" AND STATUS = ?");
            parameters.add(status.name());
        }

        if (checkpointStage != null) {
            sqlBuilder.append(" AND CHECKPOINT_STAGE = ?");
            parameters.add(checkpointStage.name());
        }

        try {
            Long count = jdbcTemplate.queryForObject(sqlBuilder.toString(), Long.class, parameters.toArray());
            return count != null ? count : 0L;
        } catch (Exception e) {
            throw new RuntimeException("Failed to count audit events with filters", e);
        }
    }

    /**
     * RowMapper implementation for mapping ResultSet to AuditEvent objects
     */
    private static class AuditEventRowMapper implements RowMapper<AuditEvent> {
        
        @Override
        public AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AuditEvent.builder()
                .auditId(parseUUID(rs.getString("AUDIT_ID")))
                .correlationId(parseUUID(rs.getString("CORRELATION_ID")))
                .sourceSystem(rs.getString("SOURCE_SYSTEM"))
                .moduleName(rs.getString("MODULE_NAME"))
                .processName(rs.getString("PROCESS_NAME"))
                .sourceEntity(rs.getString("SOURCE_ENTITY"))
                .destinationEntity(rs.getString("DESTINATION_ENTITY"))
                .keyIdentifier(rs.getString("KEY_IDENTIFIER"))
                .checkpointStage(parseCheckpointStage(rs.getString("CHECKPOINT_STAGE")))
                .eventTimestamp(parseTimestamp(rs.getTimestamp("EVENT_TIMESTAMP")))
                .status(parseAuditStatus(rs.getString("STATUS")))
                .message(rs.getString("MESSAGE"))
                .detailsJson(rs.getString("DETAILS_JSON"))
                .build();
        }

        /**
         * Safely parses a UUID string, returning null if invalid
         */
        private UUID parseUUID(String uuidString) {
            if (uuidString == null || uuidString.trim().isEmpty()) {
                return null;
            }
            try {
                return UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Safely parses a CheckpointStage enum value
         */
        private CheckpointStage parseCheckpointStage(String stageString) {
            if (stageString == null || stageString.trim().isEmpty()) {
                return null;
            }
            try {
                return CheckpointStage.valueOf(stageString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Safely parses an AuditStatus enum value
         */
        private AuditStatus parseAuditStatus(String statusString) {
            if (statusString == null || statusString.trim().isEmpty()) {
                return null;
            }
            try {
                return AuditStatus.valueOf(statusString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * Safely converts Timestamp to LocalDateTime
         */
        private LocalDateTime parseTimestamp(Timestamp timestamp) {
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        }
    }
}