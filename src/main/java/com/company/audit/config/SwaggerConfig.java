package com.company.audit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the Batch Audit System API.
 * Provides comprehensive API documentation and interactive testing interface with OAuth2/JWT security integration.
 */
@Configuration
public class SwaggerConfig {

    @Value("${audit.security.oauth2.authorization-uri:http://localhost:8080/oauth2/authorize}")
    private String authorizationUri;

    @Value("${audit.security.oauth2.token-uri:http://localhost:8080/oauth2/token}")
    private String tokenUri;

    @Value("${audit.security.oauth2.client-id:audit-system-client}")
    private String clientId;

    @Value("${audit.security.swagger.enabled:true}")
    private boolean swaggerSecurityEnabled;

    /**
     * Configure OpenAPI 3.0 documentation with comprehensive metadata and security integration.
     * 
     * @return OpenAPI configuration with title, description, version, contact info, servers, and security schemes
     */
    @Bean
    public OpenAPI auditSystemOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
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

        // Add security configuration if enabled
        if (swaggerSecurityEnabled) {
            openAPI.components(createSecurityComponents())
                   .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                   .addSecurityItem(new SecurityRequirement().addList("oauth2"));
        }

        return openAPI;
    }

    /**
     * Create security components for OpenAPI documentation.
     * Configures both JWT Bearer token and OAuth2 authorization code flow.
     * 
     * @return Components with security schemes for JWT and OAuth2
     */
    private Components createSecurityComponents() {
        return new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer token authentication. " +
                                   "Obtain a JWT token from your identity provider and include it in the Authorization header."))
                .addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .description("OAuth2 authorization code flow for interactive API testing. " +
                                   "Requires AUDIT_VIEWER or AUDIT_ADMIN role for access to audit endpoints.")
                        .flows(new OAuthFlows()
                                .authorizationCode(new OAuthFlow()
                                        .authorizationUrl(authorizationUri)
                                        .tokenUrl(tokenUri)
                                        .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                .addString("audit:read", "Read access to audit data")
                                                .addString("audit:write", "Write access to audit data")
                                                .addString("audit:admin", "Administrative access to audit system")))));
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