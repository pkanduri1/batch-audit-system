# Development Guidelines

This document outlines the specific development guidelines and coding standards for the Batch Audit System, based on the Agent Orchestration principles defined for the project.

## Code Generation Principles

### Minimal Implementation First
When implementing new features or components:

- Generate only the essential code needed to fulfill requirements
- Avoid over-engineering or premature optimization
- Focus on core functionality before adding convenience features
- Use Spring Boot's auto-configuration wherever possible
- Implement the simplest solution that meets the requirement

### Spring Boot Best Practices
Follow these Spring Boot conventions throughout the codebase:

- Leverage Spring's dependency injection and auto-configuration
- Use `@Component`, `@Service`, `@Repository` annotations appropriately
- Implement proper exception handling with `@ControllerAdvice`
- Follow Spring Data JPA conventions for repository methods
- Use Spring Boot starters for common functionality

### Database Integration Standards
For Oracle database integration:

- Use JdbcTemplate with proper Oracle-specific SQL operations
- Implement Liquibase changesets for schema evolution
- Optimize queries with appropriate indexes on audit fields
- Use UUID for primary keys to ensure uniqueness across systems
- Follow Oracle naming conventions (UPPER_CASE for tables/columns)
- Use RowMapper classes for result set mapping to POJOs

## Architecture Patterns

### Layered Architecture Enforcement
Maintain strict separation of concerns:

- **Controllers**: Handle HTTP concerns only (validation, response formatting)
- **Services**: Contain business logic and orchestration
- **Repositories**: Handle data access with no business logic
- **Entities**: Represent database structure with minimal behavior

### Audit Event Processing
Implement consistent audit event handling:

- Use `@Auditable` annotation for automatic audit capture
- Implement correlation ID tracking across all audit events
- Ensure audit events are immutable once persisted
- Handle audit failures gracefully without breaking main business flow

### Error Handling Strategy
Implement robust error handling:

- Create specific exception types for different failure scenarios
- Use circuit breaker pattern for external system dependencies
- Log audit failures separately from business logic failures
- Provide meaningful error messages for troubleshooting

## Code Style Guidelines

### Naming Conventions
Follow these naming standards:

- Use descriptive method names that indicate audit purpose
- Prefix audit-related classes with "Audit" (AuditService, AuditEvent)
- Use enum values in UPPER_CASE for audit statuses and stages
- Name REST endpoints following RESTful conventions
- Use camelCase for Java variables and methods
- Use PascalCase for class names

### Configuration Management
Handle configuration consistently:

- Use environment-specific profiles (dev, test, prod)
- Externalize sensitive configuration via environment variables
- Group related properties under common prefixes
- Document configuration options with meaningful descriptions

### Testing Requirements
Maintain high testing standards:

- Write unit tests for all service layer methods
- Create integration tests for repository operations using @JdbcTest
- Mock external dependencies in service tests
- Use TestJdbcTemplate for repository testing
- Use test profiles to avoid impacting production data
- Achieve minimum 80% code coverage

## Implementation Priorities

Follow this order when implementing new features:

1. **Core Audit Entity**: Start with AuditEvent entity and repository
2. **Service Layer**: Implement basic audit capture and retrieval
3. **REST API**: Add dashboard endpoints for monitoring
4. **Database Schema**: Create optimized tables and indexes
5. **Integration**: Add AOP aspects for automatic audit capture
6. **Reporting**: Implement reconciliation and reporting features

## Common Patterns to Follow

### Audit Event Creation
Use this pattern for automatic audit capture:

```java
@Auditable(stage = CheckpointStage.DATA_VALIDATION)
public void validateData(String correlationId, DataSet data) {
    // Business logic here
    // Audit event will be automatically captured
}
```

### Repository Query Patterns
Follow Spring JdbcTemplate conventions:

```java
@Repository
public class AuditRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public List<AuditEvent> findByCorrelationIdAndSourceSystem(UUID correlationId, String sourceSystem) {
        String sql = "SELECT * FROM PIPELINE_AUDIT_LOG WHERE CORRELATION_ID = ? AND SOURCE_SYSTEM = ?";
        return jdbcTemplate.query(sql, auditEventRowMapper, correlationId, sourceSystem);
    }
    
    public List<AuditEvent> findByEventTimestampBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM PIPELINE_AUDIT_LOG WHERE EVENT_TIMESTAMP BETWEEN ? AND ?";
        return jdbcTemplate.query(sql, auditEventRowMapper, start, end);
    }
    
    public long countByCorrelationId(UUID correlationId) {
        String sql = "SELECT COUNT(*) FROM PIPELINE_AUDIT_LOG WHERE CORRELATION_ID = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, correlationId);
    }
}
```

### Exception Handling
Implement centralized exception handling:

```java
@ControllerAdvice
public class AuditExceptionHandler {
    
    @ExceptionHandler(AuditPersistenceException.class)
    public ResponseEntity<ErrorResponse> handleAuditFailure(AuditPersistenceException ex) {
        ErrorResponse error = new ErrorResponse(
            "AUDIT_PERSISTENCE_ERROR",
            "Failed to persist audit event: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(CorrelationIdException.class)
    public ResponseEntity<ErrorResponse> handleCorrelationIdError(CorrelationIdException ex) {
        ErrorResponse error = new ErrorResponse(
            "CORRELATION_ID_ERROR",
            "Correlation ID management error: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

### Service Layer Implementation
Follow this pattern for service classes:

```java
@Service
@Transactional
public class AuditServiceImpl implements AuditService {
    
    private final AuditRepository auditRepository;
    private final CorrelationIdManager correlationIdManager;
    
    public AuditServiceImpl(AuditRepository auditRepository, 
                           CorrelationIdManager correlationIdManager) {
        this.auditRepository = auditRepository;
        this.correlationIdManager = correlationIdManager;
    }
    
    @Override
    public void logCheckpoint(AuditEvent event) {
        try {
            // Set correlation ID if not present
            if (event.getCorrelationId() == null) {
                event.setCorrelationId(correlationIdManager.getCurrentCorrelationId());
            }
            
            auditRepository.save(event);
        } catch (Exception e) {
            throw new AuditPersistenceException("Failed to log checkpoint", e);
        }
    }
}
```

### Repository Layer Implementation
Follow this pattern for repository classes using JdbcTemplate:

```java
@Repository
public class AuditRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<AuditEvent> auditEventRowMapper;
    
    public AuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditEventRowMapper = new AuditEventRowMapper();
    }
    
    public void save(AuditEvent event) {
        String sql = """
            INSERT INTO PIPELINE_AUDIT_LOG 
            (AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME, PROCESS_NAME, 
             SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER, CHECKPOINT_STAGE, 
             EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        jdbcTemplate.update(sql,
            event.getAuditId(),
            event.getCorrelationId(),
            event.getSourceSystem(),
            event.getModuleName(),
            event.getProcessName(),
            event.getSourceEntity(),
            event.getDestinationEntity(),
            event.getKeyIdentifier(),
            event.getCheckpointStage(),
            event.getEventTimestamp(),
            event.getStatus().name(),
            event.getMessage(),
            event.getDetailsJson()
        );
    }
    
    public List<AuditEvent> findByCorrelationId(UUID correlationId) {
        String sql = "SELECT * FROM PIPELINE_AUDIT_LOG WHERE CORRELATION_ID = ? ORDER BY EVENT_TIMESTAMP";
        return jdbcTemplate.query(sql, auditEventRowMapper, correlationId);
    }
}
```

### Configuration Class Pattern
Use this pattern for configuration classes:

```java
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
public class AuditDatabaseConfig {
    
    @Bean
    @ConfigurationProperties("audit.datasource")
    public DataSource auditDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean auditEntityManagerFactory(
            @Qualifier("auditDataSource") DataSource dataSource) {
        // Configure entity manager factory
    }
}
```

## Code Review Checklist

Before submitting code for review, ensure:

- [ ] Follows minimal implementation first principle
- [ ] Uses appropriate Spring annotations
- [ ] Implements proper error handling
- [ ] Includes comprehensive unit tests
- [ ] Follows naming conventions
- [ ] Uses environment-specific configuration
- [ ] Maintains layered architecture separation
- [ ] Includes meaningful JavaDoc comments
- [ ] Handles audit failures gracefully
- [ ] Uses correlation ID tracking where applicable

## Performance Considerations

### Database Optimization
- Use batch processing for bulk audit operations
- Implement appropriate indexes for common query patterns
- Use connection pooling with HikariCP
- Monitor query performance and optimize as needed

### Memory Management
- Clean up correlation IDs in ThreadLocal storage
- Use pagination for large result sets
- Implement caching for frequently accessed data
- Monitor memory usage and garbage collection

### Async Processing
- Use @Async for non-critical audit operations
- Implement circuit breaker for external dependencies
- Handle backpressure in high-volume scenarios
- Monitor thread pool usage and adjust as needed

## Security Guidelines

### Data Protection
- Never log sensitive data in audit events
- Use environment variables for database credentials
- Implement role-based access control for APIs
- Encrypt sensitive audit details at rest

### API Security
- Validate all input parameters
- Implement rate limiting for API endpoints
- Use JWT tokens for authentication
- Log security-related events separately

This document should be referenced throughout the development process to ensure consistency and quality across the Batch Audit System implementation.