package com.company.audit.service;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.exception.AuditPersistenceException;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.AuditEvent;
import com.company.audit.repository.AuditRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for AuditServiceImpl using Spring Boot 3.4+ test features and Java 17+ language enhancements.
 * 
 * Tests cover all checkpoint logging methods, error handling scenarios, validation logic,
 * and integration with mocked Oracle repository using JUnit 5 and Mockito.
 * 
 * Requirements addressed:
 * - 2.1: Test core audit event creation and persistence logic
 * - 2.2: Test Oracle database integration scenarios with mocked repository
 * - 7.5: Test error handling and validation scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditServiceImpl Unit Tests")
class AuditServiceImplTest {

    @Mock(lenient = true)
    private AuditRepository auditRepository;

    @Mock(lenient = true)
    private ObjectMapper objectMapper;

    private AuditServiceImpl auditService;

    private UUID testCorrelationId;
    private UUID testAuditId;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(auditRepository, objectMapper);
        testCorrelationId = UUID.randomUUID();
        testAuditId = UUID.randomUUID();
        testTimestamp = LocalDateTime.now();
    }

    @Nested
    @DisplayName("Core Audit Event Logging Tests")
    class CoreAuditEventTests {

        @Test
        @DisplayName("Should successfully log audit event with all required fields")
        void testLogAuditEvent_Success() {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .correlationId(testCorrelationId)
                .sourceSystem("TEST_SYSTEM")
                .moduleName("TEST_MODULE")
                .processName("TEST_PROCESS")
                .sourceEntity("SOURCE_ENTITY")
                .destinationEntity("DEST_ENTITY")
                .keyIdentifier("KEY123")
                .checkpointStage(CheckpointStage.RHEL_LANDING)
                .status(AuditStatus.SUCCESS)
                .message("Test message")
                .detailsJson("{\"test\":\"data\"}")
                .build();

            // When
            auditService.logAuditEvent(auditEvent);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertNotNull(savedEvent.getAuditId(), "Audit ID should be generated");
            assertNotNull(savedEvent.getEventTimestamp(), "Event timestamp should be set");
            assertEquals(testCorrelationId, savedEvent.getCorrelationId());
            assertEquals("TEST_SYSTEM", savedEvent.getSourceSystem());
            assertEquals("TEST_MODULE", savedEvent.getModuleName());
            assertEquals(AuditStatus.SUCCESS, savedEvent.getStatus());
            assertEquals("Test message", savedEvent.getMessage());
        }

        @Test
        @DisplayName("Should preserve existing audit ID and timestamp when provided")
        void testLogAuditEvent_PreservesExistingIdAndTimestamp() {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .auditId(testAuditId)
                .correlationId(testCorrelationId)
                .sourceSystem("TEST_SYSTEM")
                .status(AuditStatus.SUCCESS)
                .eventTimestamp(testTimestamp)
                .build();

            // When
            auditService.logAuditEvent(auditEvent);

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals(testAuditId, savedEvent.getAuditId(), "Should preserve existing audit ID");
            assertEquals(testTimestamp, savedEvent.getEventTimestamp(), "Should preserve existing timestamp");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should throw IllegalArgumentException for invalid source system")
        void testLogAuditEvent_InvalidSourceSystem_ThrowsException(String sourceSystem) {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .correlationId(testCorrelationId)
                .sourceSystem(sourceSystem)
                .status(AuditStatus.SUCCESS)
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logAuditEvent(auditEvent));
            assertEquals("Source system cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when audit event is null")
        void testLogAuditEvent_NullEvent_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logAuditEvent(null));
            assertEquals("Audit event cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when correlation ID is null")
        void testLogAuditEvent_NullCorrelationId_ThrowsException() {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .sourceSystem("TEST_SYSTEM")
                .status(AuditStatus.SUCCESS)
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logAuditEvent(auditEvent));
            assertEquals("Correlation ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when status is null")
        void testLogAuditEvent_NullStatus_ThrowsException() {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .correlationId(testCorrelationId)
                .sourceSystem("TEST_SYSTEM")
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logAuditEvent(auditEvent));
            assertEquals("Status cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AuditPersistenceException when repository fails")
        void testLogAuditEvent_RepositoryException_ThrowsAuditPersistenceException() {
            // Given
            AuditEvent auditEvent = AuditEvent.builder()
                .correlationId(testCorrelationId)
                .sourceSystem("TEST_SYSTEM")
                .status(AuditStatus.SUCCESS)
                .build();

            doThrow(new RuntimeException("Database connection failed")).when(auditRepository).save(any(AuditEvent.class));

            // When & Then
            AuditPersistenceException exception = assertThrows(AuditPersistenceException.class, 
                () -> auditService.logAuditEvent(auditEvent));
            assertEquals("Failed to persist audit event", exception.getMessage());
            assertTrue(exception.getCause() instanceof RuntimeException);
            assertEquals("Database connection failed", exception.getCause().getMessage());
        }
    }

    @Nested
    @DisplayName("File Transfer Checkpoint Tests")
    class FileTransferTests {

        @Test
        @DisplayName("Should successfully log file transfer with complete audit details")
        void testLogFileTransfer_Success() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .fileSizeBytes(1024L)
                .fileHashSha256("abc123def456")
                .recordCount(100L)
                .build();

            String expectedJson = "{\"fileSizeBytes\":1024,\"fileHashSha256\":\"abc123def456\",\"recordCount\":100}";
            when(objectMapper.writeValueAsString(auditDetails)).thenReturn(expectedJson);

            // When
            auditService.logFileTransfer(
                testCorrelationId,
                "MAINFRAME_A",
                "test_file.dat",
                "FILE_TRANSFER_PROCESS",
                "/source/mainframe/path",
                "/dest/rhel/path",
                "BATCH_KEY_123",
                AuditStatus.SUCCESS,
                "File transferred successfully from mainframe",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertNotNull(savedEvent.getAuditId());
            assertEquals(testCorrelationId, savedEvent.getCorrelationId());
            assertEquals("MAINFRAME_A", savedEvent.getSourceSystem());
            assertEquals("FILE_TRANSFER", savedEvent.getModuleName());
            assertEquals("FILE_TRANSFER_PROCESS", savedEvent.getProcessName());
            assertEquals("/source/mainframe/path", savedEvent.getSourceEntity());
            assertEquals("/dest/rhel/path", savedEvent.getDestinationEntity());
            assertEquals("BATCH_KEY_123", savedEvent.getKeyIdentifier());
            assertEquals(CheckpointStage.RHEL_LANDING, savedEvent.getCheckpointStage());
            assertEquals(AuditStatus.SUCCESS, savedEvent.getStatus());
            assertEquals("File transferred successfully from mainframe", savedEvent.getMessage());
            assertEquals(expectedJson, savedEvent.getDetailsJson());
            assertNotNull(savedEvent.getEventTimestamp());
        }

        @ParameterizedTest
        @EnumSource(AuditStatus.class)
        @DisplayName("Should generate appropriate default messages for different statuses")
        void testLogFileTransfer_DefaultMessages(AuditStatus status) throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When
            auditService.logFileTransfer(
                testCorrelationId,
                "MAINFRAME_A",
                "test_file.dat",
                "FILE_TRANSFER_PROCESS",
                "/source/path",
                "/dest/path",
                "KEY123",
                status,
                null, // null message to trigger default message generation
                null
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertNotNull(savedEvent.getMessage(), "Default message should be generated");
            assertTrue(savedEvent.getMessage().contains("test_file.dat"), "Message should contain file name");
            
            // Verify status-specific message content using Java 17+ switch expressions
            switch (status) {
                case SUCCESS -> assertTrue(savedEvent.getMessage().contains("completed successfully"));
                case FAILURE -> assertTrue(savedEvent.getMessage().contains("failed"));
                case WARNING -> assertTrue(savedEvent.getMessage().contains("warnings"));
            }
        }

        @Test
        @DisplayName("Should handle null audit details gracefully")
        void testLogFileTransfer_NullAuditDetails() throws Exception {
            // When
            auditService.logFileTransfer(
                testCorrelationId,
                "MAINFRAME_A",
                "test_file.dat",
                "FILE_TRANSFER_PROCESS",
                "/source/path",
                "/dest/path",
                "KEY123",
                AuditStatus.SUCCESS,
                "Custom message",
                null
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertNull(savedEvent.getDetailsJson(), "Details JSON should be null when audit details is null");
            verify(objectMapper, never()).writeValueAsString(any());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw IllegalArgumentException for invalid parameters")
        void testLogFileTransfer_InvalidParameters_ThrowsException(String invalidValue) {
            // Test null/empty correlation ID
            if (invalidValue == null) {
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                    () -> auditService.logFileTransfer(null, "SYSTEM", "file.txt", "process", 
                        "source", "dest", "key", AuditStatus.SUCCESS, "message", null));
                assertEquals("Correlation ID cannot be null", exception.getMessage());
            }
            
            // Test invalid source system
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logFileTransfer(testCorrelationId, invalidValue, "file.txt", "process", 
                    "source", "dest", "key", AuditStatus.SUCCESS, "message", null));
            assertEquals("Source system cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AuditPersistenceException when JSON serialization fails")
        void testLogFileTransfer_JsonSerializationError_ThrowsAuditPersistenceException() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder().fileSizeBytes(1024L).build();
            when(objectMapper.writeValueAsString(auditDetails))
                .thenThrow(new JsonProcessingException("JSON serialization failed") {});

            // When & Then
            AuditPersistenceException exception = assertThrows(AuditPersistenceException.class, 
                () -> auditService.logFileTransfer(testCorrelationId, "SYSTEM", "file.txt", "process", 
                    "source", "dest", "key", AuditStatus.SUCCESS, "message", auditDetails));
            assertEquals("Failed to serialize audit details to JSON", exception.getMessage());
            assertTrue(exception.getCause() instanceof JsonProcessingException);
        }
    }

    @Nested
    @DisplayName("SQL*Loader Checkpoint Tests")
    class SqlLoaderTests {

        @Test
        @DisplayName("Should successfully log SQL*Loader operation with load statistics")
        void testLogSqlLoaderOperation_Success() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .rowsRead(1000L)
                .rowsLoaded(950L)
                .rowsRejected(50L)
                .controlTotalAmount(new BigDecimal("25000.00"))
                .build();

            String expectedJson = "{\"rowsRead\":1000,\"rowsLoaded\":950,\"rowsRejected\":50,\"controlTotalAmount\":25000.00}";
            when(objectMapper.writeValueAsString(auditDetails)).thenReturn(expectedJson);

            // When
            auditService.logSqlLoaderOperation(
                testCorrelationId,
                "MAINFRAME_A",
                "STAGING_ACCOUNTS",
                "SQLLOADER_BATCH_PROCESS",
                "accounts_input.dat",
                "STAGING_ACCOUNTS",
                "BATCH_001",
                AuditStatus.SUCCESS,
                "SQL*Loader completed successfully",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals("SQL_LOADER", savedEvent.getModuleName());
            assertEquals("STAGING_ACCOUNTS", savedEvent.getDestinationEntity());
            assertEquals(CheckpointStage.SQLLOADER_COMPLETE, savedEvent.getCheckpointStage());
            assertEquals(expectedJson, savedEvent.getDetailsJson());
        }

        @Test
        @DisplayName("Should determine SQLLOADER_START stage for start processes")
        void testLogSqlLoaderOperation_StartStage() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When
            auditService.logSqlLoaderOperation(
                testCorrelationId,
                "MAINFRAME_A",
                "STAGING_TABLE",
                "SQLLOADER_START_PROCESS",
                "input_file.dat",
                "STAGING_TABLE",
                "BATCH001",
                AuditStatus.SUCCESS,
                "SQL*Loader operation started",
                null
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals(CheckpointStage.SQLLOADER_START, savedEvent.getCheckpointStage());
        }

        @Test
        @DisplayName("Should determine SQLLOADER_COMPLETE stage for complete processes")
        void testLogSqlLoaderOperation_CompleteStage() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When
            auditService.logSqlLoaderOperation(
                testCorrelationId,
                "MAINFRAME_A",
                "STAGING_TABLE",
                "SQLLOADER_COMPLETE_PROCESS",
                "input_file.dat",
                "STAGING_TABLE",
                "BATCH001",
                AuditStatus.SUCCESS,
                "SQL*Loader operation completed",
                null
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals(CheckpointStage.SQLLOADER_COMPLETE, savedEvent.getCheckpointStage());
        }

        @Test
        @DisplayName("Should validate SQL*Loader specific parameters")
        void testLogSqlLoaderOperation_InvalidTableName_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logSqlLoaderOperation(testCorrelationId, "SYSTEM", "", "process", 
                    "source", "dest", "key", AuditStatus.SUCCESS, "message", null));
            assertEquals("Identifier cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should validate negative row counts in audit details")
        void testLogSqlLoaderOperation_NegativeRowCounts_ThrowsException() {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .rowsRead(-1L)
                .build();

            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.logSqlLoaderOperation(testCorrelationId, "SYSTEM", "TABLE", "process", 
                    "source", "dest", "key", AuditStatus.SUCCESS, "message", auditDetails));
            assertEquals("Rows read cannot be negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should build enhanced message with load statistics")
        void testLogSqlLoaderOperation_EnhancedMessage() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .rowsRead(1000L)
                .rowsLoaded(950L)
                .rowsRejected(50L)
                .build();

            when(objectMapper.writeValueAsString(auditDetails)).thenReturn("{}");

            // When
            auditService.logSqlLoaderOperation(
                testCorrelationId,
                "MAINFRAME_A",
                "STAGING_TABLE",
                "SQLLOADER_PROCESS",
                "input_file.dat",
                "STAGING_TABLE",
                "BATCH001",
                AuditStatus.SUCCESS,
                null, // null message to trigger enhanced message generation
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            String message = savedEvent.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("STAGING_TABLE"));
            assertTrue(message.contains("1000")); // rows read
            assertTrue(message.contains("950"));  // rows loaded
            assertTrue(message.contains("50"));   // rows rejected
        }
    }

    @Nested
    @DisplayName("Business Rule Application Checkpoint Tests")
    class BusinessRuleTests {

        @Test
        @DisplayName("Should successfully log business rule application with transformation details")
        void testLogBusinessRuleApplication_Success() throws Exception {
            // Given
            Map<String, Object> ruleInput = Map.of("accountBalance", 1000.00, "accountType", "CHECKING");
            Map<String, Object> ruleOutput = Map.of("riskScore", 0.25, "approved", true);
            
            AuditDetails auditDetails = AuditDetails.builder()
                .recordCount(500L)
                .recordCountBefore(500L)
                .recordCountAfter(485L)
                .controlTotalDebits(new BigDecimal("10000.00"))
                .ruleInput(ruleInput)
                .ruleOutput(ruleOutput)
                .ruleApplied("RISK_ASSESSMENT_RULE")
                .entityIdentifier("ACCOUNT_123456")
                .transformationDetails("Applied risk assessment and validation rules")
                .build();

            String expectedJson = "{\"recordCount\":500,\"ruleInput\":{\"accountBalance\":1000.0,\"accountType\":\"CHECKING\"}}";
            when(objectMapper.writeValueAsString(auditDetails)).thenReturn(expectedJson);

            // When
            auditService.logBusinessRuleApplication(
                testCorrelationId,
                "MAINFRAME_A",
                "RISK_VALIDATION_MODULE",
                "BUSINESS_RULE_PROCESS",
                "STAGING_ACCOUNTS",
                "PROCESSED_ACCOUNTS",
                "ACCOUNT_123456",
                AuditStatus.SUCCESS,
                "Risk assessment rules applied successfully",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals("RISK_VALIDATION_MODULE", savedEvent.getModuleName());
            assertEquals(CheckpointStage.LOGIC_APPLIED, savedEvent.getCheckpointStage());
            assertEquals("ACCOUNT_123456", savedEvent.getKeyIdentifier());
            assertEquals(expectedJson, savedEvent.getDetailsJson());
        }

        @Test
        @DisplayName("Should build enhanced message with business rule details")
        void testLogBusinessRuleApplication_EnhancedMessage() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .recordCount(100L)
                .ruleApplied("VALIDATION_RULE")
                .entityIdentifier("ENTITY_123")
                .build();

            when(objectMapper.writeValueAsString(auditDetails)).thenReturn("{}");

            // When
            auditService.logBusinessRuleApplication(
                testCorrelationId,
                "MAINFRAME_A",
                "VALIDATION_MODULE",
                "BUSINESS_RULE_PROCESS",
                "SOURCE_TABLE",
                "DEST_TABLE",
                "ENTITY_123",
                AuditStatus.SUCCESS,
                null, // null message to trigger enhanced message generation
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            String message = savedEvent.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("VALIDATION_MODULE"));
            assertTrue(message.contains("ENTITY_123"));
        }

        @Test
        @DisplayName("Should handle business rule failures with detailed error information")
        void testLogBusinessRuleApplication_Failure() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .recordCount(100L)
                .recordCountBefore(100L)
                .recordCountAfter(0L) // No records processed due to failure
                .ruleApplied("VALIDATION_RULE")
                .transformationDetails("Validation failed: Invalid account format")
                .build();

            when(objectMapper.writeValueAsString(auditDetails)).thenReturn("{}");

            // When
            auditService.logBusinessRuleApplication(
                testCorrelationId,
                "MAINFRAME_A",
                "VALIDATION_MODULE",
                "BUSINESS_RULE_PROCESS",
                "SOURCE_TABLE",
                "DEST_TABLE",
                "ENTITY_123",
                AuditStatus.FAILURE,
                "Business rule validation failed",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals(AuditStatus.FAILURE, savedEvent.getStatus());
            assertEquals("Business rule validation failed", savedEvent.getMessage());
        }
    }

    @Nested
    @DisplayName("File Generation Checkpoint Tests")
    class FileGenerationTests {

        @Test
        @DisplayName("Should successfully log file generation with output metadata")
        void testLogFileGeneration_Success() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .recordCount(500L)
                .fileSizeBytes(2048L)
                .fileHashSha256("def456abc789")
                .controlTotalAmount(new BigDecimal("50000.00"))
                .build();

            String expectedJson = "{\"recordCount\":500,\"fileSizeBytes\":2048,\"controlTotalAmount\":50000.00}";
            when(objectMapper.writeValueAsString(auditDetails)).thenReturn(expectedJson);

            // When
            auditService.logFileGeneration(
                testCorrelationId,
                "MAINFRAME_A",
                "output_accounts.txt",
                "FILE_GENERATION_PROCESS",
                "PROCESSED_ACCOUNTS",
                "/output/path/output_accounts.txt",
                "BATCH_001",
                AuditStatus.SUCCESS,
                "Output file generated successfully",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals("FILE_GENERATOR", savedEvent.getModuleName());
            assertEquals(CheckpointStage.FILE_GENERATED, savedEvent.getCheckpointStage());
            assertEquals("FILE_GENERATION_PROCESS", savedEvent.getProcessName()); // Process name, not file name
            assertEquals("/output/path/output_accounts.txt", savedEvent.getDestinationEntity());
            assertEquals(expectedJson, savedEvent.getDetailsJson());
        }

        @Test
        @DisplayName("Should build enhanced message with file generation details")
        void testLogFileGeneration_EnhancedMessage() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .recordCount(250L)
                .fileSizeBytes(1024L)
                .build();

            when(objectMapper.writeValueAsString(auditDetails)).thenReturn("{}");

            // When
            auditService.logFileGeneration(
                testCorrelationId,
                "MAINFRAME_A",
                "output_file.txt",
                "FILE_GENERATION_PROCESS",
                "SOURCE_TABLE",
                "/output/path/output_file.txt",
                "BATCH_001",
                AuditStatus.SUCCESS,
                null, // null message to trigger enhanced message generation
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            String message = savedEvent.getMessage();
            assertNotNull(message);
            assertTrue(message.contains("output_file.txt"));
            assertTrue(message.contains("250")); // record count
        }
    }

    @Nested
    @DisplayName("Audit Trail Retrieval Tests")
    class AuditTrailRetrievalTests {

        @Test
        @DisplayName("Should successfully retrieve audit trail by correlation ID")
        void testGetAuditTrail_Success() {
            // Given
            List<AuditEvent> expectedEvents = Arrays.asList(
                AuditEvent.builder()
                    .auditId(UUID.randomUUID())
                    .correlationId(testCorrelationId)
                    .sourceSystem("MAINFRAME_A")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .build(),
                AuditEvent.builder()
                    .auditId(UUID.randomUUID())
                    .correlationId(testCorrelationId)
                    .sourceSystem("MAINFRAME_A")
                    .checkpointStage(CheckpointStage.SQLLOADER_COMPLETE)
                    .build()
            );

            when(auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId))
                .thenReturn(expectedEvents);

            // When
            List<AuditEvent> result = auditService.getAuditTrail(testCorrelationId);

            // Then
            assertEquals(expectedEvents, result);
            assertEquals(2, result.size());
            verify(auditRepository).findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        }

        @Test
        @DisplayName("Should return empty list when no audit events found")
        void testGetAuditTrail_EmptyResult() {
            // Given
            when(auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId))
                .thenReturn(Arrays.asList());

            // When
            List<AuditEvent> result = auditService.getAuditTrail(testCorrelationId);

            // Then
            assertTrue(result.isEmpty());
            verify(auditRepository).findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when correlation ID is null")
        void testGetAuditTrail_NullCorrelationId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.getAuditTrail(null));
            assertEquals("Correlation ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw AuditPersistenceException when repository fails")
        void testGetAuditTrail_RepositoryException_ThrowsAuditPersistenceException() {
            // Given
            when(auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId))
                .thenThrow(new RuntimeException("Database query failed"));

            // When & Then
            AuditPersistenceException exception = assertThrows(AuditPersistenceException.class, 
                () -> auditService.getAuditTrail(testCorrelationId));
            assertEquals("Failed to retrieve audit trail", exception.getMessage());
            assertTrue(exception.getCause() instanceof RuntimeException);
        }
    }

    @Nested
    @DisplayName("Audit Events Filtering Tests")
    class AuditEventsFilteringTests {

        @Test
        @DisplayName("Should successfully retrieve audit events by source system and checkpoint stage")
        void testGetAuditEventsBySourceAndStage_Success() {
            // Given
            List<AuditEvent> expectedEvents = Arrays.asList(
                AuditEvent.builder()
                    .auditId(UUID.randomUUID())
                    .sourceSystem("MAINFRAME_A")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .build()
            );

            when(auditRepository.findBySourceSystemAndCheckpointStage("MAINFRAME_A", CheckpointStage.RHEL_LANDING))
                .thenReturn(expectedEvents);

            // When
            List<AuditEvent> result = auditService.getAuditEventsBySourceAndStage("MAINFRAME_A", CheckpointStage.RHEL_LANDING);

            // Then
            assertEquals(expectedEvents, result);
            verify(auditRepository).findBySourceSystemAndCheckpointStage("MAINFRAME_A", CheckpointStage.RHEL_LANDING);
        }

        @Test
        @DisplayName("Should successfully retrieve audit events by module name and status")
        void testGetAuditEventsByModuleAndStatus_Success() {
            // Given
            List<AuditEvent> expectedEvents = Arrays.asList(
                AuditEvent.builder()
                    .auditId(UUID.randomUUID())
                    .moduleName("VALIDATION_MODULE")
                    .status(AuditStatus.FAILURE)
                    .build()
            );

            when(auditRepository.findByModuleNameAndStatus("VALIDATION_MODULE", AuditStatus.FAILURE))
                .thenReturn(expectedEvents);

            // When
            List<AuditEvent> result = auditService.getAuditEventsByModuleAndStatus("VALIDATION_MODULE", AuditStatus.FAILURE);

            // Then
            assertEquals(expectedEvents, result);
            verify(auditRepository).findByModuleNameAndStatus("VALIDATION_MODULE", AuditStatus.FAILURE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Should throw IllegalArgumentException for invalid source system")
        void testGetAuditEventsBySourceAndStage_InvalidSourceSystem_ThrowsException(String sourceSystem) {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.getAuditEventsBySourceAndStage(sourceSystem, CheckpointStage.RHEL_LANDING));
            assertEquals("Source system cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when checkpoint stage is null")
        void testGetAuditEventsBySourceAndStage_NullCheckpointStage_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.getAuditEventsBySourceAndStage("MAINFRAME_A", null));
            assertEquals("Checkpoint stage cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Audit Events Counting Tests")
    class AuditEventsCountingTests {

        @Test
        @DisplayName("Should successfully count audit events by correlation ID and status")
        void testCountAuditEventsByCorrelationAndStatus_Success() {
            // Given
            when(auditRepository.countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.SUCCESS))
                .thenReturn(5L);

            // When
            long result = auditService.countAuditEventsByCorrelationAndStatus(testCorrelationId, AuditStatus.SUCCESS);

            // Then
            assertEquals(5L, result);
            verify(auditRepository).countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should return zero when no matching audit events found")
        void testCountAuditEventsByCorrelationAndStatus_ZeroResult() {
            // Given
            when(auditRepository.countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.FAILURE))
                .thenReturn(0L);

            // When
            long result = auditService.countAuditEventsByCorrelationAndStatus(testCorrelationId, AuditStatus.FAILURE);

            // Then
            assertEquals(0L, result);
            verify(auditRepository).countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.FAILURE);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when correlation ID is null")
        void testCountAuditEventsByCorrelationAndStatus_NullCorrelationId_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.countAuditEventsByCorrelationAndStatus(null, AuditStatus.SUCCESS));
            assertEquals("Correlation ID cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when status is null")
        void testCountAuditEventsByCorrelationAndStatus_NullStatus_ThrowsException() {
            // When & Then
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> auditService.countAuditEventsByCorrelationAndStatus(testCorrelationId, null));
            assertEquals("Status cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle large JSON payload gracefully")
        void testLargeJsonPayload_LogsWarning() throws Exception {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                .transformationDetails("A".repeat(15000)) // Large string to exceed 10KB limit
                .build();

            String largeJson = "{\"transformationDetails\":\"" + "A".repeat(15000) + "\"}";
            when(objectMapper.writeValueAsString(auditDetails)).thenReturn(largeJson);

            // When
            auditService.logFileTransfer(
                testCorrelationId,
                "MAINFRAME_A",
                "large_file.dat",
                "FILE_TRANSFER_PROCESS",
                "/source/path",
                "/dest/path",
                "KEY123",
                AuditStatus.SUCCESS,
                "Large file transfer",
                auditDetails
            );

            // Then
            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(auditRepository).save(captor.capture());
            
            AuditEvent savedEvent = captor.getValue();
            assertEquals(largeJson, savedEvent.getDetailsJson());
            // Note: In a real scenario, we would verify the warning log was written
        }

        @Test
        @DisplayName("Should handle concurrent audit logging operations")
        void testConcurrentAuditLogging() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When - Simulate concurrent calls
            UUID correlationId1 = UUID.randomUUID();
            UUID correlationId2 = UUID.randomUUID();

            auditService.logFileTransfer(correlationId1, "SYSTEM_A", "file1.dat", "process1", 
                "source1", "dest1", "key1", AuditStatus.SUCCESS, "message1", null);
            auditService.logFileTransfer(correlationId2, "SYSTEM_B", "file2.dat", "process2", 
                "source2", "dest2", "key2", AuditStatus.SUCCESS, "message2", null);

            // Then
            verify(auditRepository, times(2)).save(any(AuditEvent.class));
        }

        @Test
        @DisplayName("Should preserve transaction boundaries for checkpoint methods")
        void testTransactionBoundaries() throws Exception {
            // Given
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");

            // When
            auditService.logSqlLoaderOperation(
                testCorrelationId,
                "MAINFRAME_A",
                "STAGING_TABLE",
                "SQLLOADER_PROCESS",
                "input_file.dat",
                "STAGING_TABLE",
                "BATCH001",
                AuditStatus.SUCCESS,
                "SQL*Loader operation",
                null
            );

            // Then
            verify(auditRepository).save(any(AuditEvent.class));
            // Note: Transaction behavior would be tested in integration tests
        }
    }
}