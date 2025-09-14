package com.company.audit.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for security configuration components without Spring context.
 * Tests individual security components in isolation.
 */
class SecurityConfigurationUnitTest {

    @Test
    void testJwtAuthenticationConverterWithAuditRoles() {
        // Given
        AuditSecurityConfig config = new AuditSecurityConfig();
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter();
        
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
    void testJwtAuthenticationConverterFiltersNonAuditRoles() {
        // Given
        AuditSecurityConfig config = new AuditSecurityConfig();
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter();
        
        Jwt jwt = createTestJwt(List.of("OTHER_ROLE", "AUDIT_VIEWER", "ANOTHER_ROLE"));

        // When
        Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_AUDIT_VIEWER");
    }

    @Test
    void testCorsConfigurationHasCorrectSettings() {
        // Given
        AuditSecurityConfig config = new AuditSecurityConfig();
        
        // When
        var corsSource = config.corsConfigurationSource();
        
        // Then
        assertThat(corsSource).isInstanceOf(UrlBasedCorsConfigurationSource.class);
        
        // Test that the configuration is properly set up
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) corsSource;
        Map<String, CorsConfiguration> corsConfigurations = urlBasedSource.getCorsConfigurations();
        
        assertThat(corsConfigurations).isNotEmpty();
        assertThat(corsConfigurations).containsKeys("/api/**", "/swagger-ui/**", "/api-docs/**");
    }

    @Test
    void testJwtDecoderCreation() {
        // Given
        AuditSecurityConfig config = new AuditSecurityConfig();
        
        // When
        var jwtDecoder = config.jwtDecoder();
        
        // Then
        assertThat(jwtDecoder).isNotNull();
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
}