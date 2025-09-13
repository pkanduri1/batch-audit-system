package com.company.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SwaggerConfig to verify OpenAPI configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
class SwaggerConfigTest {

    private final SwaggerConfig swaggerConfig = new SwaggerConfig();

    @Test
    void testAuditSystemOpenAPIConfiguration() {
        // Given & When
        OpenAPI openAPI = swaggerConfig.auditSystemOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Batch Audit System API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription()).contains("Comprehensive end-to-end audit trail system");
        
        // Verify contact information
        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Audit System Team");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("audit-team@company.com");
        
        // Verify servers configuration
        assertThat(openAPI.getServers()).hasSize(3);
        assertThat(openAPI.getServers().get(0).getUrl()).isEqualTo("http://localhost:8080");
        assertThat(openAPI.getServers().get(0).getDescription()).isEqualTo("Development server");
        assertThat(openAPI.getServers().get(2).getUrl()).isEqualTo("https://audit-api.company.com");
        assertThat(openAPI.getServers().get(2).getDescription()).isEqualTo("Production server");
    }

    @Test
    void testAuditSystemApiGrouping() {
        // Given & When
        GroupedOpenApi groupedOpenApi = swaggerConfig.auditSystemApi();

        // Then
        assertThat(groupedOpenApi).isNotNull();
        assertThat(groupedOpenApi.getGroup()).isEqualTo("audit-system");
        assertThat(groupedOpenApi.getDisplayName()).isEqualTo("Audit System API");
    }

    @Test
    void testActuatorApiGrouping() {
        // Given & When
        GroupedOpenApi groupedOpenApi = swaggerConfig.actuatorApi();

        // Then
        assertThat(groupedOpenApi).isNotNull();
        assertThat(groupedOpenApi.getGroup()).isEqualTo("actuator");
        assertThat(groupedOpenApi.getDisplayName()).isEqualTo("System Health & Monitoring");
    }
}