# Security Configuration Guide

## Overview

The Batch Audit System implements comprehensive API security using Spring Security 6.x with JWT-based authentication and role-based authorization. The system is compatible with Spring Boot 3.4+ and integrates with SpringDoc OpenAPI v2 for Swagger UI security.

## Security Features

### 1. JWT Authentication
- **JWT Token Validation**: Uses JWK Set URI for token validation
- **Claims Processing**: Extracts user information and roles from JWT claims
- **Stateless Authentication**: No server-side session management

### 2. Role-Based Authorization
- **AUDIT_VIEWER**: Read-only access to audit data and reports
- **AUDIT_ADMIN**: Full access including write operations
- **Endpoint Protection**: Different endpoints require different role levels

### 3. CORS Configuration
- **Cross-Origin Support**: Configurable allowed origins for web applications
- **Swagger Integration**: CORS enabled for API documentation access
- **Security Headers**: Comprehensive security headers including HSTS

## Configuration

### Environment Variables

```bash
# JWT Configuration
JWT_JWK_SET_URI=https://your-identity-provider.com/.well-known/jwks.json
JWT_ISSUER=https://your-identity-provider.com

# OAuth2 Configuration (for Swagger UI)
OAUTH2_AUTHORIZATION_URI=https://your-identity-provider.com/oauth2/authorize
OAUTH2_TOKEN_URI=https://your-identity-provider.com/oauth2/token
OAUTH2_CLIENT_ID=audit-system-client

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,https://your-frontend.com

# Security Features
SWAGGER_SECURITY_ENABLED=true
```

### Application Properties

```yaml
audit:
  security:
    jwt:
      jwk-set-uri: ${JWT_JWK_SET_URI:}
      issuer: ${JWT_ISSUER:}
    oauth2:
      authorization-uri: ${OAUTH2_AUTHORIZATION_URI:http://localhost:8080/oauth2/authorize}
      token-uri: ${OAUTH2_TOKEN_URI:http://localhost:8080/oauth2/token}
      client-id: ${OAUTH2_CLIENT_ID:audit-system-client}
    cors:
      allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:8080}
    swagger:
      enabled: ${SWAGGER_SECURITY_ENABLED:true}
```

## API Endpoints Security

### Public Endpoints
- `/actuator/health` - Health check endpoint
- `/actuator/info` - Application information
- `/api-docs/**` - OpenAPI documentation
- `/swagger-ui/**` - Swagger UI interface

### Protected Endpoints

#### Read Access (AUDIT_VIEWER or AUDIT_ADMIN)
- `GET /api/audit/events` - Retrieve audit events
- `GET /api/audit/reconciliation/{correlationId}` - Get reconciliation reports
- `GET /api/audit/statistics` - Audit statistics
- `GET /api/audit/discrepancies` - Data discrepancies

#### Write Access (AUDIT_ADMIN only)
- `POST /api/audit/**` - Create audit records
- `PUT /api/audit/**` - Update audit records
- `DELETE /api/audit/**` - Delete audit records

## JWT Token Format

### Required Claims
```json
{
  "sub": "user-id",
  "iss": "https://your-identity-provider.com",
  "aud": "audit-system",
  "exp": 1640995200,
  "iat": 1640991600,
  "audit_roles": ["AUDIT_VIEWER", "AUDIT_ADMIN"],
  "preferred_username": "john.doe",
  "email": "john.doe@company.com"
}
```

### Role Extraction
The system extracts roles from JWT claims in the following order:
1. `audit_roles` claim (preferred)
2. `groups` claim (fallback)

Only roles starting with `AUDIT_` are processed for authorization.

## Swagger UI Integration

### Authentication Methods
The Swagger UI supports two authentication methods:

1. **Bearer Token**: Manual JWT token entry
2. **OAuth2 Authorization Code Flow**: Interactive login

### Using Bearer Token
1. Obtain a JWT token from your identity provider
2. Click "Authorize" in Swagger UI
3. Enter `Bearer <your-jwt-token>` in the bearerAuth field

### Using OAuth2 Flow
1. Click "Authorize" in Swagger UI
2. Select oauth2 authentication
3. Complete the OAuth2 authorization flow
4. Swagger UI will automatically include the token in requests

## Testing Security

### Unit Tests
```java
@Test
void testJwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = auditSecurityConfig.jwtAuthenticationConverter();
    Jwt jwt = createTestJwt(List.of("AUDIT_VIEWER", "AUDIT_ADMIN"));
    
    Collection<? extends GrantedAuthority> authorities = converter.convert(jwt).getAuthorities();
    
    assertThat(authorities.stream().map(GrantedAuthority::getAuthority))
            .containsExactlyInAnyOrder("ROLE_AUDIT_VIEWER", "ROLE_AUDIT_ADMIN");
}
```

### Integration Tests
```java
@Test
@WithMockUser(roles = {"AUDIT_VIEWER"})
void testAuditViewerCanAccessReadEndpoints() throws Exception {
    mockMvc.perform(get("/api/audit/events"))
            .andExpect(status().isOk());
}
```

## Security Best Practices

### 1. Token Management
- Use short-lived JWT tokens (15-30 minutes)
- Implement token refresh mechanism
- Validate token issuer and audience

### 2. Role Assignment
- Follow principle of least privilege
- Regularly audit user roles and permissions
- Use groups/teams for role management

### 3. CORS Configuration
- Restrict allowed origins to known domains
- Avoid using wildcards in production
- Regularly review and update allowed origins

### 4. Monitoring
- Log authentication failures
- Monitor for unusual access patterns
- Set up alerts for security events

## Troubleshooting

### Common Issues

#### 401 Unauthorized
- Check JWT token validity and expiration
- Verify JWK Set URI is accessible
- Ensure token issuer matches configuration

#### 403 Forbidden
- Verify user has required roles (AUDIT_VIEWER or AUDIT_ADMIN)
- Check role claim format in JWT token
- Ensure roles start with "AUDIT_" prefix

#### CORS Errors
- Verify allowed origins configuration
- Check request headers and methods
- Ensure preflight requests are handled

### Debug Configuration
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.web.cors: DEBUG
```

## Production Deployment

### Security Checklist
- [ ] Configure proper JWK Set URI
- [ ] Set up HTTPS for all endpoints
- [ ] Configure appropriate CORS origins
- [ ] Enable security headers (HSTS, CSP)
- [ ] Set up monitoring and alerting
- [ ] Regular security audits and updates