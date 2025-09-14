package com.company.audit.integration;

import com.company.audit.BatchAuditApplication;
import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.AuditEvent;
import com.company.audit.model.dto.AuditEventDTO;
import com.company.audit.model.dto.AuditStatistics;
import com.company.audit.model.dto.ReconciliationReportDTO;
import com.company.audit.repository.AuditRepository;
import com.company.audit.service.AuditService;
import com.company.audit.service.CorrelationIdManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive end-to-end integration tests for the Batch Audit System.
 * Tests complete audit flow from service to Oracle database using JdbcTemplate
 * with Spring Boot 3.4+ and Java 17+ features.
 * 
 * Requirements tested: 1.1, 1.2, 2.5, 6.6, 6.7
 */
@SpringBootTest(classes = BatchAuditApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "spring.liquibase.contexts=integration",
    "audit.database.table-prefix=Test_",
    "logging.level.com.company.audit=DEBUG"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndAuditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private CorrelationIdManager correlationIdManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCorrelationId;
    private static final String TEST_SOURCE_SYSTEM = "MAINFRAME_TEST";
    private static final String TEST_MODULE_NAME = "DATA_PROCESSOR_TEST";

    @BeforeEach
    void setUp() {
        // Generate unique correlation ID for each test
        testCorrelationId = correlationIdManager.generateCorrelationId();
        correlationIdManager.setCorrelationId(testCorrelationId);
        
        // Clean up any existing test data
        cleanupTestData();
    }

    /**
     * Test complete audit flow from file transfer through all checkpoints
     * Requirements: 1.1, 1.2, 2.5
     */
    @Test
    @Order(1)
    void testCompleteAuditFlowFromServiceToDatabase() {
        // Test Checkpoint 1: File Transfer
        AuditDetails fileTransferDetails = AuditDetails.builder()
            .fileSizeBytes(1024L)
            .fileHashSha256("abc123def456")
            .build();

        auditService.logFileTransfer(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "test_file.dat",
            "FILE_TRANSFER_PROCESS",
            "MAINFRAME_SOURCE",
            "RHEL_DESTINATION",
            "FILE_KEY_1",
            AuditStatus.SUCCESS,
            "File transfer completed successfully",
            fileTransferDetails
        );

        // Test Checkpoint 2: SQL Loader Operation
        AuditDetails sqlLoaderDetails = AuditDetails.builder()
            .rowsRead(1000L)
            .rowsLoaded(995L)
            .rowsRejected(5L)
            .build();

        auditService.logSqlLoaderOperation(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "STAGING_TABLE",
            "SQL_LOADER_PROCESS",
            "test_file.dat",
            "STAGING_TABLE",
            "SQL_KEY_1",
            AuditStatus.SUCCESS,
            "SQL Loader operation completed successfully",
            sqlLoaderDetails
        );

        // Test Checkpoint 3: Business Rule Application
        AuditDetails businessRuleDetails = AuditDetails.builder()
            .recordCount(995L)
            .controlTotalDebits(new BigDecimal("50000.00"))
            .ruleInput(Map.of("inputRecords", 995))
            .ruleOutput(Map.of("outputRecords", 990, "validationErrors", 5))
            .build();

        auditService.logBusinessRuleApplication(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            TEST_MODULE_NAME,
            "BUSINESS_RULE_PROCESS",
            "STAGING_TABLE",
            "PROCESSED_TABLE",
            "ACCT_12345",
            AuditStatus.SUCCESS,
            "Business rule application completed successfully",
            businessRuleDetails
        );

        // Test Checkpoint 4: File Generation
        AuditDetails fileGenDetails = AuditDetails.builder()
            .fileSizeBytes(2048L)
            .fileHashSha256("def456ghi789")
            .recordCount(990L)
            .build();

        auditService.logFileGeneration(
            testCorrelationId,
            TEST_SOURCE_SYSTEM,
            "output_file.dat",
            "FILE_GENERATION_PROCESS",
            "PROCESSED_TABLE",
            "OUTPUT_FILE_LOCATION",
            "OUTPUT_KEY_1",
            AuditStatus.SUCCESS,
            "File generation completed successfully",
            fileGenDetails
        );

        // Verify all audit events were persisted to database
        List<AuditEvent> auditTrail = auditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
        
        assertThat(auditTrail).hasSize(4);
        assertThat(auditTrail.get(0).getCheckpointStage()).isEqualTo(CheckpointStage.RHEL_LANDING.name());
        assertThat(auditTrail.get(1).getCheckpointStage()).isEqualTo(CheckpointStage.SQLLOADER_COMPLETE.name());
        assertThat(auditTrail.get(2).getCheckpointStage()).isEqualTo(CheckpointStage.LOGIC_APPLIED.name());
        assertThat(auditTrail.get(3).getCheckpointStage()).isEqualTo(CheckpointStage.FILE_GENERATED.name());

        // Verify correlation ID propagation
        auditTrail.forEach(event -> 
            assertThat(event.getCorrelationId()).isEqualTo(testCorrelationId)
        );

        // Verify source system consistency
        auditTrail.forEach(event -> 
            assertThat(event.getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM)
        );
    }

    /**
     * Test REST API endpoints with real Oracle database
     * Requirements: 6.6, 6.7
     */
    @Test
    @Order(2)
    void testRestApiEndpointsWithRealDatabase() throws Exception {
        // First create some test audit data
        createTestAuditData();

        // Test GET /api/audit/events endpoint
        MvcResult eventsResult = mockMvc.perform(get("/api/audit/events")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].sourceSystem").value(TEST_SOURCE_SYSTEM))
            .andReturn();

        String eventsResponse = eventsResult.getResponse().getContentAsString();
        assertThat(eventsResponse).contains(TEST_SOURCE_SYSTEM);

        // Test GET /api/audit/reconciliation/{correlationId} endpoint
        mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.correlationId").value(testCorrelationId.toString()))
            .andExpect(jsonPath("$.sourceSystem").value(TEST_SOURCE_SYSTEM));

        // Test GET /api/audit/statistics endpoint
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalEvents").exists())
            .andExpect(jsonPath("$.successfulEvents").exists())
            .andExpect(jsonPath("$.failedEvents").exists());

        // Test GET /api/audit/discrepancies endpoint
        mockMvc.perform(get("/api/audit/discrepancies")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }

    /**
     * Test Liquibase migrations in integration environment
     * Requirements: 2.5
     */
    @Test
    @Order(3)
    void testLiquibaseMigrationsInIntegrationEnvironment() {
        // Verify that Liquibase has created the required tables with Test_ prefix
        String tableExistsQuery = """
            SELECT COUNT(*) FROM user_tables 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            """;
        
        Integer tableCount = jdbcTemplate.queryForObject(tableExistsQuery, Integer.class);
        assertThat(tableCount).isEqualTo(1);

        // Verify table structure
        String columnQuery = """
            SELECT column_name, data_type 
            FROM user_tab_columns 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            ORDER BY column_name
            """;
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(columnQuery);
        assertThat(columns).isNotEmpty();
        
        // Verify key columns exist
        List<String> columnNames = columns.stream()
            .map(col -> (String) col.get("COLUMN_NAME"))
            .toList();
        
        assertThat(columnNames).contains(
            "AUDIT_ID", "CORRELATION_ID", "SOURCE_SYSTEM", 
            "MODULE_NAME", "CHECKPOINT_STAGE", "EVENT_TIMESTAMP", 
            "STATUS", "DETAILS_JSON"
        );

        // Verify indexes exist
        String indexQuery = """
            SELECT index_name FROM user_indexes 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            """;
        
        List<String> indexes = jdbcTemplate.queryForList(indexQuery, String.class);
        assertThat(indexes).isNotEmpty();
    }

    /**
     * Test correlation ID propagation across all components
     * Requirements: 1.2
     */
    @Test
    @Order(4)
    void testCorrelationIdPropagationAcrossComponents() {
        UUID specificCorrelationId = UUID.randomUUID();
        correlationIdManager.setCorrelationId(specificCorrelationId);

        // Create audit events through different service methods
        auditService.logFileTransfer(specificCorrelationId, TEST_SOURCE_SYSTEM, "test1.dat", 
            "CORRELATION_TEST_PROCESS", "CORRELATION_SOURCE", "CORRELATION_DEST", "CORRELATION_KEY_1",
            AuditStatus.SUCCESS, "Correlation test file transfer", AuditDetails.builder().fileSizeBytes(100L).build());
        
        auditService.logSqlLoaderOperation(specificCorrelationId, TEST_SOURCE_SYSTEM, "TABLE1", 
            "CORRELATION_SQL_PROCESS", "test1.dat", "TABLE1", "CORRELATION_KEY_2",
            AuditStatus.SUCCESS, "Correlation test SQL loader", AuditDetails.builder().rowsLoaded(50L).build());
        
        auditService.logBusinessRuleApplication(specificCorrelationId, TEST_SOURCE_SYSTEM, TEST_MODULE_NAME, "CORRELATION_RULE_PROCESS",
            "TABLE1", "PROCESSED_TABLE", "KEY1", AuditStatus.SUCCESS, "Correlation test business rule",
            AuditDetails.builder().recordCount(50L).build());

        // Verify all events have the same correlation ID
        List<AuditEvent> events = auditRepository.findByCorrelationIdOrderByEventTimestamp(specificCorrelationId);
        assertThat(events).hasSize(3);
        
        events.forEach(event -> {
            assertThat(event.getCorrelationId()).isEqualTo(specificCorrelationId);
            assertThat(event.getSourceSystem()).isEqualTo(TEST_SOURCE_SYSTEM);
        });

        // Verify correlation ID is maintained in thread-local storage
        assertThat(correlationIdManager.getCurrentCorrelationId()).isEqualTo(specificCorrelationId);
    }

    /**
     * Test SpringDoc OpenAPI v2 Swagger UI functionality and API documentation generation
     * Requirements: 6.7
     */
    @Test
    @Order(5)
    void testSwaggerUiFunctionalityAndApiDocumentation() throws Exception {
        // Test Swagger UI accessibility
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk());

        // Test OpenAPI JSON documentation endpoint
        MvcResult openApiResult = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        String openApiJson = openApiResult.getResponse().getContentAsString();
        
        // Verify API documentation contains expected endpoints
        assertThat(openApiJson).contains("/api/audit/events");
        assertThat(openApiJson).contains("/api/audit/reconciliation");
        assertThat(openApiJson).contains("/api/audit/statistics");
        assertThat(openApiJson).contains("/api/audit/discrepancies");
        
        // Verify API documentation contains expected schemas
        assertThat(openApiJson).contains("AuditEventDTO");
        assertThat(openApiJson).contains("ReconciliationReportDTO");
        assertThat(openApiJson).contains("AuditStatistics");
        
        // Test specific API group documentation
        mockMvc.perform(get("/v3/api-docs/audit-system"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    /**
     * Test error handling and resilience in end-to-end scenarios
     * Requirements: 1.1, 2.5
     */
    @Test
    @Order(6)
    void testErrorHandlingAndResilienceInEndToEndScenarios() throws Exception {
        // Test API error handling with invalid correlation ID
        mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists());

        // Test API error handling with invalid date range
        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", "invalid-date")
                .param("endDate", "also-invalid")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // Test service layer error handling
        try {
            auditService.logFileTransfer(null, null, null, null, null, null, null, null, null, null);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(IllegalArgumentException.class);
        }
    }

    private void createTestAuditData() {
        AuditDetails testDetails = AuditDetails.builder()
            .fileSizeBytes(512L)
            .recordCount(100L)
            .build();

        auditService.logFileTransfer(testCorrelationId, TEST_SOURCE_SYSTEM, "integration_test.dat", 
            "TEST_PROCESS", "TEST_SOURCE", "TEST_DEST", "TEST_KEY", AuditStatus.SUCCESS, "Test message", testDetails);
        auditService.logSqlLoaderOperation(testCorrelationId, TEST_SOURCE_SYSTEM, "TEST_TABLE", 
            "TEST_PROCESS", "TEST_SOURCE", "TEST_DEST", "TEST_KEY", AuditStatus.SUCCESS, "Test message", testDetails);
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.execute("DELETE FROM Test_PIPELINE_AUDIT_LOG WHERE SOURCE_SYSTEM = '" + TEST_SOURCE_SYSTEM + "'");
        } catch (Exception e) {
            // Ignore cleanup errors in case table doesn't exist yet
        }
    }
}