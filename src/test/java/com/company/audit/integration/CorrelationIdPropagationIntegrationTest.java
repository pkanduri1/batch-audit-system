package com.company.audit.integration;

import com.company.audit.BatchAuditApplication;
import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.AuditEvent;
import com.company.audit.repository.AuditRepository;
import com.company.audit.service.AuditService;
import com.company.audit.service.CorrelationIdManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for correlation ID propagation across all components.
 * Tests Java 17+ enhanced debugging features and thread-local correlation ID management.
 * 
 * Requirements tested: 1.2
 */
@SpringBootTest(classes = BatchAuditApplication.class)
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "spring.liquibase.contexts=integration",
    "audit.database.table-prefix=Test_",
    "logging.level.com.company.audit=DEBUG"
})
class CorrelationIdPropagationIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private CorrelationIdManager correlationIdManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TEST_SOURCE_SYSTEM = "CORRELATION_TEST";

    @BeforeEach
    void setUp() {
        cleanupTestData();
    }

    /**
     * Test correlation ID propagation through complete audit flow
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdPropagationThroughCompleteAuditFlow() {
        // Generate and set correlation ID
        UUID testCorrelationId = correlationIdManager.generateCorrelationId();
        correlationIdManager.setCorrelationId(testCorrelationId);

        // Verify correlation ID is set in thread-local storage
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);

        // Execute complete audit flow
        executeCompleteAuditFlow();

        // Verify all audit events have the same correlation ID
        List<AuditEvent> auditEvents = auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        assertThat(auditEvents).hasSize(4);

        auditEvents.forEach(event -> {
            assertThat(event.getCorrelationId()).isEqualTo(testCorrelationId);
            assertThat(event.getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM);
        });

        // Verify checkpoint progression
        assertThat(auditEvents.get(0).getCheckpointStage()).isEqualTo(CheckpointStage.RHEL_LANDING.name());
        assertThat(auditEvents.get(1).getCheckpointStage()).isEqualTo(CheckpointStage.SQLLOADER_COMPLETE.name());
        assertThat(auditEvents.get(2).getCheckpointStage()).isEqualTo(CheckpointStage.LOGIC_APPLIED.name());
        assertThat(auditEvents.get(3).getCheckpointStage()).isEqualTo(CheckpointStage.FILE_GENERATED.name());

        // Verify timestamps are in chronological order
        for (int i = 1; i < auditEvents.size(); i++) {
            assertThat(auditEvents.get(i).getEventTimestamp())
                .isAfterOrEqualTo(auditEvents.get(i-1).getEventTimestamp());
        }
    }

    /**
     * Test correlation ID isolation between different threads
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdIsolationBetweenThreads() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        try {
            // Create three different correlation IDs for three threads
            UUID correlationId1 = UUID.randomUUID();
            UUID correlationId2 = UUID.randomUUID();
            UUID correlationId3 = UUID.randomUUID();

            // Execute audit operations in parallel threads
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                correlationIdManager.setCorrelationId(correlationId1);
                auditService.logFileTransfer(
                    correlationId1,
                    TEST_SOURCE_SYSTEM + "_THREAD1",
                    "file1.dat",
                    "THREAD1_PROCESS",
                    "THREAD1_SOURCE",
                    "THREAD1_DEST",
                    "THREAD1_KEY",
                    AuditStatus.SUCCESS,
                    "Thread 1 file transfer",
                    AuditDetails.builder().fileSizeBytes(1000L).build()
                );
                
                // Verify correlation ID is maintained in this thread
                assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(correlationId1);
            }, executor);

            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                correlationIdManager.setCorrelationId(correlationId2);
                auditService.logSqlLoaderOperation(
                    correlationId2,
                    TEST_SOURCE_SYSTEM + "_THREAD2",
                    "table2",
                    "THREAD2_PROCESS",
                    "THREAD2_SOURCE",
                    "THREAD2_DEST",
                    "THREAD2_KEY",
                    AuditStatus.SUCCESS,
                    "Thread 2 SQL loader operation",
                    AuditDetails.builder().rowsLoaded(500L).build()
                );
                
                // Verify correlation ID is maintained in this thread
                assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(correlationId2);
            }, executor);

            CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
                correlationIdManager.setCorrelationId(correlationId3);
                auditService.logBusinessRuleApplication(
                    correlationId3,
                    TEST_SOURCE_SYSTEM + "_THREAD3",
                    "MODULE3",
                    "THREAD3_PROCESS",
                    "THREAD3_SOURCE",
                    "THREAD3_DEST",
                    "KEY3",
                    AuditStatus.SUCCESS,
                    "Thread 3 business rule application",
                    AuditDetails.builder().recordCount(300L).build()
                );
                
                // Verify correlation ID is maintained in this thread
                assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(correlationId3);
            }, executor);

            // Wait for all threads to complete
            CompletableFuture.allOf(future1, future2, future3).get(10, TimeUnit.SECONDS);

            // Verify each thread created audit events with correct correlation IDs
            List<AuditEvent> events1 = auditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId1);
            assertThat(events1).hasSize(1);
            assertThat(events1.get(0).getCorrelationId()).isEqualTo(correlationId1);
            assertThat(events1.get(0).getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM + "_THREAD1");

            List<AuditEvent> events2 = auditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId2);
            assertThat(events2).hasSize(1);
            assertThat(events2.get(0).getCorrelationId()).isEqualTo(correlationId2);
            assertThat(events2.get(0).getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM + "_THREAD2");

            List<AuditEvent> events3 = auditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId3);
            assertThat(events3).hasSize(1);
            assertThat(events3.get(0).getCorrelationId()).isEqualTo(correlationId3);
            assertThat(events3.get(0).getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM + "_THREAD3");

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /**
     * Test correlation ID persistence across service method calls
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdPersistenceAcrossServiceMethodCalls() {
        UUID testCorrelationId = UUID.randomUUID();
        correlationIdManager.setCorrelationId(testCorrelationId);

        // Call multiple service methods without resetting correlation ID
        auditService.logFileTransfer(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "persistence_test.dat",
            "PERSISTENCE_PROCESS",
            "PERSISTENCE_SOURCE",
            "PERSISTENCE_DEST",
            "PERSISTENCE_KEY_1",
            AuditStatus.SUCCESS,
            "Persistence test file transfer",
            AuditDetails.builder()
                .fileSizeBytes(2048L)
                .fileHashSha256("hash123")
                .build()
        );

        // Verify correlation ID is still set
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);

        auditService.logSqlLoaderOperation(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "PERSISTENCE_TABLE",
            "PERSISTENCE_SQL_PROCESS",
            "PERSISTENCE_FILE",
            "PERSISTENCE_TABLE",
            "PERSISTENCE_KEY_2",
            AuditStatus.SUCCESS,
            "Persistence test SQL loader operation",
            AuditDetails.builder()
                .rowsRead(1000L)
                .rowsLoaded(995L)
                .rowsRejected(5L)
                .build()
        );

        // Verify correlation ID is still set
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);

        auditService.logBusinessRuleApplication(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "PERSISTENCE_MODULE",
            "PERSISTENCE_RULE_PROCESS",
            "PERSISTENCE_TABLE",
            "PROCESSED_TABLE",
            "PERSISTENCE_KEY",
            AuditStatus.SUCCESS,
            "Persistence test business rule application",
            AuditDetails.builder()
                .recordCount(995L)
                .controlTotalDebits(new BigDecimal("10000.00"))
                .ruleInput(Map.of("input", "test"))
                .ruleOutput(Map.of("output", "result"))
                .build()
        );

        // Verify correlation ID is still set
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);

        // Verify all events have the same correlation ID
        List<AuditEvent> events = auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        assertThat(events).hasSize(3);
        events.forEach(event -> 
            assertThat(event.getCorrelationId()).isEqualTo(testCorrelationId)
        );
    }

    /**
     * Test correlation ID cleanup and memory leak prevention
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdCleanupAndMemoryLeakPrevention() {
        UUID testCorrelationId = UUID.randomUUID();
        
        // Set correlation ID
        correlationIdManager.setCorrelationId(testCorrelationId);
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);

        // Clear correlation ID
        correlationIdManager.clearCorrelationId();
        assertThat(correlationIdManager.getCurrentCorrelationId()).isNull();

        // Create audit event without correlation ID set
        auditService.logFileTransfer(
            correlationIdManager.generateCorrelationId(),
            TEST_SOURCE_SYSTEM,
            "cleanup_test.dat",
            "CLEANUP_PROCESS",
            "CLEANUP_SOURCE",
            "CLEANUP_DEST",
            "CLEANUP_KEY",
            AuditStatus.SUCCESS,
            "Cleanup test file transfer",
            AuditDetails.builder().fileSizeBytes(512L).build()
        );

        // Verify that a new correlation ID was generated for the audit event
        String query = """
            SELECT CORRELATION_ID FROM Test_PIPELINE_AUDIT_LOG 
            WHERE SOURCE_SYSTEM = ? AND SOURCE_ENTITY = ?
            """;
        
        List<String> correlationIds = jdbcTemplate.queryForList(
            query, String.class, TEST_SOURCE_SYSTEM, "cleanup_test.dat"
        );
        
        assertThat(correlationIds).hasSize(1);
        UUID generatedCorrelationId = UUID.fromString(correlationIds.get(0));
        assertThat(generatedCorrelationId).isNotEqualTo(testCorrelationId);
        assertThat(generatedCorrelationId).isNotNull();
    }

    /**
     * Test correlation ID generation uniqueness and format
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdGenerationUniquenessAndFormat() {
        // Generate multiple correlation IDs
        UUID id1 = correlationIdManager.generateCorrelationId();
        UUID id2 = correlationIdManager.generateCorrelationId();
        UUID id3 = correlationIdManager.generateCorrelationId();

        // Verify uniqueness
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id2).isNotEqualTo(id3);
        assertThat(id1).isNotEqualTo(id3);

        // Verify format (UUID version 4)
        assertThat(id1.version()).isEqualTo(4);
        assertThat(id2.version()).isEqualTo(4);
        assertThat(id3.version()).isEqualTo(4);

        // Verify string representation format
        String id1String = id1.toString();
        assertThat(id1String).matches("[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}");

        // Test setting and getting correlation IDs
        correlationIdManager.setCorrelationId(id1);
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(id1);

        correlationIdManager.setCorrelationId(id2);
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(id2);
    }

    /**
     * Test correlation ID propagation with database transactions
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdPropagationWithDatabaseTransactions() {
        UUID testCorrelationId = UUID.randomUUID();
        correlationIdManager.setCorrelationId(testCorrelationId);

        // Execute multiple audit operations that should be part of the same logical transaction
        auditService.logFileTransfer(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "transaction_test.dat",
            "TRANSACTION_PROCESS",
            "TRANSACTION_SOURCE",
            "TRANSACTION_DEST",
            "TRANSACTION_KEY_1",
            AuditStatus.SUCCESS,
            "Transaction test file transfer",
            AuditDetails.builder().fileSizeBytes(1024L).build()
        );

        auditService.logSqlLoaderOperation(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "TRANSACTION_TABLE",
            "TRANSACTION_SQL_PROCESS",
            "TRANSACTION_FILE",
            "TRANSACTION_TABLE",
            "TRANSACTION_KEY_2",
            AuditStatus.SUCCESS,
            "Transaction test SQL loader operation",
            AuditDetails.builder().rowsLoaded(100L).build()
        );

        // Verify both events are persisted with the same correlation ID
        List<AuditEvent> events = auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        assertThat(events).hasSize(2);

        // Verify database consistency
        String countQuery = """
            SELECT COUNT(*) FROM Test_PIPELINE_AUDIT_LOG 
            WHERE CORRELATION_ID = ?
            """;
        
        Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class, testCorrelationId.toString());
        assertThat(count).isEqualTo(2);

        // Verify all events have consistent timestamps and correlation IDs
        events.forEach(event -> {
            assertThat(event.getCorrelationId()).isEqualTo(testCorrelationId);
            assertThat(event.getEventTimestamp()).isNotNull();
            assertThat(event.getStatus()).isEqualTo(AuditStatus.SUCCESS);
        });
    }

    /**
     * Test correlation ID propagation with error scenarios
     * Requirements: 1.2
     */
    @Test
    void testCorrelationIdPropagationWithErrorScenarios() {
        UUID testCorrelationId = UUID.randomUUID();
        correlationIdManager.setCorrelationId(testCorrelationId);

        // Test with null parameters (should handle gracefully)
        try {
            auditService.logFileTransfer(null, null, null, null, null, null, null, null, null, null);
        } catch (Exception e) {
            // Expected exception, but correlation ID should still be maintained
            assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(testCorrelationId);
        }

        // Test successful operation after error
        auditService.logFileTransfer(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "error_recovery_test.dat",
            "ERROR_RECOVERY_PROCESS",
            "ERROR_RECOVERY_SOURCE",
            "ERROR_RECOVERY_DEST",
            "ERROR_RECOVERY_KEY",
            AuditStatus.SUCCESS,
            "Error recovery test file transfer",
            AuditDetails.builder().fileSizeBytes(256L).build()
        );

        // Verify correlation ID was maintained and event was created
        List<AuditEvent> events = auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getCorrelationId()).isEqualTo(testCorrelationId);
        assertThat(events.get(0).getSourceEntity()).isEqualTo("error_recovery_test.dat");
    }

    private void executeCompleteAuditFlow() {
        // Checkpoint 1: File Transfer
        auditService.logFileTransfer(
            correlationIdManager.getCurrentCorrelationId(),
            TEST_SOURCE_SYSTEM,
            "complete_flow_test.dat",
            "COMPLETE_FLOW_FILE_PROCESS",
            "MAINFRAME_SOURCE",
            "RHEL_DESTINATION",
            "COMPLETE_FLOW_KEY_1",
            AuditStatus.SUCCESS,
            "Complete flow file transfer",
            AuditDetails.builder()
                .fileSizeBytes(4096L)
                .fileHashSha256("complete_flow_hash")
                .build()
        );

        // Checkpoint 2: SQL Loader Operation
        auditService.logSqlLoaderOperation(
            correlationIdManager.getCurrentCorrelationId(),
            TEST_SOURCE_SYSTEM,
            "COMPLETE_FLOW_TABLE",
            "COMPLETE_FLOW_SQL_PROCESS",
            "complete_flow_test.dat",
            "COMPLETE_FLOW_TABLE",
            "COMPLETE_FLOW_KEY_2",
            AuditStatus.SUCCESS,
            "Complete flow SQL loader operation",
            AuditDetails.builder()
                .rowsRead(2000L)
                .rowsLoaded(1995L)
                .rowsRejected(5L)
                .build()
        );

        // Checkpoint 3: Business Rule Application
        auditService.logBusinessRuleApplication(
            correlationIdManager.getCurrentCorrelationId(),
            TEST_SOURCE_SYSTEM,
            "COMPLETE_FLOW_MODULE",
            "COMPLETE_FLOW_RULE_PROCESS",
            "COMPLETE_FLOW_TABLE",
            "PROCESSED_TABLE",
            "COMPLETE_FLOW_KEY",
            AuditStatus.SUCCESS,
            "Complete flow business rule application",
            AuditDetails.builder()
                .recordCount(1995L)
                .controlTotalDebits(new BigDecimal("25000.00"))
                .ruleInput(Map.of("inputRecords", 1995, "validationRules", "STANDARD"))
                .ruleOutput(Map.of("outputRecords", 1990, "validationErrors", 5))
                .build()
        );

        // Checkpoint 4: File Generation
        auditService.logFileGeneration(
            correlationIdManager.getCurrentCorrelationId(),
            TEST_SOURCE_SYSTEM,
            "complete_flow_output.dat",
            "COMPLETE_FLOW_GEN_PROCESS",
            "PROCESSED_TABLE",
            "OUTPUT_FILE_LOCATION",
            "COMPLETE_FLOW_KEY_4",
            AuditStatus.SUCCESS,
            "Complete flow file generation",
            AuditDetails.builder()
                .fileSizeBytes(8192L)
                .fileHashSha256("complete_flow_output_hash")
                .recordCount(1990L)
                .build()
        );
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.execute("DELETE FROM Test_PIPELINE_AUDIT_LOG WHERE SOURCE_SYSTEM LIKE '" + TEST_SOURCE_SYSTEM + "%'");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}