package com.company.audit.integration;

import com.company.audit.BatchAuditApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SpringDoc OpenAPI v2 Swagger UI functionality and API documentation generation.
 * Tests Spring Boot 3.4+ integration with SpringDoc OpenAPI v2.
 * 
 * Requirements tested: 6.7
 */
@SpringBootTest(classes = BatchAuditApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "springdoc.api-docs.enabled=true",
    "springdoc.swagger-ui.enabled=true",
    "springdoc.swagger-ui.path=/swagger-ui.html",
    "springdoc.api-docs.path=/v3/api-docs",
    "springdoc.swagger-ui.operationsSorter=method",
    "springdoc.swagger-ui.tagsSorter=alpha",
    "logging.level.org.springdoc=DEBUG"
})
class SwaggerOpenApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test Swagger UI accessibility and basic functionality
     * Requirements: 6.7
     */
    @Test
    void testSwaggerUiAccessibilityAndBasicFunctionality() throws Exception {
        // Test Swagger UI main page
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        // Test Swagger UI index page (alternative path)
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));

        // Test Swagger UI configuration endpoint
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.urls").exists());
    }

    /**
     * Test OpenAPI 3.0 JSON documentation generation
     * Requirements: 6.7
     */
    @Test
    void testOpenApi3JsonDocumentationGeneration() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.openapi").value("3.0.1"))
            .andExpect(jsonPath("$.info").exists())
            .andExpect(jsonPath("$.info.title").value("Batch Audit System API"))
            .andExpect(jsonPath("$.info.version").value("1.0.0"))
            .andExpect(jsonPath("$.info.description").exists())
            .andExpect(jsonPath("$.servers").isArray())
            .andExpect(jsonPath("$.paths").exists())
            .andExpect(jsonPath("$.components").exists())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);

        // Verify API info section
        JsonNode info = openApiDoc.get("info");
        assertThat(info.get("title").asText()).isEqualTo("Batch Audit System API");
        assertThat(info.get("version").asText()).isEqualTo("1.0.0");
        assertThat(info.get("description").asText()).contains("audit trail system");

        // Verify contact information
        if (info.has("contact")) {
            JsonNode contact = info.get("contact");
            assertThat(contact.get("name").asText()).isEqualTo("Audit Team");
            assertThat(contact.get("email").asText()).contains("@company.com");
        }

        // Verify servers configuration
        JsonNode servers = openApiDoc.get("servers");
        assertThat(servers.isArray()).isTrue();
        assertThat(servers.size()).isGreaterThanOrEqualTo(1);
    }

    /**
     * Test API endpoints documentation in OpenAPI spec
     * Requirements: 6.7
     */
    @Test
    void testApiEndpointsDocumentationInOpenApiSpec() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);
        JsonNode paths = openApiDoc.get("paths");

        // Verify audit events endpoint
        assertThat(paths.has("/api/audit/events")).isTrue();
        JsonNode eventsEndpoint = paths.get("/api/audit/events");
        assertThat(eventsEndpoint.has("get")).isTrue();
        
        JsonNode eventsGet = eventsEndpoint.get("get");
        assertThat(eventsGet.get("summary").asText()).contains("audit events");
        assertThat(eventsGet.get("operationId").asText()).isNotEmpty();
        assertThat(eventsGet.has("parameters")).isTrue();
        assertThat(eventsGet.has("responses")).isTrue();

        // Verify reconciliation endpoint
        assertThat(paths.has("/api/audit/reconciliation/{correlationId}")).isTrue();
        JsonNode reconciliationEndpoint = paths.get("/api/audit/reconciliation/{correlationId}");
        assertThat(reconciliationEndpoint.has("get")).isTrue();
        
        JsonNode reconciliationGet = reconciliationEndpoint.get("get");
        assertThat(reconciliationGet.get("summary").asText()).contains("reconciliation");
        assertThat(reconciliationGet.has("parameters")).isTrue();

        // Verify statistics endpoint
        assertThat(paths.has("/api/audit/statistics")).isTrue();
        JsonNode statisticsEndpoint = paths.get("/api/audit/statistics");
        assertThat(statisticsEndpoint.has("get")).isTrue();

        // Verify discrepancies endpoint
        assertThat(paths.has("/api/audit/discrepancies")).isTrue();
        JsonNode discrepanciesEndpoint = paths.get("/api/audit/discrepancies");
        assertThat(discrepanciesEndpoint.has("get")).isTrue();

        // Verify reconciliation reports endpoint
        assertThat(paths.has("/api/audit/reconciliation/reports")).isTrue();
        JsonNode reportsEndpoint = paths.get("/api/audit/reconciliation/reports");
        assertThat(reportsEndpoint.has("get")).isTrue();
    }

    /**
     * Test API schemas and components documentation
     * Requirements: 6.7
     */
    @Test
    void testApiSchemasAndComponentsDocumentation() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);
        JsonNode components = openApiDoc.get("components");
        assertThat(components).isNotNull();

        JsonNode schemas = components.get("schemas");
        assertThat(schemas).isNotNull();

        // Verify AuditEventDTO schema
        assertThat(schemas.has("AuditEventDTO")).isTrue();
        JsonNode auditEventSchema = schemas.get("AuditEventDTO");
        assertThat(auditEventSchema.get("type").asText()).isEqualTo("object");
        assertThat(auditEventSchema.has("properties")).isTrue();
        
        JsonNode auditEventProperties = auditEventSchema.get("properties");
        assertThat(auditEventProperties.has("auditId")).isTrue();
        assertThat(auditEventProperties.has("correlationId")).isTrue();
        assertThat(auditEventProperties.has("sourceSystem")).isTrue();
        assertThat(auditEventProperties.has("checkpointStage")).isTrue();
        assertThat(auditEventProperties.has("status")).isTrue();

        // Verify ReconciliationReportDTO schema
        assertThat(schemas.has("ReconciliationReportDTO")).isTrue();
        JsonNode reconciliationSchema = schemas.get("ReconciliationReportDTO");
        assertThat(reconciliationSchema.get("type").asText()).isEqualTo("object");
        assertThat(reconciliationSchema.has("properties")).isTrue();

        JsonNode reconciliationProperties = reconciliationSchema.get("properties");
        assertThat(reconciliationProperties.has("correlationId")).isTrue();
        assertThat(reconciliationProperties.has("sourceSystem")).isTrue();
        assertThat(reconciliationProperties.has("checkpoints")).isTrue();
        assertThat(reconciliationProperties.has("discrepancies")).isTrue();

        // Verify AuditStatistics schema
        assertThat(schemas.has("AuditStatistics")).isTrue();
        JsonNode statisticsSchema = schemas.get("AuditStatistics");
        assertThat(statisticsSchema.get("type").asText()).isEqualTo("object");
        assertThat(statisticsSchema.has("properties")).isTrue();

        JsonNode statisticsProperties = statisticsSchema.get("properties");
        assertThat(statisticsProperties.has("totalEvents")).isTrue();
        assertThat(statisticsProperties.has("successfulEvents")).isTrue();
        assertThat(statisticsProperties.has("failedEvents")).isTrue();

        // Verify DataDiscrepancy schema
        assertThat(schemas.has("DataDiscrepancy")).isTrue();
        JsonNode discrepancySchema = schemas.get("DataDiscrepancy");
        assertThat(discrepancySchema.get("type").asText()).isEqualTo("object");

        // Verify ErrorResponse schema
        assertThat(schemas.has("ErrorResponse")).isTrue();
        JsonNode errorSchema = schemas.get("ErrorResponse");
        assertThat(errorSchema.get("type").asText()).isEqualTo("object");
    }

    /**
     * Test API parameter documentation and validation
     * Requirements: 6.7
     */
    @Test
    void testApiParameterDocumentationAndValidation() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);
        JsonNode paths = openApiDoc.get("paths");

        // Test audit events endpoint parameters
        JsonNode eventsEndpoint = paths.get("/api/audit/events");
        JsonNode eventsParameters = eventsEndpoint.get("get").get("parameters");
        assertThat(eventsParameters.isArray()).isTrue();

        // Verify parameter documentation
        boolean hasSourceSystemParam = false;
        boolean hasModuleNameParam = false;
        boolean hasStatusParam = false;
        boolean hasPaginationParams = false;

        for (JsonNode param : eventsParameters) {
            String paramName = param.get("name").asText();
            switch (paramName) {
                case "sourceSystem":
                    hasSourceSystemParam = true;
                    assertThat(param.get("in").asText()).isEqualTo("query");
                    assertThat(param.get("description").asText()).contains("source system");
                    break;
                case "moduleName":
                    hasModuleNameParam = true;
                    assertThat(param.get("in").asText()).isEqualTo("query");
                    assertThat(param.get("description").asText()).contains("module");
                    break;
                case "status":
                    hasStatusParam = true;
                    assertThat(param.get("in").asText()).isEqualTo("query");
                    assertThat(param.get("description").asText()).contains("status");
                    break;
                case "page":
                case "size":
                    hasPaginationParams = true;
                    assertThat(param.get("in").asText()).isEqualTo("query");
                    break;
            }
        }

        assertThat(hasSourceSystemParam).isTrue();
        assertThat(hasModuleNameParam).isTrue();
        assertThat(hasStatusParam).isTrue();
        assertThat(hasPaginationParams).isTrue();

        // Test reconciliation endpoint path parameter
        JsonNode reconciliationEndpoint = paths.get("/api/audit/reconciliation/{correlationId}");
        JsonNode reconciliationParameters = reconciliationEndpoint.get("get").get("parameters");
        
        boolean hasCorrelationIdParam = false;
        for (JsonNode param : reconciliationParameters) {
            if ("correlationId".equals(param.get("name").asText())) {
                hasCorrelationIdParam = true;
                assertThat(param.get("in").asText()).isEqualTo("path");
                assertThat(param.get("required").asBoolean()).isTrue();
                assertThat(param.get("description").asText()).contains("correlation");
                break;
            }
        }
        assertThat(hasCorrelationIdParam).isTrue();
    }

    /**
     * Test API response documentation and status codes
     * Requirements: 6.7
     */
    @Test
    void testApiResponseDocumentationAndStatusCodes() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);
        JsonNode paths = openApiDoc.get("paths");

        // Test audit events endpoint responses
        JsonNode eventsEndpoint = paths.get("/api/audit/events");
        JsonNode eventsResponses = eventsEndpoint.get("get").get("responses");

        // Verify 200 OK response
        assertThat(eventsResponses.has("200")).isTrue();
        JsonNode okResponse = eventsResponses.get("200");
        assertThat(okResponse.get("description").asText()).contains("success");
        assertThat(okResponse.has("content")).isTrue();
        
        JsonNode okContent = okResponse.get("content");
        assertThat(okContent.has("application/json")).isTrue();
        JsonNode jsonContent = okContent.get("application/json");
        assertThat(jsonContent.has("schema")).isTrue();

        // Verify 400 Bad Request response
        assertThat(eventsResponses.has("400")).isTrue();
        JsonNode badRequestResponse = eventsResponses.get("400");
        assertThat(badRequestResponse.get("description").asText()).contains("Invalid");

        // Test reconciliation endpoint responses
        JsonNode reconciliationEndpoint = paths.get("/api/audit/reconciliation/{correlationId}");
        JsonNode reconciliationResponses = reconciliationEndpoint.get("get").get("responses");

        // Verify 200 OK response
        assertThat(reconciliationResponses.has("200")).isTrue();
        JsonNode reconciliationOkResponse = reconciliationResponses.get("200");
        assertThat(reconciliationOkResponse.has("content")).isTrue();

        // Verify 404 Not Found response
        assertThat(reconciliationResponses.has("404")).isTrue();
        JsonNode notFoundResponse = reconciliationResponses.get("404");
        assertThat(notFoundResponse.get("description").asText()).contains("not found");
    }

    /**
     * Test API tags and grouping functionality
     * Requirements: 6.7
     */
    @Test
    void testApiTagsAndGroupingFunctionality() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn();

        String openApiJson = result.getResponse().getContentAsString();
        JsonNode openApiDoc = objectMapper.readTree(openApiJson);

        // Verify tags section exists
        JsonNode tags = openApiDoc.get("tags");
        assertThat(tags).isNotNull();
        assertThat(tags.isArray()).isTrue();

        // Verify Audit Dashboard tag
        boolean hasAuditDashboardTag = false;
        for (JsonNode tag : tags) {
            if ("Audit Dashboard".equals(tag.get("name").asText())) {
                hasAuditDashboardTag = true;
                assertThat(tag.get("description").asText()).contains("audit trail");
                break;
            }
        }
        assertThat(hasAuditDashboardTag).isTrue();

        // Verify endpoints are tagged correctly
        JsonNode paths = openApiDoc.get("paths");
        JsonNode eventsEndpoint = paths.get("/api/audit/events");
        JsonNode eventsTags = eventsEndpoint.get("get").get("tags");
        assertThat(eventsTags.isArray()).isTrue();
        assertThat(eventsTags.get(0).asText()).isEqualTo("Audit Dashboard");

        // Test grouped API documentation
        mockMvc.perform(get("/v3/api-docs/audit-system"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.openapi").exists())
            .andExpect(jsonPath("$.paths").exists());
    }

    /**
     * Test Swagger UI configuration and customization
     * Requirements: 6.7
     */
    @Test
    void testSwaggerUiConfigurationAndCustomization() throws Exception {
        // Test Swagger UI configuration endpoint
        MvcResult configResult = mockMvc.perform(get("/v3/api-docs/swagger-config"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        String configJson = configResult.getResponse().getContentAsString();
        JsonNode config = objectMapper.readTree(configJson);

        // Verify configuration contains expected URLs
        assertThat(config.has("urls")).isTrue();
        JsonNode urls = config.get("urls");
        assertThat(urls.isArray()).isTrue();

        // Verify main API documentation URL
        boolean hasMainApiUrl = false;
        for (JsonNode url : urls) {
            if (url.get("url").asText().contains("/v3/api-docs")) {
                hasMainApiUrl = true;
                assertThat(url.get("name").asText()).isNotEmpty();
                break;
            }
        }
        assertThat(hasMainApiUrl).isTrue();

        // Test that Swagger UI loads with custom configuration
        MvcResult uiResult = mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isOk())
            .andReturn();

        String uiContent = uiResult.getResponse().getContentAsString();
        assertThat(uiContent).contains("swagger-ui");
        assertThat(uiContent).contains("Swagger UI");
    }
}