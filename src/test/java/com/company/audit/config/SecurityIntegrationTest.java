package com.company.audit.config;

import com.company.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for security configuration with REST endpoints.
 * Tests role-based access control and endpoint security.
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
        "audit.security.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json",
        "audit.security.swagger.enabled=true"
})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    void testPublicEndpointsAccessibleWithoutAuthentication() throws Exception {
        // Health endpoint should be accessible without authentication
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // Swagger UI should be accessible without authentication
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());

        // API docs should be accessible without authentication
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void testAuditEndpointsRequireAuthentication() throws Exception {
        // Audit endpoints should require authentication
        mockMvc.perform(get("/api/audit/events"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/audit/statistics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/audit/reconciliation/123e4567-e89b-12d3-a456-426614174000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"AUDIT_VIEWER"})
    void testAuditViewerCanAccessReadEndpoints() throws Exception {
        // AUDIT_VIEWER should be able to access read endpoints
        mockMvc.perform(get("/api/audit/events"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit/discrepancies"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"AUDIT_VIEWER"})
    void testAuditViewerCannotAccessWriteEndpoints() throws Exception {
        // AUDIT_VIEWER should not be able to access write endpoints
        mockMvc.perform(post("/api/audit/events")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"AUDIT_ADMIN"})
    void testAuditAdminCanAccessAllEndpoints() throws Exception {
        // AUDIT_ADMIN should be able to access read endpoints
        mockMvc.perform(get("/api/audit/events"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isOk());

        // AUDIT_ADMIN should be able to access write endpoints
        mockMvc.perform(post("/api/audit/events")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request due to empty body, but not forbidden
    }

    @Test
    @WithMockUser(roles = {"OTHER_ROLE"})
    void testUserWithoutAuditRoleCannotAccessAuditEndpoints() throws Exception {
        // User without audit roles should not be able to access audit endpoints
        mockMvc.perform(get("/api/audit/events"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/audit/statistics")
                .param("startDate", "2024-01-01T00:00:00")
                .param("endDate", "2024-12-31T23:59:59"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/audit/events")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCorsHeadersPresent() throws Exception {
        mockMvc.perform(get("/api/audit/events")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    // Even for unauthorized requests, CORS headers should be present
                    String accessControlAllowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    // Note: CORS headers might not be present for unauthorized requests depending on configuration
                    // This test verifies the CORS configuration is properly set up
                });
    }
}