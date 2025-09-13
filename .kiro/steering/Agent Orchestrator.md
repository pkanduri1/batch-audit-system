---
inclusion: always
---

# Agent Orchestration Guidelines

## Code Generation Principles

### Minimal Implementation First
- Generate only the essential code needed to fulfill requirements
- Avoid over-engineering or premature optimization
- Focus on core functionality before adding convenience features
- Use Spring Boot's auto-configuration wherever possible

### Spring Boot Best Practices
- Leverage Spring's dependency injection and auto-configuration
- Use `@Component`, `@Service`, `@Repository` annotations appropriately
- Implement proper exception handling with `@ControllerAdvice`
- Follow Spring JdbcTemplate conventions for repository methods

### Database Integration
- Use JdbcTemplate with proper Oracle-specific SQL operations
- Implement Liquibase changesets for schema evolution
- Optimize queries with appropriate indexes on audit fields
- Use UUID for primary keys to ensure uniqueness across systems

## Architecture Patterns

### Layered Architecture Enforcement
- Controllers handle HTTP concerns only (validation, response formatting)
- Services contain business logic and orchestration
- Repositories handle data access with no business logic
- Entities represent database structure with minimal behavior

### Audit Event Processing
- Use `@Auditable` annotation for automatic audit capture
- Implement correlation ID tracking across all audit events
- Ensure audit events are immutable once persisted
- Handle audit failures gracefully without breaking main business flow

### Error Handling Strategy
- Create specific exception types for different failure scenarios
- Use circuit breaker pattern for external system dependencies
- Log audit failures separately from business logic failures
- Provide meaningful error messages for troubleshooting

## Code Style Guidelines

### Naming Conventions
- Use descriptive method names that indicate audit purpose
- Prefix audit-related classes with "Audit" (AuditService, AuditEvent)
- Use enum values in UPPER_CASE for audit statuses and stages
- Name REST endpoints following RESTful conventions

### Configuration Management
- Use environment-specific profiles (dev, test, prod)
- Externalize sensitive configuration via environment variables
- Group related properties under common prefixes
- Document configuration options with meaningful descriptions

### Testing Requirements
- Write unit tests for all service layer methods
- Create integration tests for repository operations
- Mock external dependencies in service tests
- Use test profiles to avoid impacting production data

## Implementation Priorities

1. **Core Audit Entity**: Start with AuditEvent entity and repository
2. **Service Layer**: Implement basic audit capture and retrieval
3. **REST API**: Add dashboard endpoints for monitoring
4. **Database Schema**: Create optimized tables and indexes
5. **Integration**: Add AOP aspects for automatic audit capture
6. **Reporting**: Implement reconciliation and reporting features

## Common Patterns to Follow

### Audit Event Creation
```java
@Auditable(stage = CheckpointStage.DATA_VALIDATION)
public void validateData(String correlationId, DataSet data) {
    // Business logic here
}
```

### Repository Query Patterns
```java
@Repository
public class AuditRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public List<AuditEvent> findByCorrelationIdAndSourceSystem(String correlationId, String sourceSystem) {
        String sql = "SELECT * FROM PIPELINE_AUDIT_LOG WHERE CORRELATION_ID = ? AND SOURCE_SYSTEM = ?";
        return jdbcTemplate.query(sql, auditEventRowMapper, correlationId, sourceSystem);
    }
}
```

### Exception Handling
```java
@ControllerAdvice
public class AuditExceptionHandler {
    @ExceptionHandler(AuditPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleAuditFailure(AuditPersistenceException ex) {
        // Handle gracefully
    }
}
```