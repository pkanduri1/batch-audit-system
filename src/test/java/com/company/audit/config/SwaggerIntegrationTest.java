package com.company.audit.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify Swagger/OpenAPI configuration is properly loaded.
 */
@SpringBootTest
@ActiveProfiles("test")
class SwaggerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void testSwaggerConfigurationIsLoaded() {
        // Verify that SwaggerConfig bean is loaded
        SwaggerConfig swaggerConfig = applicationContext.getBean(SwaggerConfig.class);
        assertThat(swaggerConfig).isNotNull();
    }

    @Test
    void testOpenAPIBeanIsCreated() {
        // Verify that OpenAPI bean is created and configured
        OpenAPI openAPI = applicationContext.getBean(OpenAPI.class);
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Batch Audit System API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void testGroupedOpenApiBeanIsCreated() {
        // Verify that GroupedOpenApi beans are created
        assertThat(applicationContext.getBeansOfType(GroupedOpenApi.class)).hasSize(2);
        
        // Verify audit system API group
        GroupedOpenApi auditSystemApi = applicationContext.getBean("auditSystemApi", GroupedOpenApi.class);
        assertThat(auditSystemApi).isNotNull();
        assertThat(auditSystemApi.getGroup()).isEqualTo("audit-system");
        
        // Verify actuator API group
        GroupedOpenApi actuatorApi = applicationContext.getBean("actuatorApi", GroupedOpenApi.class);
        assertThat(actuatorApi).isNotNull();
        assertThat(actuatorApi.getGroup()).isEqualTo("actuator");
    }

    @Test
    void testSpringDocPropertiesAreConfigured() {
        // Verify that SpringDoc properties are properly configured
        String apiDocsPath = applicationContext.getEnvironment().getProperty("springdoc.api-docs.path");
        String swaggerUIPath = applicationContext.getEnvironment().getProperty("springdoc.swagger-ui.path");
        
        assertThat(apiDocsPath).isEqualTo("/api-docs");
        assertThat(swaggerUIPath).isEqualTo("/swagger-ui.html");
    }
}