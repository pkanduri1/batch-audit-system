package com.company.audit.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditSecurityConfig.
 * Tests JWT authentication converter, CORS configuration, and security setup.
 */
@SpringBootTest(classes = {AuditSecurityConfig.class})
@TestPropertySource(properties = {
        "audit.security.jwt.jwk-set-uri=http://localhost:8080/.well-known/jwks.json",
        "audit.security.jwt.issuer=test-issuer",
        "audit.security.cors.allowed-origins=http://localhost:3000,http://localhost:8080",
        "audit.security.swagger.enabled=true"
})
class AuditSecurityConfigTest {

    @Autowired
    private AuditSecurityConfig auditSecurityConfig;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void testJwtAuthenticationConverter() {
        // Given
        JwtAuthenticationConverter converter = auditSecurityConfig.jwtAuthenticationConverter();
        
        Jwt jwt = createTestJwt(List.of("AUDIT_VIEWER", "AUDIT_ADMIN"));

        // When
        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(2);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_AUDIT_VIEWER", "ROLE_AUDIT_ADMIN");
    }

    @Test
    void testJwtAuthenticationConverterWithGroupsClaim() {
        // Given
        JwtAuthenticationConverter converter = auditSecurityConfig.jwtAuthenticationConverter();
        
        Jwt jwt = createTestJwtWithGroups(List.of("AUDIT_VIEWER", "OTHER_ROLE"));

        // When
        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_AUDIT_VIEWER");
    }

    @Test
    void testJwtAuthenticationConverterWithNoAuditRoles() {
        // Given
        JwtAuthenticationConverter converter = auditSecurityConfig.jwtAuthenticationConverter();
        
        Jwt jwt = createTestJwt(List.of("OTHER_ROLE", "ANOTHER_ROLE"));

        // When
        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).isEmpty();
    }

    @Test
    void testCorsConfigurationSource() {
        // Given
        CorsConfigurationSource corsSource = auditSecurityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/audit/events");

        // When
        var corsConfig = corsSource.getCorsConfiguration(request);

        // Then
        assertThat(corsConfig).isNotNull();
        assertThat(corsConfig.getAllowedOrigins()).containsExactlyInAnyOrder(
                "http://localhost:3000", "http://localhost:8080");
        assertThat(corsConfig.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(corsConfig.getAllowedHeaders()).contains("*");
        assertThat(corsConfig.getAllowCredentials()).isTrue();
        assertThat(corsConfig.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void testCorsConfigurationForSwaggerEndpoints() {
        // Given
        CorsConfigurationSource corsSource = auditSecurityConfig.corsConfigurationSource();
        MockHttpServletRequest swaggerRequest = new MockHttpServletRequest();
        swaggerRequest.setRequestURI("/swagger-ui/index.html");
        MockHttpServletRequest apiDocsRequest = new MockHttpServletRequest();
        apiDocsRequest.setRequestURI("/api-docs/swagger-config");

        // When
        var swaggerCorsConfig = corsSource.getCorsConfiguration(swaggerRequest);
        var apiDocsCorsConfig = corsSource.getCorsConfiguration(apiDocsRequest);

        // Then
        assertThat(swaggerCorsConfig).isNotNull();
        assertThat(apiDocsCorsConfig).isNotNull();
        
        assertThat(swaggerCorsConfig.getAllowedOrigins()).containsExactlyInAnyOrder(
                "http://localhost:3000", "http://localhost:8080");
        assertThat(apiDocsCorsConfig.getAllowedOrigins()).containsExactlyInAnyOrder(
                "http://localhost:3000", "http://localhost:8080");
    }

    @Test
    void testJwtDecoderConfiguration() {
        // Given & When
        JwtDecoder decoder = auditSecurityConfig.jwtDecoder();

        // Then
        assertThat(decoder).isNotNull();
        assertThat(decoder).isInstanceOf(JwtDecoder.class);
    }

    /**
     * Creates a test JWT with audit_roles claim.
     */
    private Jwt createTestJwt(List<String> auditRoles) {
        return new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256", "typ", "JWT"),
                Map.of(
                        "sub", "test-user",
                        "iss", "test-issuer",
                        "aud", "audit-system",
                        "exp", Instant.now().plusSeconds(3600).getEpochSecond(),
                        "iat", Instant.now().getEpochSecond(),
                        "audit_roles", auditRoles,
                        "preferred_username", "test-user",
                        "email", "test-user@company.com"
                )
        );
    }

    /**
     * Creates a test JWT with groups claim (fallback for audit roles).
     */
    private Jwt createTestJwtWithGroups(List<String> groups) {
        return new Jwt(
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256", "typ", "JWT"),
                Map.of(
                        "sub", "test-user",
                        "iss", "test-issuer",
                        "aud", "audit-system",
                        "exp", Instant.now().plusSeconds(3600).getEpochSecond(),
                        "iat", Instant.now().getEpochSecond(),
                        "groups", groups,
                        "preferred_username", "test-user",
                        "email", "test-user@company.com"
                )
        );
    }
}