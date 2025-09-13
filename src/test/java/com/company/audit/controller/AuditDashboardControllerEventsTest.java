package com.company.audit.controller;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import com.company.audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the audit events endpoint in AuditDashboardController.
 * Tests the GET /api/audit/events endpoint with various filtering and pagination scenarios.
 */
@WebMvcTest(AuditDashboardController.class)
class AuditDashboardControllerEventsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<AuditEvent> sampleAuditEvents;
    private UUID correlationId;

    @BeforeEach
    void setUp() {
        correlationId = UUID.randomUUID();
        
        // Create sample audit events for testing
        AuditEvent event1 = AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(correlationId)
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
            .correlationId(correlationId)
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

    @Test
    void getAuditEvents_WithoutFilters_ShouldReturnPagedResponse() throws Exception {
        // Arrange
        when(auditService.getAuditEventsWithFilters(null, null, null, null, 0, 20))
            .thenReturn(sampleAuditEvents);
        when(auditService.countAuditEventsWithFilters(null, null, null, null))
            .thenReturn(2L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
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
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    @Test
    void getAuditEvents_WithSourceSystemFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        String sourceSystem = "MAINFRAME_SYSTEM_A";
        when(auditService.getAuditEventsWithFilters(eq(sourceSystem), isNull(), isNull(), isNull(), eq(0), eq(20)))
            .thenReturn(sampleAuditEvents);
        when(auditService.countAuditEventsWithFilters(eq(sourceSystem), isNull(), isNull(), isNull()))
            .thenReturn(2L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("sourceSystem", sourceSystem)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].sourceSystem").value(sourceSystem))
                .andExpect(jsonPath("$.content[1].sourceSystem").value(sourceSystem));
    }

    @Test
    void getAuditEvents_WithModuleNameFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        String moduleName = "FILE_TRANSFER";
        List<AuditEvent> filteredEvents = Arrays.asList(sampleAuditEvents.get(0)); // Only first event
        
        when(auditService.getAuditEventsWithFilters(isNull(), eq(moduleName), isNull(), isNull(), eq(0), eq(20)))
            .thenReturn(filteredEvents);
        when(auditService.countAuditEventsWithFilters(isNull(), eq(moduleName), isNull(), isNull()))
            .thenReturn(1L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("moduleName", moduleName)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].moduleName").value(moduleName))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAuditEvents_WithStatusFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        AuditStatus status = AuditStatus.SUCCESS;
        when(auditService.getAuditEventsWithFilters(isNull(), isNull(), eq(status), isNull(), eq(0), eq(20)))
            .thenReturn(sampleAuditEvents);
        when(auditService.countAuditEventsWithFilters(isNull(), isNull(), eq(status), isNull()))
            .thenReturn(2L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("status", status.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].status").value(status.name()))
                .andExpect(jsonPath("$.content[1].status").value(status.name()));
    }

    @Test
    void getAuditEvents_WithCheckpointStageFilter_ShouldReturnFilteredResults() throws Exception {
        // Arrange
        CheckpointStage checkpointStage = CheckpointStage.RHEL_LANDING;
        List<AuditEvent> filteredEvents = Arrays.asList(sampleAuditEvents.get(0)); // Only first event
        
        when(auditService.getAuditEventsWithFilters(isNull(), isNull(), isNull(), eq(checkpointStage), eq(0), eq(20)))
            .thenReturn(filteredEvents);
        when(auditService.countAuditEventsWithFilters(isNull(), isNull(), isNull(), eq(checkpointStage)))
            .thenReturn(1L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("checkpointStage", checkpointStage.name())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].checkpointStage").value(checkpointStage.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAuditEvents_WithCustomPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        int page = 1;
        int size = 10;
        when(auditService.getAuditEventsWithFilters(isNull(), isNull(), isNull(), isNull(), eq(page), eq(size)))
            .thenReturn(sampleAuditEvents);
        when(auditService.countAuditEventsWithFilters(isNull(), isNull(), isNull(), isNull()))
            .thenReturn(25L); // Total of 25 events

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page").value(page))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void getAuditEvents_WithAllFilters_ShouldReturnFilteredResults() throws Exception {
        // Arrange
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

        // Act & Assert
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
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getAuditEvents_WithInvalidPageNumber_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAuditEvents_WithInvalidPageSize_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        mockMvc.perform(get("/api/audit/events")
                .param("size", "1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAuditEvents_WithServiceException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAuditEvents_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
        // Arrange
        when(auditService.getAuditEventsWithFilters(any(), any(), any(), any(), anyInt(), anyInt()))
            .thenReturn(Arrays.asList());
        when(auditService.countAuditEventsWithFilters(any(), any(), any(), any()))
            .thenReturn(0L);

        // Act & Assert
        mockMvc.perform(get("/api/audit/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.numberOfElements").value(0));
    }
}