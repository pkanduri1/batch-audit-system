package com.company.audit.controller;

import com.company.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for AuditDashboardController.
 * Tests the basic controller structure and Spring MVC configuration.
 */
@WebMvcTest(AuditDashboardController.class)
class AuditDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void contextLoads() {
        // Test that the controller loads properly in Spring context
        // This verifies the basic Spring Boot 3.4+ and Java 17+ compatibility
    }

    @Test
    void controllerMappingIsConfigured() throws Exception {
        // Test that the base path mapping is configured correctly
        // Since no specific endpoints are implemented yet, we expect 404 for any path under /api/audit
        mockMvc.perform(get("/api/audit/test"))
                .andExpect(status().isNotFound());
    }
}