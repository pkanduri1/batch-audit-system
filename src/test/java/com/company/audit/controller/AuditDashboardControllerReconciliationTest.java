package com.company.audit.controller;

import com.company.audit.model.dto.ReconciliationReport;
import com.company.audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for reconciliation endpoints in AuditDashboardController.
 * Tests the REST API endpoints for retrieving reconciliation reports and individual reports.
 */
@WebMvcTest(AuditDashboardController.class)
@DisplayName("AuditDashboardController Reconciliation Tests")
class AuditDashboardControllerReconciliationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID testCorrelationId;
    private ReconciliationReport testReport;

    @BeforeEach
    void setUp() {
        testCorrelationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Create test reconciliation report
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
        
        testReport = new ReconciliationReport(
            testCorrelationId, "MAINFRAME_SYSTEM_A", LocalDateTime.now(),
            LocalDateTime.now().minusHours(1), LocalDateTime.now(),
            "SUCCESS", checkpointCounts, controlTotals,
            Arrays.asList(), summary, Arrays.asList()
        );
    }

    @Nested
    @DisplayName("GET /api/audit/reconciliation/{correlationId}")
    class GetReconciliationReportTests {

        @Test
        @DisplayName("Should return reconciliation report for valid correlation ID")
        void testGetReconciliationReport_ValidCorrelationId_ReturnsReport() throws Exception {
            // Given
            when(auditService.generateReconciliationReport(testCorrelationId)).thenReturn(testReport);

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
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
                    .andExpect(jsonPath("$.summary.dataIntegrityValid").value(true));
        }

        @Test
        @DisplayName("Should return 400 for invalid correlation ID")
        void testGetReconciliationReport_InvalidCorrelationId_ReturnsBadRequest() throws Exception {
            // Given
            UUID invalidCorrelationId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            when(auditService.generateReconciliationReport(invalidCorrelationId))
                    .thenThrow(new IllegalArgumentException("No audit events found for correlation ID"));

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", invalidCorrelationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 for service exception")
        void testGetReconciliationReport_ServiceException_ReturnsInternalServerError() throws Exception {
            // Given
            when(auditService.generateReconciliationReport(testCorrelationId))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/{correlationId}", testCorrelationId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/audit/reconciliation/reports")
    class GetReconciliationReportsTests {

        @Test
        @DisplayName("Should return list of reconciliation reports with no filters")
        void testGetReconciliationReports_NoFilters_ReturnsReports() throws Exception {
            // Given
            List<ReconciliationReport> reports = Arrays.asList(testReport);
            when(auditService.getReconciliationReports(any(Map.class))).thenReturn(reports);

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].correlationId").value(testCorrelationId.toString()))
                    .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_SYSTEM_A"))
                    .andExpect(jsonPath("$[0].overallStatus").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return filtered reconciliation reports")
        void testGetReconciliationReports_WithFilters_ReturnsFilteredReports() throws Exception {
            // Given
            List<ReconciliationReport> reports = Arrays.asList(testReport);
            Map<String, String> expectedFilters = new HashMap<>();
            expectedFilters.put("sourceSystem", "MAINFRAME_SYSTEM_A");
            expectedFilters.put("status", "SUCCESS");
            
            when(auditService.getReconciliationReports(expectedFilters)).thenReturn(reports);

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("sourceSystem", "MAINFRAME_SYSTEM_A")
                    .param("status", "SUCCESS")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_SYSTEM_A"))
                    .andExpect(jsonPath("$[0].overallStatus").value("SUCCESS"));
        }

        @Test
        @DisplayName("Should return 400 for invalid status filter")
        void testGetReconciliationReports_InvalidStatusFilter_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("status", "INVALID_STATUS")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid date format")
        void testGetReconciliationReports_InvalidDateFormat_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("startDate", "invalid-date-format")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for start date after end date")
        void testGetReconciliationReports_StartDateAfterEndDate_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("startDate", "2024-01-31T23:59:59")
                    .param("endDate", "2024-01-01T00:00:00")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 for service exception")
        void testGetReconciliationReports_ServiceException_ReturnsInternalServerError() throws Exception {
            // Given
            when(auditService.getReconciliationReports(any(Map.class)))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should return empty list when no reports found")
        void testGetReconciliationReports_NoReportsFound_ReturnsEmptyList() throws Exception {
            // Given
            when(auditService.getReconciliationReports(any(Map.class))).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should handle valid date range filters")
        void testGetReconciliationReports_ValidDateRange_ReturnsReports() throws Exception {
            // Given
            List<ReconciliationReport> reports = Arrays.asList(testReport);
            when(auditService.getReconciliationReports(any(Map.class))).thenReturn(reports);

            // When & Then
            mockMvc.perform(get("/api/audit/reconciliation/reports")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }
}