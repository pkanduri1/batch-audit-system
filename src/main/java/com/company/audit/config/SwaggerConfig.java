package com.company.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the Batch Audit System API.
 * Provides comprehensive API documentation and interactive testing interface.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configure OpenAPI 3.0 documentation with comprehensive metadata.
     * 
     * @return OpenAPI configuration with title, description, version, contact info, and servers
     */
    @Bean
    public OpenAPI auditSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Batch Audit System API")
                        .description("Comprehensive end-to-end audit trail system for enterprise data processing pipelines. " +
                                   "Tracks data lineage from multiple mainframe source systems through Oracle staging databases, " +
                                   "Java module transformations, and final output file generation. Provides complete traceability, " +
                                   "data integrity verification, error diagnosis capabilities, and compliance reporting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Audit System Team")
                                .email("audit-team@company.com")
                                .url("https://company.com/audit-team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development server"),
                        new Server()
                                .url("https://audit-api-dev.company.com")
                                .description("Development environment"),
                        new Server()
                                .url("https://audit-api.company.com")
                                .description("Production server")
                ));
    }

    /**
     * Configure API grouping for audit system endpoints.
     * Groups all audit-related endpoints under a single API group for better organization.
     * 
     * @return GroupedOpenApi configuration for audit system endpoints
     */
    @Bean
    public GroupedOpenApi auditSystemApi() {
        return GroupedOpenApi.builder()
                .group("audit-system")
                .displayName("Audit System API")
                .pathsToMatch("/api/audit/**")
                .build();
    }

    /**
     * Configure API grouping for health and monitoring endpoints.
     * Separates system health endpoints from business logic endpoints.
     * 
     * @return GroupedOpenApi configuration for actuator endpoints
     */
    @Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder()
                .group("actuator")
                .displayName("System Health & Monitoring")
                .pathsToMatch("/actuator/**")
                .build();
    }
}