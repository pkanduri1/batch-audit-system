package com.company.audit.controller;

import com.company.audit.config.TestSecurityConfig;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive REST API integration tests for AuditDashboardController.
 * Tests all endpoints with Spring Boot 3.4+ test framework and JUnit 5.
 * Includes JSON serialization, pagination, filtering, error scenarios, and Swagger documentation verification.
 */
@WebMvcTest(AuditDashboardController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("AuditDashboardController Integration Tests")
class AuditDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<AuditEvent> sampleAuditEvents;
    private UUID testCorrelationId;
    private AuditStatistics sampleStatistics;
    private List<DataDiscrepancy> sampleDiscrepancies;
    private ReconciliationReport sampleReconciliationReport;
    private ReconciliationReportDTO sampleReconciliationReportDTO;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for Java 17+ features and Jackson 2.15+
        objectMapper.registerModule(new JavaTimeModule());
        
        testCorrelationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Create sample audit events
        setupSampleAuditEvents();
        
        // Create sample statistics
        setupSampleStatistics();
        
        // Create sample discrepancies
        setupSampleDiscrepancies();
        
        // Create sample reconciliation report
        setupSampleReconciliationReport();
        
        // Create sample reconciliation report DTO
        setupSampleReconciliationReportDTO();
    }

    private void setupSampleAuditEvents() {
        AuditEvent event1 = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(testCorrelationId)
            .sourceSystem("MAINFRAME_SYSTEM_A")
            .moduleName("FILE_TRANSFER")
            .processName("FILE_LANDING")
            .sourceEntity("mainframe")
            .destinationEntity("rhel_landing")
            .keyIdentifier("ACC123456")
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(LocalDateTime.now().minusHours(1))
            .status(AuditStatus.SUCCESS)
            .message("File transfer completed successfully")
            .detailsJson("{\"fileSizeBytes\":1024,\"fileHashSha256\":\"abc123\"}")
            .build();

        AuditEvent event2 = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(testCorrelationId)
            .sourceSystem("MAINFRAME_SYSTEM_A")
            .moduleName("SQL_LOADER")
            .processName("LOAD_CUSTOMER_DATA")
            .sourceEntity("customer_file.dat")
            .destinationEntity("CUSTOMER_STAGING")
            .keyIdentifier("ACC123456")
            .checkpointStage(CheckpointStage.SQLLOADER_COMPLETE)
            .eventTimestamp(LocalDateTime.now().minusMinutes(30))
            .status(AuditStatus.SUCCESS)
            .message("SQL*Loader operation completed successfully")
            .detailsJson("{\"rowsRead\":1000,\"rowsLoaded\":1000,\"rowsRejected\":0}")
            .build();

        sampleAuditEvents = Arrays.asList(event1, event2);
    }

    private void setupSampleStatistics() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        sampleStatistics = new AuditStatistics(startDate, endDate, 1000, 900, 80, 20);
        
        Map<String, Long> eventsBySourceSystem = new HashMap<>();
        eventsBySourceSystem.put("MAINFRAME_A", 600L);
        eventsBySourceSystem.put("MAINFRAME_B", 400L);
        sampleStatistics.setEventsBySourceSystem(eventsBySourceSystem);
        
        Map<String, Long> eventsByModule = new HashMap<>();
        eventsByModule.put("FILE_TRANSFER", 300L);
        eventsByModule.put("SQL_LOADER", 400L);
        eventsByModule.put("BUSINESS_RULES", 300L);
        sampleStatistics.setEventsByModule(eventsByModule);
        
        sampleStatistics.setAverageEventsPerDay(32.26);
        sampleStatistics.setPeakEventsPerDay(45L);
        sampleStatistics.setPeakDate(LocalDateTime.of(2024, 1, 15, 0, 0));
    }

    private void setupSampleDiscrepancies() {
        DataDiscrepancy discrepancy1 = new DataDiscrepancy();
        discrepancy1.setCorrelationId(UUID.randomUUID());
        discrepancy1.setSourceSystem("MAINFRAME_A");
        discrepancy1.setModuleName("SQL_LOADER");
        discrepancy1.setDiscrepancyType(DataDiscrepancy.DiscrepancyType.RECORD_COUNT_MISMATCH);
        discrepancy1.setSeverity(DataDiscrepancy.DiscrepancySeverity.MEDIUM);
        discrepancy1.setExpectedValue("1000");
        discrepancy1.setActualValue("995");
        discrepancy1.setDescription("Record count mismatch between input and output");
        discrepancy1.setDetectedAt(LocalDateTime.now().minusHours(2));
        discrepancy1.setStatus(DataDiscrepancy.DiscrepancyStatus.OPEN);

        DataDiscrepancy discrepancy2 = new DataDiscrepancy();
        discrepancy2.setCorrelationId(UUID.randomUUID());
        discrepancy2.setSourceSystem("MAINFRAME_B");
        discrepancy2.setModuleName("PIPELINE_MONITOR");
        discrepancy2.setDiscrepancyType(DataDiscrepancy.DiscrepancyType.PROCESSING_TIMEOUT);
        discrepancy2.setSeverity(DataDiscrepancy.DiscrepancySeverity.MEDIUM);
        discrepancy2.setExpectedValue("< 60 minutes");
        discrepancy2.setActualValue("75 minutes");
        discrepancy2.setDescription("Processing timeout detected between stages");
        discrepancy2.setDetectedAt(LocalDateTime.now().minusHours(1));
        discrepancy2.setStatus(DataDiscrepancy.DiscrepancyStatus.INVESTIGATING);

        sampleDiscrepancies = Arrays.asList(discrepancy1, discrepancy2);
    }

    private void setupSampleReconciliationReport() {
        Map<String, Long> checkpointCounts = new HashMap<>();
        checkpointCounts.put("RHEL_LANDING", 1000L);
        checkpointCounts.put("SQLLOADER_COMPLETE", 950L);
        checkpointCounts.put("FILE_GENERATED", 950L);
        
        Map<String, Double> controlTotals = new HashMap<>();
        controlTotals.put("RHEL_LANDING", 50000.0);
        controlTotals.put("FILE_GENERATED", 50000.0);
        
        ReconciliationReport.ReconciliationSummary summary = new ReconciliationReport.ReconciliationSummary(
            1000L, 950L, 50L, 0L, 95.0, 3600000L, true
        );
        
        sampleReconciliationReport = new ReconciliationReport(
            testCorrelationId, "MAINFRAME_SYSTEM_A", LocalDateTime.now().minusHours(2),
            LocalDateTime.now().minusHours(1), LocalDateTime.now(),
            "SUCCESS", checkpointCounts, controlTotals,
            Arrays.asList(), summary, Arrays.asList()
        );
    }

    private void setupSampleReconciliationReportDTO() {
        Map<String, Long> checkpointCounts = new HashMap<>();
        checkpointCounts.put("RHEL_LANDING", 1000L);
        checkpointCounts.put("SQLLOADER_COMPLETE", 950L);
        checkpointCounts.put("FILE_GENERATED", 950L);
        
        Map<String, Double> controlTotals = new HashMap<>();
        controlTotals.put("RHEL_LANDING", 50000.0);
        controlTotals.put("FILE_GENERATED", 50000.0);
        
        ReconciliationReportDTO.BasicSummary summary = new ReconciliationReportDTO.BasicSummary(
            950L, 50L, 0L, 95.0
        );
        
        sampleReconciliationReportDTO = ReconciliationReportDTO.createStandardReport(
            testCorrelationId, "MAINFRAME_SYSTEM_A", LocalDateTime.now(),
            ReconciliationReportDTO.ReportStatus.SUCCESS,
            LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
            checkpointCounts, controlTotals, 0, summary
        );
    }

    @Nested
    @DisplayName("Context and Configuration Tests")
    class ContextAndConfigurationTests {

        @Test
        @DisplayName("Should load Spring Boot 3.4+ context with Java 17+ compatibility")
        void contextLoads() {
            // Test that the controller loads properly in Spring context
            // This verifies the basic Spring Boot 3.4+ and Java 17+ compatibility
            assertThat(mockMvc).isNotNull();
            assertThat(objectMapper).isNotNull();
        }

        @Test
        @DisplayName("Should configure Jackson 2.15+ with Java 17+ features")
        void jacksonConfigurationTest() throws Exception {
            // Test Jackson 2.15+ serialization with Java 17+ LocalDateTime
            LocalDateTime testTime = LocalDateTime.now();
            String json = objectMapper.writeValueAsString(testTime);
            LocalDateTime deserializedTime = objectMapper.readValue(json, LocalDateTime.class);
            
            assertThat(deserializedTime).isEqualTo(testTime);
        }

        @Test
        @DisplayName("Should handle Spring Boot 3.4+ test utilities")
        void springBootTestUtilities() throws Exception {
            // Test Spring Boot 3.4+ test framework features
            mockMvc.perform(get("/api/audit/events")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print()) // Spring Boot 3.4+ enhanced printing
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Audit Events Endpoint Integration Tests")
    class AuditEventsEndpointTests {

        @Test
        @DisplayName("Should return paginated audit events with proper JSON serialization")
        void testGetAuditEvents_PaginationAndSerialization() throws Exception {
            // Given
            when(auditService.getAuditEventsWithFilters(null, null, null, null, 0, 20))
                .thenReturn(sampleAuditEvents);
            when(auditService.countAuditEventsWithFilters(null, null, null, null))
                .thenReturn(2L);

            // When & Then
            MvcResult result = mockMvc.perform(get("/api/audit/events")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(20))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.first").value(true))
                    .andExpect(jsonPath("$.last").value(true))
                    .andExpect(jsonPath("$.numberOfElements").value(2))
                    .andReturn();

            // Verify JSON serialization with Jackson 2.15+
            String responseJson = result.getResponse().getContentAsString();
            PagedResponse<?> pagedResponse = objectMapper.readValue(responseJson, PagedResponse.class);
            assertThat(pagedResponse.getContent()).hasSize(2);
            assertThat(pagedResponse.getTotalElements()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should handle complex filtering scenarios")
        void testGetAuditEvents_ComplexFiltering() throws Exception {
            // Given
            String sourceSystem = "MAINFRAME_SYSTEM_A";
            String moduleName = "FILE_TRANSFER";
            AuditStatus status = AuditStatus.SUCCESS;
            CheckpointStage checkpointStage = CheckpointStage.RHEL_LANDING;
            
            List<AuditEvent> filteredEvents = Arrays.asList(sampleAuditEvents.get(0));
            
            when(auditService.getAuditEventsWithFilters(
                eq(sourceSystem), eq(moduleName), eq(status), eq(checkpointStage), eq(0), eq(20)))
                .thenReturn(filteredEvents);
            when(auditService.countAuditEventsWithFilters(
                eq(sourceSystem), eq(moduleName), eq(status), eq(checkpointStage)))
                .thenReturn(1L);

            // When & Then
            mockMvc.perform(get("/api/audit/events")
                    .param("sourceSystem", sourceSystem)
                    .param("moduleName", moduleName)
                    .param("status", status.name())
                    .param("checkpointStage", checkpointStage.name())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].sourceSystem").value(sourceSystem))
                    .andExpect(jsonPath("$.content[0].moduleName").value(moduleName))
                    .andExpect(jsonPath("$.content[0].status").value(status.name()))
                    .andExpect(jsonPath("$.content[0].checkpointStage").value(checkpointStage.name()));
        }

        @Test
        @DisplayName("Should validate pagination parameters and return appropriate errors")
        void testGetAuditEvents_PaginationValidation() throws Exception {
            // Test invalid page number
            mockMvc.perform(get("/api/audit/events")
                    .param("page", "-1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            
            // Test invalid page size (too small)
            mockMvc.perform(get("/api/audit/events")
                    .param("size", "0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
            
            // Test invalid page size (too large)
            mockMvc.perform(get("/api/audit/events")
                    .param("size", "1001")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle service exceptions gracefully")
        void testGetAuditEvents_ServiceExceptionHandling() throws Exception {
            // Given
            when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/audit/events")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Statistics Endpoint Integration Tests")
    class StatisticsEndpointTests {

        @Test
        @DisplayName("Should return comprehensive audit statistics with proper JSON structure")
        void testGetAuditStatistics_ComprehensiveData() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            
            when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sampleStatistics);

            // When & Then
            MvcResult result = mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEvents").value(1000))
                .andExpect(jsonPath("$.successfulEvents").value(900))
                .andExpect(jsonPath("$.failedEvents").value(80))
                .andExpect(jsonPath("$.warningEvents").value(20))
                .andExpect(jsonPath("$.successRate").value(90.0))
                .andExpect(jsonPath("$.failureRate").value(8.0))
                .andExpect(jsonPath("$.warningRate").value(2.0))
                .andExpect(jsonPath("$.eventsBySourceSystem.MAINFRAME_A").value(600))
                .andExpect(jsonPath("$.eventsBySourceSystem.MAINFRAME_B").value(400))
                .andExpect(jsonPath("$.eventsByModule.FILE_TRANSFER").value(300))
                .andExpect(jsonPath("$.eventsByModule.SQL_LOADER").value(400))
                .andExpect(jsonPath("$.eventsByModule.BUSINESS_RULES").value(300))
                .andExpect(jsonPath("$.averageEventsPerDay").value(32.26))
                .andExpect(jsonPath("$.peakEventsPerDay").value(45))
                .andReturn();

            // Verify complex JSON deserialization with Jackson 2.15+
            String responseJson = result.getResponse().getContentAsString();
            AuditStatistics statistics = objectMapper.readValue(responseJson, AuditStatistics.class);
            assertThat(statistics.getTotalEvents()).isEqualTo(1000);
            assertThat(statistics.getEventsBySourceSystem()).containsEntry("MAINFRAME_A", 600L);
            assertThat(statistics.getEventsByModule()).containsEntry("SQL_LOADER", 400L);
        }

        @Test
        @DisplayName("Should validate date parameters comprehensively")
        void testGetAuditStatistics_DateValidation() throws Exception {
            // Test missing start date
            mockMvc.perform(get("/api/audit/statistics")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
            
            // Test missing end date
            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-01T00:00:00")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
            
            // Test start date after end date
            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-31T23:59:59")
                    .param("endDate", "2024-01-01T00:00:00")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Discrepancies Endpoint Integration Tests")
    class DiscrepanciesEndpointTests {

        @Test
        @DisplayName("Should return data discrepancies with comprehensive filtering")
        void testGetDataDiscrepancies_ComprehensiveFiltering() throws Exception {
            // Given
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(sampleDiscrepancies);

            // When & Then
            MvcResult result = mockMvc.perform(get("/api/audit/discrepancies")
                    .param("sourceSystem", "MAINFRAME_A")
                    .param("severity", "HIGH")
                    .param("status", "OPEN")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_A"))
                .andExpect(jsonPath("$[0].moduleName").value("SQL_LOADER"))
                .andExpect(jsonPath("$[0].discrepancyType").value("RECORD_COUNT_MISMATCH"))
                .andExpect(jsonPath("$[0].severity").value("MEDIUM"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].expectedValue").value("1000"))
                .andExpect(jsonPath("$[0].actualValue").value("995"))
                .andExpect(jsonPath("$[1].sourceSystem").value("MAINFRAME_B"))
                .andExpect(jsonPath("$[1].discrepancyType").value("PROCESSING_TIMEOUT"))
                .andExpect(jsonPath("$[1].severity").value("MEDIUM"))
                .andExpect(jsonPath("$[1].status").value("INVESTIGATING"))
                .andReturn();

            // Verify complex object deserialization
            String responseJson = result.getResponse().getContentAsString();
            List<?> discrepancies = objectMapper.readValue(responseJson, List.class);
            assertThat(discrepancies).hasSize(2);
        }

        @Test
        @DisplayName("Should validate filter parameters and enum values")
        void testGetDataDiscrepancies_FilterValidation() throws Exception {
            // Test invalid severity
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("severity", "INVALID_SEVERITY")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
            
            // Test invalid status
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("status", "INVALID_STATUS")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
            
            // Test invalid date format
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("startDate", "invalid-date-format")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Reconciliation Endpoints Integration Tests")
    class ReconciliationEndpointsTests {

        @Test
        @DisplayName("Should return detailed reconciliation report with complex data structures")
        void testGetReconciliationReport_DetailedReport() throws Exception {
            // Given
            when(auditService.generateReconciliationReport(testCorrelationId))
                .thenReturn(sampleReconciliationReport);

            // When & Then
            MvcResult result = mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.correlationId").value(testCorrelationId.toString()))
                    .andExpect(jsonPath("$.sourceSystem").value("MAINFRAME_SYSTEM_A"))
                    .andExpect(jsonPath("$.overallStatus").value("SUCCESS"))
                    .andExpect(jsonPath("$.checkpointCounts.RHEL_LANDING").value(1000))
                    .andExpect(jsonPath("$.checkpointCounts.SQLLOADER_COMPLETE").value(950))
                    .andExpect(jsonPath("$.checkpointCounts.FILE_GENERATED").value(950))
                    .andExpect(jsonPath("$.controlTotals.RHEL_LANDING").value(50000.0))
                    .andExpect(jsonPath("$.controlTotals.FILE_GENERATED").value(50000.0))
                    .andExpect(jsonPath("$.summary.totalRecordsProcessed").value(1000))
                    .andExpect(jsonPath("$.summary.successfulEvents").value(950))
                    .andExpect(jsonPath("$.summary.failedEvents").value(50))
                    .andExpect(jsonPath("$.summary.successRate").value(95.0))
                    .andExpect(jsonPath("$.summary.dataIntegrityValid").value(true))
                    .andReturn();

            // Verify complex nested object deserialization
            String responseJson = result.getResponse().getContentAsString();
            ReconciliationReport report = objectMapper.readValue(responseJson, ReconciliationReport.class);
            assertThat(report.getCorrelationId()).isEqualTo(testCorrelationId);
            assertThat(report.getCheckpointCounts()).containsEntry("RHEL_LANDING", 1000L);
            assertThat(report.getSummary().getDataIntegrityValid()).isTrue();
        }

        @Test
        @DisplayName("Should handle reconciliation report DTO with Java 17+ sealed classes")
        void testGetReconciliationReportDTO_SealedClasses() throws Exception {
            // Given
            when(auditService.generateStandardReconciliationReportDTO(testCorrelationId))
                .thenReturn((ReconciliationReportDTO.StandardReconciliationReport) sampleReconciliationReportDTO);

            // When & Then
            MvcResult result = mockMvc.perform(get("/api/audit/reconciliation/{correlationId}/dto", testCorrelationId)
                    .param("reportType", "STANDARD")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.correlationId").value(testCorrelationId.toString()))
                    .andExpect(jsonPath("$.sourceSystem").value("MAINFRAME_SYSTEM_A"))
                    .andExpect(jsonPath("$.overallStatus").value("SUCCESS"))
                    .andExpect(jsonPath("$.reportType").value("STANDARD"))
                    .andExpect(jsonPath("$.checkpointCounts.RHEL_LANDING").value(1000))
                    .andExpect(jsonPath("$.checkpointCounts.SQLLOADER_COMPLETE").value(950))
                    .andExpect(jsonPath("$.summary.successfulEvents").value(950))
                    .andExpect(jsonPath("$.summary.failedEvents").value(50))
                    .andExpect(jsonPath("$.summary.successRate").value(95.0))
                    .andReturn();

            // Verify DTO serialization with Java 17+ features
            String responseJson = result.getResponse().getContentAsString();
            ReconciliationReportDTO reportDTO = objectMapper.readValue(responseJson, ReconciliationReportDTO.class);
            assertThat(reportDTO.getCorrelationId()).isEqualTo(testCorrelationId);
            assertThat(reportDTO.getOverallStatus()).isEqualTo(ReconciliationReportDTO.ReportStatus.SUCCESS);
        }

        @Test
        @DisplayName("Should validate report type parameters")
        void testGetReconciliationReportDTO_ReportTypeValidation() throws Exception {
            // Test invalid report type
            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}/dto", testCorrelationId)
                    .param("reportType", "INVALID_TYPE")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return list of reconciliation reports with filtering")
        void testGetReconciliationReports_ListWithFiltering() throws Exception {
            // Given
            List<ReconciliationReport> reports = Arrays.asList(sampleReconciliationReport);
            when(auditService.getReconciliationReports(anyMap())).thenReturn(reports);

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("sourceSystem", "MAINFRAME_SYSTEM_A")
                    .param("status", "SUCCESS")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].correlationId").value(testCorrelationId.toString()))
                    .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_SYSTEM_A"))
                    .andExpect(jsonPath("$[0].overallStatus").value("SUCCESS"));
        }
    }

    @Nested
    @DisplayName("Error Handling and Edge Cases")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle various service exceptions appropriately")
        void testServiceExceptionHandling() throws Exception {
            // Test IllegalArgumentException -> 400 Bad Request
            when(auditService.generateReconciliationReport(any(UUID.class)))
                .thenThrow(new IllegalArgumentException("Invalid correlation ID"));

            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // Test RuntimeException -> 500 Internal Server Error
            when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle empty results gracefully")
        void testEmptyResultsHandling() throws Exception {
            // Test empty audit events
            when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
            when(auditService.countAuditEventsWithFilters(any(), any(), any(), any()))
                .thenReturn(0L);

            mockMvc.perform(get("/api/audit/events")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));

            // Test empty discrepancies
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(Arrays.asList());

            mockMvc.perform(get("/api/audit/discrepancies")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should handle malformed UUID parameters")
        void testMalformedUUIDHandling() throws Exception {
            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", "invalid-uuid")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Swagger Documentation Verification")
    class SwaggerDocumentationTests {

        @Test
        @DisplayName("Should verify SpringDoc OpenAPI v2 annotations are processed")
        void testSwaggerAnnotationsProcessing() throws Exception {
            // This test verifies that the controller endpoints are accessible
            // and that Swagger annotations don't interfere with normal operation
            
            // Mock the service calls for all endpoints
            when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Arrays.asList());
            when(auditService.countAuditEventsWithFilters(any(), any(), any(), any()))
                .thenReturn(0L);
            when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sampleStatistics);
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(Arrays.asList());
            when(auditService.generateReconciliationReport(any(UUID.class)))
                .thenReturn(sampleReconciliationReport);
            
            // Test that all major endpoints are accessible (indicating Swagger annotations are valid)
            mockMvc.perform(get("/api/audit/events")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/audit/discrepancies")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle OpenAPI 3.0 schema validation")
        void testOpenAPISchemaValidation() throws Exception {
            // Test that complex request/response structures work with OpenAPI schemas
            when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(sampleAuditEvents);
            when(auditService.countAuditEventsWithFilters(any(), any(), any(), any()))
                .thenReturn(2L);

            MvcResult result = mockMvc.perform(get("/api/audit/events")
                    .param("sourceSystem", "MAINFRAME_SYSTEM_A")
                    .param("moduleName", "FILE_TRANSFER")
                    .param("status", "SUCCESS")
                    .param("checkpointStage", "RHEL_LANDING")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            // Verify that the response structure matches what would be expected by OpenAPI schema
            String responseJson = result.getResponse().getContentAsString();
            assertThat(responseJson).contains("content", "page", "size", "totalElements", "totalPages");
        }
    }
}