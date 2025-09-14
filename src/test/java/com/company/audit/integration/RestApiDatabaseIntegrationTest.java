package com.company.audit.integration;

import com.company.audit.BatchAuditApplication;
import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditDetails;
import com.company.audit.model.AuditEvent;
import com.company.audit.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests focusing on REST API endpoints with real Oracle database operations.
 * Tests Spring Boot 3.4+ test framework with JdbcTemplate and Oracle database.
 * 
 * Requirements tested: 6.6, 6.7, 2.5
 */
@SpringBootTest(classes = BatchAuditApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "spring.liquibase.contexts=integration",
    "audit.database.table-prefix=Test_",
    "logging.level.com.company.audit=DEBUG",
    "springdoc.api-docs.enabled=true",
    "springdoc.swagger-ui.enabled=true"
})
class RestApiDatabaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCorrelationId;
    private static final String TEST_SOURCE_SYSTEM = "REST_API_TEST";

    @BeforeEach
    void setUp() {
        testCorrelationId = UUID.randomUUID();
        cleanupTestData();
        createTestAuditData();
    }

    /**
     * Test audit events REST endpoint with database queries and pagination
     * Requirements: 6.6, 6.7
     */
    @Test
    void testAuditEventsEndpointWithDatabaseQueries() throws Exception {
        // Test basic events endpoint
        MvcResult result = mockMvc.perform(get("/api/audit/events")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.totalElements").exists())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("content");
        assertThat(response).contains("pageable");

        // Test filtering by source system
        mockMvc.perform(get("/api/audit/events")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].sourceSystem").value(TEST_SOURCE_SYSTEM));

        // Test filtering by status
        mockMvc.perform(get("/api/audit/events")
                .param("status", "SUCCESS")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        // Test filtering by module name
        mockMvc.perform(get("/api/audit/events")
                .param("moduleName", "TEST_MODULE")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test reconciliation endpoints with real database data
     * Requirements: 6.6, 6.7
     */
    @Test
    void testReconciliationEndpointsWithRealDatabase() throws Exception {
        // Test reconciliation report for specific correlation ID
        mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.correlationId").value(testCorrelationId.toString()))
            .andExpect(jsonPath("$.sourceSystem").value(TEST_SOURCE_SYSTEM))
            .andExpect(jsonPath("$.checkpoints").isArray())
            .andExpect(jsonPath("$.totalRecordsProcessed").exists())
            .andExpect(jsonPath("$.discrepancies").isArray());

        // Test reconciliation reports with filtering
        mockMvc.perform(get("/api/audit/reconciliation/reports")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.pageable").exists());

        // Test reconciliation reports with date range
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        mockMvc.perform(get("/api/audit/reconciliation/reports")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test statistics endpoint with database aggregations
     * Requirements: 6.6, 6.7
     */
    @Test
    void testStatisticsEndpointWithDatabaseAggregations() throws Exception {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        MvcResult result = mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalEvents").exists())
            .andExpect(jsonPath("$.successfulEvents").exists())
            .andExpect(jsonPath("$.failedEvents").exists())
            .andExpect(jsonPath("$.warningEvents").exists())
            .andExpect(jsonPath("$.eventsBySourceSystem").exists())
            .andExpect(jsonPath("$.eventsByCheckpoint").exists())
            .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("totalEvents");
        assertThat(response).contains("eventsBySourceSystem");

        // Test statistics with source system filter
        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventsBySourceSystem").exists());
    }

    /**
     * Test discrepancies endpoint with database queries
     * Requirements: 6.6, 6.7
     */
    @Test
    void testDiscrepanciesEndpointWithDatabaseQueries() throws Exception {
        // Test basic discrepancies endpoint
        mockMvc.perform(get("/api/audit/discrepancies")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());

        // Test discrepancies with source system filter
        mockMvc.perform(get("/api/audit/discrepancies")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        // Test discrepancies with correlation ID filter
        mockMvc.perform(get("/api/audit/discrepancies")
                .param("correlationId", testCorrelationId.toString())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());

        // Test discrepancies with severity filter
        mockMvc.perform(get("/api/audit/discrepancies")
                .param("severity", "HIGH")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    /**
     * Test API error handling with database constraints
     * Requirements: 6.6, 6.7
     */
    @Test
    void testApiErrorHandlingWithDatabaseConstraints() throws Exception {
        // Test invalid correlation ID format
        mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", "invalid-uuid-format")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.error").exists())
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());

        // Test invalid date format
        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", "not-a-date")
                .param("endDate", "also-not-a-date")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());

        // Test invalid pagination parameters
        mockMvc.perform(get("/api/audit/events")
                .param("page", "-1")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // Test non-existent correlation ID
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").exists());
    }

    /**
     * Test database performance with large datasets
     * Requirements: 2.5, 6.6
     */
    @Test
    void testDatabasePerformanceWithLargeDatasets() throws Exception {
        // Create additional test data for performance testing
        createLargeTestDataset();

        long startTime = System.currentTimeMillis();

        // Test pagination performance
        mockMvc.perform(get("/api/audit/events")
                .param("page", "0")
                .param("size", "100")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Verify response time is reasonable (less than 2 seconds)
        assertThat(responseTime).isLessThan(2000);

        // Test filtering performance
        startTime = System.currentTimeMillis();

        mockMvc.perform(get("/api/audit/events")
                .param("sourceSystem", TEST_SOURCE_SYSTEM)
                .param("status", "SUCCESS")
                .param("page", "0")
                .param("size", "50")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        endTime = System.currentTimeMillis();
        responseTime = endTime - startTime;

        // Verify filtered query performance
        assertThat(responseTime).isLessThan(1500);
    }

    private void createTestAuditData() {
        // Create test audit events directly in database
        AuditEvent event1 = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(testCorrelationId)
            .sourceSystem(TEST_SOURCE_SYSTEM)
            .moduleName("TEST_MODULE")
            .processName("FILE_TRANSFER")
            .sourceEntity("test_file.dat")
            .destinationEntity("STAGING_TABLE")
            .keyIdentifier("TEST_KEY_1")
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(LocalDateTime.now())
            .status(AuditStatus.SUCCESS)
            .message("File transfer completed successfully")
            .detailsJson(createTestDetailsJson())
            .build();

        AuditEvent event2 = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(testCorrelationId)
            .sourceSystem(TEST_SOURCE_SYSTEM)
            .moduleName("TEST_MODULE")
            .processName("SQL_LOADER")
            .sourceEntity("STAGING_TABLE")
            .destinationEntity("PROCESSED_TABLE")
            .keyIdentifier("TEST_KEY_2")
            .checkpointStage(CheckpointStage.SQLLOADER_COMPLETE)
            .eventTimestamp(LocalDateTime.now())
            .status(AuditStatus.SUCCESS)
            .message("SQL Loader operation completed")
            .detailsJson(createTestDetailsJson())
            .build();

        auditRepository.save(event1);
        auditRepository.save(event2);
    }

    private void createLargeTestDataset() {
        // Create multiple audit events for performance testing
        for (int i = 0; i < 50; i++) {
            AuditEvent event = AuditEvent.builder()
                .auditId(UUID.randomUUID())
                .correlationId(UUID.randomUUID())
                .sourceSystem(TEST_SOURCE_SYSTEM)
                .moduleName("PERF_TEST_MODULE_" + i)
                .processName("PERFORMANCE_TEST")
                .sourceEntity("perf_test_" + i + ".dat")
                .destinationEntity("PERF_TABLE_" + i)
                .keyIdentifier("PERF_KEY_" + i)
                .checkpointStage(CheckpointStage.values()[i % CheckpointStage.values().length])
                .eventTimestamp(LocalDateTime.now().minusMinutes(i))
                .status(AuditStatus.values()[i % AuditStatus.values().length])
                .message("Performance test event " + i)
                .detailsJson(createTestDetailsJson())
                .build();

            auditRepository.save(event);
        }
    }

    private String createTestDetailsJson() {
        try {
            AuditDetails details = AuditDetails.builder()
                .fileSizeBytes(1024L)
                .fileHashSha256("test_hash_" + System.currentTimeMillis())
                .rowsRead(100L)
                .rowsLoaded(95L)
                .rowsRejected(5L)
                .recordCount(95L)
                .controlTotalDebits(new BigDecimal("1000.00"))
                .ruleInput(Map.of("inputParam", "testValue"))
                .ruleOutput(Map.of("outputParam", "resultValue"))
                .build();

            return objectMapper.writeValueAsString(details);
        } catch (Exception e) {
            return "{}";
        }
    }

    private void cleanupTestData() {
        try {
            jdbcTemplate.execute("DELETE FROM Test_PIPELINE_AUDIT_LOG WHERE SOURCE_SYSTEM = '" + TEST_SOURCE_SYSTEM + "'");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
}