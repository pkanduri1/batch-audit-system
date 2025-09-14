package com.company.audit.model.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReconciliationReportDTO sealed class hierarchy using Java 17+ features.
 * Tests sealed class behavior, JSON serialization, and factory methods.
 */
class ReconciliationReportDTOTest {

    private ObjectMapper objectMapper;
    private UUID testCorrelationId;
    private LocalDateTime testTimestamp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        testCorrelationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        testTimestamp = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
    }

    @Test
    @DisplayName("Should create StandardReconciliationReport using factory method")
    void shouldCreateStandardReconciliationReport() {
        // Given
        Map<String, Long> checkpointCounts = Map.of(
            "RHEL_LANDING", 150000L,
            "SQLLOADER_COMPLETE", 149500L,
            "FILE_GENERATED", 149500L
        );
        Map<String, Double> controlTotals = Map.of(
            "TOTAL_AMOUNT", 1250000.50
        );
        ReconciliationReportDTO.BasicSummary summary = new ReconciliationReportDTO.BasicSummary(
            149500L, 500L, 0L, 99.67
        );

        // When
        ReconciliationReportDTO.StandardReconciliationReport report = 
            ReconciliationReportDTO.createStandardReport(
                testCorrelationId,
                "MAINFRAME_SYSTEM_A",
                testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                testTimestamp.minusHours(2),
                testTimestamp,
                checkpointCounts,
                controlTotals,
                0,
                summary
            );

        // Then
        assertNotNull(report);
        assertEquals("STANDARD", report.getReportType());
        assertEquals(testCorrelationId, report.getCorrelationId());
        assertEquals("MAINFRAME_SYSTEM_A", report.getSourceSystem());
        assertEquals(testTimestamp, report.getReportGeneratedAt());
        assertEquals(ReconciliationReportDTO.ReportStatus.SUCCESS, report.getOverallStatus());
        assertEquals(checkpointCounts, report.getCheckpointCounts());
        assertEquals(controlTotals, report.getControlTotals());
        assertEquals(0, report.getDiscrepancyCount());
        assertEquals(summary, report.getSummary());
    }

    @Test
    @DisplayName("Should create DetailedReconciliationReport using factory method")
    void shouldCreateDetailedReconciliationReport() {
        // Given
        Map<String, Long> checkpointCounts = Map.of(
            "RHEL_LANDING", 150000L,
            "SQLLOADER_COMPLETE", 149500L,
            "FILE_GENERATED", 149500L
        );
        Map<String, Double> controlTotals = Map.of(
            "TOTAL_AMOUNT", 1250000.50
        );
        List<DataDiscrepancy> discrepancies = List.of();
        ReconciliationReportDTO.DetailedSummary summary = new ReconciliationReportDTO.DetailedSummary(
            150000L, 149500L, 500L, 0L, 99.67, 7200000L, true, 48.0
        );
        List<ReconciliationReportDTO.CheckpointDetail> checkpointDetails = List.of(
            new ReconciliationReportDTO.CheckpointDetail(
                "RHEL_LANDING", 150000L, 1250000.50, "SUCCESS",
                testTimestamp.minusHours(2), testTimestamp.minusHours(1), 3600000L
            )
        );
        ReconciliationReportDTO.PerformanceMetrics performanceMetrics = 
            new ReconciliationReportDTO.PerformanceMetrics(
                20.83, 512.5, 75.2, 8, 1250L, 15.7
            );

        // When
        ReconciliationReportDTO.DetailedReconciliationReport report = 
            ReconciliationReportDTO.createDetailedReport(
                testCorrelationId,
                "MAINFRAME_SYSTEM_A",
                testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                testTimestamp.minusHours(2),
                testTimestamp,
                checkpointCounts,
                controlTotals,
                discrepancies,
                summary,
                checkpointDetails,
                performanceMetrics
            );

        // Then
        assertNotNull(report);
        assertEquals("DETAILED", report.getReportType());
        assertEquals(testCorrelationId, report.getCorrelationId());
        assertEquals("MAINFRAME_SYSTEM_A", report.getSourceSystem());
        assertEquals(discrepancies, report.getDiscrepancies());
        assertEquals(summary, report.getSummary());
        assertEquals(checkpointDetails, report.getCheckpointDetails());
        assertEquals(performanceMetrics, report.getPerformanceMetrics());
    }

    @Test
    @DisplayName("Should create SummaryReconciliationReport using factory method")
    void shouldCreateSummaryReconciliationReport() {
        // When
        ReconciliationReportDTO.SummaryReconciliationReport report = 
            ReconciliationReportDTO.createSummaryReport(
                testCorrelationId,
                "MAINFRAME_SYSTEM_A",
                testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                7200000L,
                150000L,
                99.67,
                true,
                0
            );

        // Then
        assertNotNull(report);
        assertEquals("SUMMARY", report.getReportType());
        assertEquals(testCorrelationId, report.getCorrelationId());
        assertEquals("MAINFRAME_SYSTEM_A", report.getSourceSystem());
        assertEquals(7200000L, report.getTotalProcessingTimeMs());
        assertEquals(150000L, report.getTotalRecordsProcessed());
        assertEquals(99.67, report.getSuccessRate());
        assertTrue(report.getDataIntegrityValid());
        assertEquals(0, report.getCriticalIssuesCount());
    }

    @Test
    @DisplayName("Should serialize StandardReconciliationReport to JSON correctly")
    void shouldSerializeStandardReportToJSON() throws Exception {
        // Given
        Map<String, Long> checkpointCounts = Map.of("RHEL_LANDING", 150000L);
        Map<String, Double> controlTotals = Map.of("TOTAL_AMOUNT", 1250000.50);
        ReconciliationReportDTO.BasicSummary summary = new ReconciliationReportDTO.BasicSummary(
            149500L, 500L, 0L, 99.67
        );
        
        ReconciliationReportDTO.StandardReconciliationReport report = 
            ReconciliationReportDTO.createStandardReport(
                testCorrelationId, "MAINFRAME_SYSTEM_A", testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                testTimestamp.minusHours(2), testTimestamp,
                checkpointCounts, controlTotals, 0, summary
            );

        // When
        String json = objectMapper.writeValueAsString(report);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("\"reportType\":\"STANDARD\""));
        assertTrue(json.contains("\"correlationId\":\"550e8400-e29b-41d4-a716-446655440000\""));
        assertTrue(json.contains("\"sourceSystem\":\"MAINFRAME_SYSTEM_A\""));
        assertTrue(json.contains("\"overallStatus\":\"SUCCESS\""));
        assertTrue(json.contains("\"discrepancyCount\":0"));
    }

    @Test
    @DisplayName("Should validate sealed class type hierarchy")
    void shouldValidateSealedClassTypeHierarchy() {
        // Given
        ReconciliationReportDTO.StandardReconciliationReport standardReport = 
            ReconciliationReportDTO.createStandardReport(
                testCorrelationId, "MAINFRAME_SYSTEM_A", testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                testTimestamp.minusHours(2), testTimestamp,
                Map.of(), Map.of(), 0,
                new ReconciliationReportDTO.BasicSummary(100L, 0L, 0L, 100.0)
            );

        // Then - sealed class ensures only permitted subtypes exist
        assertTrue(standardReport instanceof ReconciliationReportDTO);
        assertTrue(standardReport instanceof ReconciliationReportDTO.StandardReconciliationReport);
        assertEquals("STANDARD", standardReport.getReportType());
    }

    @Test
    @DisplayName("Should test ReportStatus enum descriptions")
    void shouldTestReportStatusEnumDescriptions() {
        // Then
        assertEquals("Pipeline completed successfully", 
            ReconciliationReportDTO.ReportStatus.SUCCESS.getDescription());
        assertEquals("Pipeline completed with warnings", 
            ReconciliationReportDTO.ReportStatus.WARNING.getDescription());
        assertEquals("Pipeline failed", 
            ReconciliationReportDTO.ReportStatus.FAILURE.getDescription());
        assertEquals("Pipeline in progress", 
            ReconciliationReportDTO.ReportStatus.IN_PROGRESS.getDescription());
        assertEquals("Status unknown", 
            ReconciliationReportDTO.ReportStatus.UNKNOWN.getDescription());
    }

    @Test
    @DisplayName("Should create BasicSummary record correctly")
    void shouldCreateBasicSummaryRecord() {
        // When
        ReconciliationReportDTO.BasicSummary summary = new ReconciliationReportDTO.BasicSummary(
            149500L, 500L, 0L, 99.67
        );

        // Then
        assertNotNull(summary);
        assertEquals(149500L, summary.successfulEvents());
        assertEquals(500L, summary.failedEvents());
        assertEquals(0L, summary.warningEvents());
        assertEquals(99.67, summary.successRate());
    }

    @Test
    @DisplayName("Should create DetailedSummary record correctly")
    void shouldCreateDetailedSummaryRecord() {
        // When
        ReconciliationReportDTO.DetailedSummary summary = new ReconciliationReportDTO.DetailedSummary(
            150000L, 149500L, 500L, 0L, 99.67, 7200000L, true, 48.0
        );

        // Then
        assertNotNull(summary);
        assertEquals(150000L, summary.totalRecordsProcessed());
        assertEquals(149500L, summary.successfulEvents());
        assertEquals(500L, summary.failedEvents());
        assertEquals(0L, summary.warningEvents());
        assertEquals(99.67, summary.successRate());
        assertEquals(7200000L, summary.totalProcessingTimeMs());
        assertTrue(summary.dataIntegrityValid());
        assertEquals(48.0, summary.averageProcessingTimePerRecordMs());
    }

    @Test
    @DisplayName("Should create CheckpointDetail record correctly")
    void shouldCreateCheckpointDetailRecord() {
        // When
        ReconciliationReportDTO.CheckpointDetail detail = new ReconciliationReportDTO.CheckpointDetail(
            "RHEL_LANDING", 150000L, 1250000.50, "SUCCESS",
            testTimestamp.minusHours(2), testTimestamp.minusHours(1), 3600000L
        );

        // Then
        assertNotNull(detail);
        assertEquals("RHEL_LANDING", detail.checkpointStage());
        assertEquals(150000L, detail.recordCount());
        assertEquals(1250000.50, detail.controlTotal());
        assertEquals("SUCCESS", detail.status());
        assertEquals(testTimestamp.minusHours(2), detail.startTime());
        assertEquals(testTimestamp.minusHours(1), detail.endTime());
        assertEquals(3600000L, detail.durationMs());
    }

    @Test
    @DisplayName("Should create PerformanceMetrics record correctly")
    void shouldCreatePerformanceMetricsRecord() {
        // When
        ReconciliationReportDTO.PerformanceMetrics metrics = new ReconciliationReportDTO.PerformanceMetrics(
            20.83, 512.5, 75.2, 8, 1250L, 15.7
        );

        // Then
        assertNotNull(metrics);
        assertEquals(20.83, metrics.recordsPerSecond());
        assertEquals(512.5, metrics.peakMemoryUsageMB());
        assertEquals(75.2, metrics.averageCpuUtilization());
        assertEquals(8, metrics.dbConnectionPoolPeakUsage());
        assertEquals(1250L, metrics.totalDbQueries());
        assertEquals(15.7, metrics.averageDbQueryTimeMs());
    }

    @Test
    @DisplayName("Should demonstrate sealed class type checking")
    void shouldDemonstrateTypeChecking() {
        // Given
        ReconciliationReportDTO.StandardReconciliationReport standardReport = 
            ReconciliationReportDTO.createStandardReport(
                testCorrelationId, "MAINFRAME_SYSTEM_A", testTimestamp,
                ReconciliationReportDTO.ReportStatus.SUCCESS,
                testTimestamp.minusHours(2), testTimestamp,
                Map.of(), Map.of(), 0,
                new ReconciliationReportDTO.BasicSummary(100L, 0L, 0L, 100.0)
            );

        // When - Using traditional instanceof checks
        String reportDescription;
        if (standardReport instanceof ReconciliationReportDTO.StandardReconciliationReport) {
            ReconciliationReportDTO.StandardReconciliationReport sr = 
                (ReconciliationReportDTO.StandardReconciliationReport) standardReport;
            reportDescription = "Standard report with " + sr.getDiscrepancyCount() + " discrepancies";
        } else {
            reportDescription = "Unknown report type";
        }

        // Then
        assertEquals("Standard report with 0 discrepancies", reportDescription);
    }

    @Test
    @DisplayName("Should maintain sealed class type safety")
    void shouldMaintainSealedClassTypeSafety() {
        // Given
        ReconciliationReportDTO report = ReconciliationReportDTO.createStandardReport(
            testCorrelationId, "MAINFRAME_SYSTEM_A", testTimestamp,
            ReconciliationReportDTO.ReportStatus.SUCCESS,
            testTimestamp.minusHours(2), testTimestamp,
            Map.of(), Map.of(), 0,
            new ReconciliationReportDTO.BasicSummary(100L, 0L, 0L, 100.0)
        );

        // Then - sealed class ensures only permitted subtypes exist
        assertTrue(report instanceof ReconciliationReportDTO.StandardReconciliationReport ||
                  report instanceof ReconciliationReportDTO.DetailedReconciliationReport ||
                  report instanceof ReconciliationReportDTO.SummaryReconciliationReport);
        
        // Traditional instanceof check
        if (report instanceof ReconciliationReportDTO.StandardReconciliationReport) {
            ReconciliationReportDTO.StandardReconciliationReport standardReport = 
                (ReconciliationReportDTO.StandardReconciliationReport) report;
            assertEquals("STANDARD", standardReport.getReportType());
            assertEquals(0, standardReport.getDiscrepancyCount());
        }
    }
}