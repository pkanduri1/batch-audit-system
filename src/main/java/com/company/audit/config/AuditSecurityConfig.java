package com.company.audit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Spring Security 6.x configuration for Batch Audit System REST APIs.
 * Provides JWT-based authentication and role-based authorization for audit endpoints.
 * Compatible with Spring Boot 3.4+ and SpringDoc OpenAPI v2 Swagger UI integration.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class AuditSecurityConfig {

    @Value("${audit.security.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${audit.security.jwt.issuer:}")
    private String jwtIssuer;

    @Value("${audit.security.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private List<String> allowedOrigins;

    @Value("${audit.security.swagger.enabled:true}")
    private boolean swaggerSecurityEnabled;

    /**
     * Main security filter chain for audit API endpoints.
     * Configures JWT authentication, role-based authorization, and CORS.
     */
    @Bean
    public SecurityFilterChain auditApiSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for stateless API
                .csrf(AbstractHttpConfigurer::disable)
                
                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                
                // Configure session management as stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Configure security headers
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .contentTypeOptions(contentTypeOptions -> {})
                        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(true))
                        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                
                // Configure authorization rules
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - health checks and API documentation
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Audit API endpoints with role-based access
                        .requestMatchers(HttpMethod.GET, "/api/audit/events/**").hasAnyRole("AUDIT_VIEWER", "AUDIT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/audit/reconciliation/**").hasAnyRole("AUDIT_VIEWER", "AUDIT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/audit/statistics/**").hasAnyRole("AUDIT_VIEWER", "AUDIT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/audit/discrepancies/**").hasAnyRole("AUDIT_VIEWER", "AUDIT_ADMIN")
                        
                        // Administrative endpoints require admin role
                        .requestMatchers(HttpMethod.POST, "/api/audit/**").hasRole("AUDIT_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/audit/**").hasRole("AUDIT_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/audit/**").hasRole("AUDIT_ADMIN")
                        
                        // All other audit API endpoints require authentication
                        .requestMatchers("/api/audit/**").authenticated()
                        
                        // All other requests require authentication
                        .anyRequest().authenticated())
                
                // Configure OAuth2 Resource Server with JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                
                .build();
    }

    /**
     * JWT decoder configuration for validating JWT tokens.
     * Uses JWK Set URI for token validation.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if (jwkSetUri != null && !jwkSetUri.isEmpty()) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        
        // For development/testing - use a mock decoder
        // In production, this should always use a real JWK Set URI
        return NimbusJwtDecoder.withJwkSetUri("http://localhost:8080/.well-known/jwks.json").build();
    }

    /**
     * JWT authentication converter that extracts roles from JWT claims.
     * Maps JWT authorities to Spring Security authorities with proper role prefixes.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract standard authorities
            Collection<? extends GrantedAuthority> standardAuthorities = authoritiesConverter.convert(jwt);
            
            // Extract custom audit roles from JWT claims
            Collection<SimpleGrantedAuthority> auditAuthorities = extractAuditRoles(jwt);
            
            // Combine both sets of authorities
            return Stream.concat(
                standardAuthorities != null ? standardAuthorities.stream() : Stream.empty(),
                auditAuthorities.stream()
            ).map(authority -> (GrantedAuthority) authority).toList();
        });

        return authenticationConverter;
    }

    /**
     * Extracts audit-specific roles from JWT claims.
     * Supports both 'audit_roles' and 'groups' claims for role extraction.
     */
    private Collection<SimpleGrantedAuthority> extractAuditRoles(org.springframework.security.oauth2.jwt.Jwt jwt) {
        // Try to extract from 'audit_roles' claim first
        List<String> auditRoles = jwt.getClaimAsStringList("audit_roles");
        if (auditRoles == null || auditRoles.isEmpty()) {
            // Fallback to 'groups' claim
            auditRoles = jwt.getClaimAsStringList("groups");
        }
        
        if (auditRoles == null) {
            return List.of();
        }
        
        return auditRoles.stream()
                .filter(role -> role.startsWith("AUDIT_"))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    /**
     * CORS configuration for cross-origin requests.
     * Allows configured origins to access audit APIs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/swagger-ui/**", configuration);
        source.registerCorsConfiguration("/api-docs/**", configuration);
        
        return source;
    }
}