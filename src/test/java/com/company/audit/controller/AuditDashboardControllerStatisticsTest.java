package com.company.audit.controller;

import com.company.audit.model.dto.AuditStatistics;
import com.company.audit.model.dto.DataDiscrepancy;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for AuditDashboardController statistics and discrepancy endpoints.
 * Tests the REST API endpoints for audit statistics and data discrepancy retrieval.
 */
@WebMvcTest(AuditDashboardController.class)
@DisplayName("AuditDashboardController Statistics and Discrepancies Tests")
class AuditDashboardControllerStatisticsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditStatistics sampleStatistics;
    private List<DataDiscrepancy> sampleDiscrepancies;

    @BeforeEach
    void setUp() {
        // Set up sample statistics
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        
        sampleStatistics = new AuditStatistics(startDate, endDate, 1000, 900, 80, 20);
        
        Map<String, Long> eventsBySourceSystem = new HashMap<>();
        eventsBySourceSystem.put("MAINFRAME_A", 600L);
        eventsBySourceSystem.put("MAINFRAME_B", 400L);
        sampleStatistics.setEventsBySourceSystem(eventsBySourceSystem);
        
        sampleStatistics.setAverageEventsPerDay(32.26);
        sampleStatistics.setPeakEventsPerDay(45L);
        sampleStatistics.setPeakDate(LocalDateTime.of(2024, 1, 15, 0, 0));

        // Set up sample discrepancies
        DataDiscrepancy discrepancy1 = new DataDiscrepancy();
        discrepancy1.setCorrelationId(java.util.UUID.randomUUID());
        discrepancy1.setSourceSystem("MAINFRAME_A");
        discrepancy1.setModuleName("SQL_LOADER");
        discrepancy1.setDiscrepancyType(DataDiscrepancy.DiscrepancyType.RECORD_COUNT_MISMATCH);
        discrepancy1.setSeverity(DataDiscrepancy.DiscrepancySeverity.MEDIUM);
        discrepancy1.setExpectedValue("1000");
        discrepancy1.setActualValue("995");
        discrepancy1.setDescription("Record count mismatch between input and output");

        DataDiscrepancy discrepancy2 = new DataDiscrepancy();
        discrepancy2.setCorrelationId(java.util.UUID.randomUUID());
        discrepancy2.setSourceSystem("MAINFRAME_B");
        discrepancy2.setModuleName("PIPELINE_MONITOR");
        discrepancy2.setDiscrepancyType(DataDiscrepancy.DiscrepancyType.PROCESSING_TIMEOUT);
        discrepancy2.setSeverity(DataDiscrepancy.DiscrepancySeverity.MEDIUM);
        discrepancy2.setExpectedValue("< 60 minutes");
        discrepancy2.setActualValue("75 minutes");
        discrepancy2.setDescription("Processing timeout detected between stages");

        sampleDiscrepancies = Arrays.asList(discrepancy1, discrepancy2);
    }

    @Nested
    @DisplayName("Statistics Endpoint Tests")
    class StatisticsEndpointTests {

        @Test
        @DisplayName("Should return statistics for valid date range")
        void testGetAuditStatistics_ValidDateRange_ReturnsStatistics() throws Exception {
            // Given
            LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
            LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
            
            when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(sampleStatistics);

            // When & Then
            mockMvc.perform(get("/api/audit/statistics")
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
                .andExpect(jsonPath("$.averageEventsPerDay").value(32.26))
                .andExpect(jsonPath("$.peakEventsPerDay").value(45));
        }

        @Test
        @DisplayName("Should return 400 for missing start date")
        void testGetAuditStatistics_MissingStartDate_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/statistics")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing end date")
        void testGetAuditStatistics_MissingEndDate_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-01T00:00:00")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when start date is after end date")
        void testGetAuditStatistics_StartDateAfterEndDate_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-31T23:59:59")
                    .param("endDate", "2024-01-01T00:00:00")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void testGetAuditStatistics_ServiceException_ReturnsInternalServerError() throws Exception {
            // Given
            when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/api/audit/statistics")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Discrepancies Endpoint Tests")
    class DiscrepanciesEndpointTests {

        @Test
        @DisplayName("Should return discrepancies with no filters")
        void testGetDataDiscrepancies_NoFilters_ReturnsDiscrepancies() throws Exception {
            // Given
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(sampleDiscrepancies);

            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_A"))
                .andExpect(jsonPath("$[0].moduleName").value("SQL_LOADER"))
                .andExpect(jsonPath("$[0].discrepancyType").value("RECORD_COUNT_MISMATCH"))
                .andExpect(jsonPath("$[0].severity").value("MEDIUM"))
                .andExpect(jsonPath("$[0].expectedValue").value("1000"))
                .andExpect(jsonPath("$[0].actualValue").value("995"))
                .andExpect(jsonPath("$[1].sourceSystem").value("MAINFRAME_B"))
                .andExpect(jsonPath("$[1].moduleName").value("PIPELINE_MONITOR"))
                .andExpect(jsonPath("$[1].discrepancyType").value("PROCESSING_TIMEOUT"))
                .andExpect(jsonPath("$[1].severity").value("MEDIUM"));
        }

        @Test
        @DisplayName("Should return discrepancies with valid filters")
        void testGetDataDiscrepancies_ValidFilters_ReturnsFilteredDiscrepancies() throws Exception {
            // Given
            List<DataDiscrepancy> filteredDiscrepancies = Arrays.asList(sampleDiscrepancies.get(0));
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(filteredDiscrepancies);

            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("sourceSystem", "MAINFRAME_A")
                    .param("severity", "HIGH")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sourceSystem").value("MAINFRAME_A"))
                .andExpect(jsonPath("$[0].severity").value("MEDIUM"));
        }

        @Test
        @DisplayName("Should return 400 for invalid severity filter")
        void testGetDataDiscrepancies_InvalidSeverity_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("severity", "INVALID_SEVERITY")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid status filter")
        void testGetDataDiscrepancies_InvalidStatus_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("status", "INVALID_STATUS")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid date format")
        void testGetDataDiscrepancies_InvalidDateFormat_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("startDate", "invalid-date-format")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return discrepancies with valid date filters")
        void testGetDataDiscrepancies_ValidDateFilters_ReturnsDiscrepancies() throws Exception {
            // Given
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenReturn(sampleDiscrepancies);

            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .param("startDate", "2024-01-01T00:00:00")
                    .param("endDate", "2024-01-31T23:59:59")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void testGetDataDiscrepancies_ServiceException_ReturnsInternalServerError() throws Exception {
            // Given
            when(auditService.getDataDiscrepancies(anyMap()))
                .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/api/audit/discrepancies")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        }
    }
}