# Technology Stack

## Core Framework
- **Spring Boot 3.4+**: Main application framework with auto-configuration
- **Spring JdbcTemplate**: Data access layer with Oracle database integration
- **Spring Web MVC**: REST API endpoints for dashboard and reporting
- **Spring Security**: Authentication and authorization for audit APIs
- **Spring Boot Actuator**: Health monitoring and metrics

## Database & Persistence
- **Oracle Database 19c/21c**: Primary audit data storage with optimized indexing
- **Spring JdbcTemplate**: Direct SQL operations with Oracle database integration
- **HikariCP**: High-performance connection pooling for Oracle
- **Liquibase**: Database schema migration and version control
- **UUID**: Primary keys for audit events ensuring uniqueness across systems

## Build System & Dependencies
- **Maven**: Build automation and dependency management
- **Java 17+**: Target runtime environment
- **Jackson**: JSON serialization for audit details and API responses
- **JUnit 5**: Unit and integration testing framework
- **Mockito**: Mocking framework for service layer testing

## Architecture Patterns
- **Layered Architecture**: Clear separation between presentation, service, repository, and entity layers
- **Repository Pattern**: Data access abstraction using Spring JdbcTemplate
- **Aspect-Oriented Programming (AOP)**: Automatic audit capture with @Auditable annotation
- **Event-Driven Architecture**: Spring Application Events for decoupled audit logging
- **Circuit Breaker Pattern**: Resilience for audit system failures

## Common Commands

### Build & Test
```bash
# Clean build with tests
mvn clean compile test

# Package application
mvn clean package

# Run application locally
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Database Operations
```bash
# Run database migrations with Liquibase
mvn liquibase:update

# Generate Liquibase changelog from existing database
mvn liquibase:generateChangeLog

# Validate Liquibase changelog
mvn liquibase:validate

# Rollback database changes
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Generate JPA entities from database
mvn hibernate3:hbm2java
```

### Testing
```bash
# Run unit tests only
mvn test

# Run integration tests
mvn verify

# Run specific test class
mvn test -Dtest=AuditServiceTest

# Generate test coverage report
mvn jacoco:report
```

## Configuration Management
- **application.yml**: Main configuration with environment-specific profiles
- **Oracle Connection**: Environment variables for database credentials
- **Audit Settings**: Configurable batch sizes, retention policies, and reconciliation schedules
- **Security**: JWT token validation and role-based access control