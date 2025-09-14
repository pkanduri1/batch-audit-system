package com.company.audit.model.dto;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditEventDTO record using Java 17+ features.
 * Tests JSON serialization, entity conversion, and builder pattern functionality.
 */
class AuditEventDTOTest {

    private ObjectMapper objectMapper;
    private UUID testAuditId;
    private UUID testCorrelationId;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        testAuditId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testCorrelationId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
    }

    @Test
    @DisplayName("Should create AuditEventDTO with all fields using record constructor")
    void shouldCreateAuditEventDTOWithAllFields() {
        // Given
        AuditEventDTO dto = new AuditEventDTO(
            testAuditId,
            testCorrelationId,
            "MAINFRAME_SYSTEM_A",
            "FILE_TRANSFER",
            "DAILY_BATCH_PROCESS",
            "CUSTOMER_DATA.csv",
            "STAGING.CUSTOMER_STAGING",
            "BATCH_20240115_001",
            CheckpointStage.RHEL_LANDING,
            testTimestamp,
            AuditStatus.SUCCESS,
            "File successfully transferred",
            "{\"fileSize\": 1024000, \"recordCount\": 5000}"
        );

        // Then
        assertNotNull(dto);
        assertEquals(testAuditId, dto.auditId());
        assertEquals(testCorrelationId, dto.correlationId());
        assertEquals("MAINFRAME_SYSTEM_A", dto.sourceSystem());
        assertEquals("FILE_TRANSFER", dto.moduleName());
        assertEquals("DAILY_BATCH_PROCESS", dto.processName());
        assertEquals("CUSTOMER_DATA.csv", dto.sourceEntity());
        assertEquals("STAGING.CUSTOMER_STAGING", dto.destinationEntity());
        assertEquals("BATCH_20240115_001", dto.keyIdentifier());
        assertEquals(CheckpointStage.RHEL_LANDING, dto.checkpointStage());
        assertEquals(testTimestamp, dto.eventTimestamp());
        assertEquals(AuditStatus.SUCCESS, dto.status());
        assertEquals("File successfully transferred", dto.message());
        assertEquals("{\"fileSize\": 1024000, \"recordCount\": 5000}", dto.detailsJson());
    }

    @Test
    @DisplayName("Should create AuditEventDTO from AuditEvent entity")
    void shouldCreateDTOFromEntity() {
        // Given
        AuditEvent entity = AuditEvent.builder()
            .auditId(testAuditId)
            .correlationId(testCorrelationId)
            .sourceSystem("MAINFRAME_SYSTEM_A")
            .moduleName("FILE_TRANSFER")
            .processName("DAILY_BATCH_PROCESS")
            .sourceEntity("CUSTOMER_DATA.csv")
            .destinationEntity("STAGING.CUSTOMER_STAGING")
            .keyIdentifier("BATCH_20240115_001")
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(testTimestamp)
            .status(AuditStatus.SUCCESS)
            .message("File successfully transferred")
            .detailsJson("{\"fileSize\": 1024000, \"recordCount\": 5000}")
            .build();

        // When
        AuditEventDTO dto = AuditEventDTO.fromEntity(entity);

        // Then
        assertNotNull(dto);
        assertEquals(entity.getAuditId(), dto.auditId());
        assertEquals(entity.getCorrelationId(), dto.correlationId());
        assertEquals(entity.getSourceSystem(), dto.sourceSystem());
        assertEquals(entity.getModuleName(), dto.moduleName());
        assertEquals(entity.getProcessName(), dto.processName());
        assertEquals(entity.getSourceEntity(), dto.sourceEntity());
        assertEquals(entity.getDestinationEntity(), dto.destinationEntity());
        assertEquals(entity.getKeyIdentifier(), dto.keyIdentifier());
        assertEquals(entity.getCheckpointStage(), dto.checkpointStage());
        assertEquals(entity.getEventTimestamp(), dto.eventTimestamp());
        assertEquals(entity.getStatus(), dto.status());
        assertEquals(entity.getMessage(), dto.message());
        assertEquals(entity.getDetailsJson(), dto.detailsJson());
    }

    @Test
    @DisplayName("Should throw exception when creating DTO from null entity")
    void shouldThrowExceptionWhenCreatingDTOFromNullEntity() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> AuditEventDTO.fromEntity(null)
        );
        
        assertEquals("AuditEvent cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should convert DTO back to AuditEvent entity")
    void shouldConvertDTOToEntity() {
        // Given
        AuditEventDTO dto = new AuditEventDTO(
            testAuditId,
            testCorrelationId,
            "MAINFRAME_SYSTEM_A",
            "FILE_TRANSFER",
            "DAILY_BATCH_PROCESS",
            "CUSTOMER_DATA.csv",
            "STAGING.CUSTOMER_STAGING",
            "BATCH_20240115_001",
            CheckpointStage.RHEL_LANDING,
            testTimestamp,
            AuditStatus.SUCCESS,
            "File successfully transferred",
            "{\"fileSize\": 1024000, \"recordCount\": 5000}"
        );

        // When
        AuditEvent entity = dto.toEntity();

        // Then
        assertNotNull(entity);
        assertEquals(dto.auditId(), entity.getAuditId());
        assertEquals(dto.correlationId(), entity.getCorrelationId());
        assertEquals(dto.sourceSystem(), entity.getSourceSystem());
        assertEquals(dto.moduleName(), entity.getModuleName());
        assertEquals(dto.processName(), entity.getProcessName());
        assertEquals(dto.sourceEntity(), entity.getSourceEntity());
        assertEquals(dto.destinationEntity(), entity.getDestinationEntity());
        assertEquals(dto.keyIdentifier(), entity.getKeyIdentifier());
        assertEquals(dto.checkpointStage(), entity.getCheckpointStage());
        assertEquals(dto.eventTimestamp(), entity.getEventTimestamp());
        assertEquals(dto.status(), entity.getStatus());
        assertEquals(dto.message(), entity.getMessage());
        assertEquals(dto.detailsJson(), entity.getDetailsJson());
    }

    @Test
    @DisplayName("Should create AuditEventDTO using builder pattern")
    void shouldCreateDTOUsingBuilder() {
        // When
        AuditEventDTO dto = AuditEventDTO.builder()
            .auditId(testAuditId)
            .correlationId(testCorrelationId)
            .sourceSystem("MAINFRAME_SYSTEM_A")
            .moduleName("FILE_TRANSFER")
            .processName("DAILY_BATCH_PROCESS")
            .sourceEntity("CUSTOMER_DATA.csv")
            .destinationEntity("STAGING.CUSTOMER_STAGING")
            .keyIdentifier("BATCH_20240115_001")
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(testTimestamp)
            .status(AuditStatus.SUCCESS)
            .message("File successfully transferred")
            .detailsJson("{\"fileSize\": 1024000, \"recordCount\": 5000}")
            .build();

        // Then
        assertNotNull(dto);
        assertEquals(testAuditId, dto.auditId());
        assertEquals(testCorrelationId, dto.correlationId());
        assertEquals("MAINFRAME_SYSTEM_A", dto.sourceSystem());
        assertEquals("FILE_TRANSFER", dto.moduleName());
        assertEquals(CheckpointStage.RHEL_LANDING, dto.checkpointStage());
        assertEquals(AuditStatus.SUCCESS, dto.status());
    }

    @Test
    @DisplayName("Should serialize AuditEventDTO to JSON correctly")
    void shouldSerializeToJSON() throws Exception {
        // Given
        AuditEventDTO dto = new AuditEventDTO(
            testAuditId,
            testCorrelationId,
            "MAINFRAME_SYSTEM_A",
            "FILE_TRANSFER",
            "DAILY_BATCH_PROCESS",
            "CUSTOMER_DATA.csv",
            "STAGING.CUSTOMER_STAGING",
            "BATCH_20240115_001",
            CheckpointStage.RHEL_LANDING,
            testTimestamp,
            AuditStatus.SUCCESS,
            "File successfully transferred",
            "{\"fileSize\": 1024000, \"recordCount\": 5000}"
        );

        // When
        String json = objectMapper.writeValueAsString(dto);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"auditId\":\"550e8400-e29b-41d4-a716-446655440000\""));
        assertTrue(json.contains("\"correlationId\":\"123e4567-e89b-12d3-a456-426614174000\""));
        assertTrue(json.contains("\"sourceSystem\":\"MAINFRAME_SYSTEM_A\""));
        assertTrue(json.contains("\"moduleName\":\"FILE_TRANSFER\""));
        assertTrue(json.contains("\"checkpointStage\":\"RHEL_LANDING\""));
        assertTrue(json.contains("\"status\":\"SUCCESS\""));
        assertTrue(json.contains("\"eventTimestamp\":\"2024-01-15T10:30:00\""));
    }

    @Test
    @DisplayName("Should deserialize JSON to AuditEventDTO correctly")
    void shouldDeserializeFromJSON() throws Exception {
        // Given
        String json = """
            {
                "auditId": "550e8400-e29b-41d4-a716-446655440000",
                "correlationId": "123e4567-e89b-12d3-a456-426614174000",
                "sourceSystem": "MAINFRAME_SYSTEM_A",
                "moduleName": "FILE_TRANSFER",
                "processName": "DAILY_BATCH_PROCESS",
                "sourceEntity": "CUSTOMER_DATA.csv",
                "destinationEntity": "STAGING.CUSTOMER_STAGING",
                "keyIdentifier": "BATCH_20240115_001",
                "checkpointStage": "RHEL_LANDING",
                "eventTimestamp": "2024-01-15T10:30:00",
                "status": "SUCCESS",
                "message": "File successfully transferred",
                "detailsJson": "{\\"fileSize\\": 1024000, \\"recordCount\\": 5000}"
            }
            """;

        // When
        AuditEventDTO dto = objectMapper.readValue(json, AuditEventDTO.class);

        // Then
        assertNotNull(dto);
        assertEquals(testAuditId, dto.auditId());
        assertEquals(testCorrelationId, dto.correlationId());
        assertEquals("MAINFRAME_SYSTEM_A", dto.sourceSystem());
        assertEquals("FILE_TRANSFER", dto.moduleName());
        assertEquals("DAILY_BATCH_PROCESS", dto.processName());
        assertEquals("CUSTOMER_DATA.csv", dto.sourceEntity());
        assertEquals("STAGING.CUSTOMER_STAGING", dto.destinationEntity());
        assertEquals("BATCH_20240115_001", dto.keyIdentifier());
        assertEquals(CheckpointStage.RHEL_LANDING, dto.checkpointStage());
        assertEquals(testTimestamp, dto.eventTimestamp());
        assertEquals(AuditStatus.SUCCESS, dto.status());
        assertEquals("File successfully transferred", dto.message());
        assertEquals("{\"fileSize\": 1024000, \"recordCount\": 5000}", dto.detailsJson());
    }

    @Test
    @DisplayName("Should handle null optional fields correctly")
    void shouldHandleNullOptionalFields() {
        // Given
        AuditEventDTO dto = new AuditEventDTO(
            testAuditId,
            testCorrelationId,
            "MAINFRAME_SYSTEM_A",
            "FILE_TRANSFER",
            null, // processName
            null, // sourceEntity
            null, // destinationEntity
            null, // keyIdentifier
            CheckpointStage.RHEL_LANDING,
            testTimestamp,
            AuditStatus.SUCCESS,
            null, // message
            null  // detailsJson
        );

        // Then
        assertNotNull(dto);
        assertEquals(testAuditId, dto.auditId());
        assertEquals(testCorrelationId, dto.correlationId());
        assertEquals("MAINFRAME_SYSTEM_A", dto.sourceSystem());
        assertEquals("FILE_TRANSFER", dto.moduleName());
        assertNull(dto.processName());
        assertNull(dto.sourceEntity());
        assertNull(dto.destinationEntity());
        assertNull(dto.keyIdentifier());
        assertEquals(CheckpointStage.RHEL_LANDING, dto.checkpointStage());
        assertEquals(testTimestamp, dto.eventTimestamp());
        assertEquals(AuditStatus.SUCCESS, dto.status());
        assertNull(dto.message());
        assertNull(dto.detailsJson());
    }

    @Test
    @DisplayName("Should maintain record immutability")
    void shouldMaintainRecordImmutability() {
        // Given
        AuditEventDTO dto = new AuditEventDTO(
            testAuditId,
            testCorrelationId,
            "MAINFRAME_SYSTEM_A",
            "FILE_TRANSFER",
            "DAILY_BATCH_PROCESS",
            "CUSTOMER_DATA.csv",
            "STAGING.CUSTOMER_STAGING",
            "BATCH_20240115_001",
            CheckpointStage.RHEL_LANDING,
            testTimestamp,
            AuditStatus.SUCCESS,
            "File successfully transferred",
            "{\"fileSize\": 1024000, \"recordCount\": 5000}"
        );

        // When - trying to access fields (records are immutable by design)
        UUID auditId = dto.auditId();
        String sourceSystem = dto.sourceSystem();

        // Then - values should be accessible but immutable
        assertEquals(testAuditId, auditId);
        assertEquals("MAINFRAME_SYSTEM_A", sourceSystem);
        
        // Records automatically provide equals, hashCode, and toString
        assertNotNull(dto.toString());
        assertEquals(dto, dto); // equals with itself
        assertTrue(dto.hashCode() != 0); // hashCode should be computed
    }
}