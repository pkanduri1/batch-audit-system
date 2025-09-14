package com.company.audit.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

/**
 * Test security configuration for unit and integration tests.
 * Provides mock JWT decoder and simplified security configuration for testing.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    /**
     * Test security filter chain that disables security for testing.
     * Used in integration tests where security is not the focus.
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .build();
    }

    /**
     * Mock JWT decoder for testing JWT-based authentication.
     * Returns a valid JWT with test claims for authentication testing.
     */
    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> createTestJwt();
    }

    /**
     * Creates a test JWT with standard claims for testing purposes.
     * Includes audit-specific roles for authorization testing.
     */
    private Jwt createTestJwt() {
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
                        "roles", java.util.List.of("AUDIT_VIEWER", "AUDIT_ADMIN"),
                        "audit_roles", java.util.List.of("AUDIT_VIEWER", "AUDIT_ADMIN"),
                        "groups", java.util.List.of("AUDIT_VIEWER", "AUDIT_ADMIN"),
                        "preferred_username", "test-user",
                        "email", "test-user@company.com"
                )
        );
    }
}