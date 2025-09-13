# Batch Audit System

A comprehensive end-to-end audit trail system for enterprise data processing pipelines built with Spring Boot and Oracle Database.

## Overview

The Batch Audit System provides complete traceability for data processing pipelines that ingest files from multiple mainframe source systems, process them through Oracle staging databases, apply business logic via Java modules, and generate final output files. The system uses Spring JdbcTemplate for direct SQL operations with Oracle database for optimal performance and control.

### Key Features

- **Multi-source system support** with distinct audit trails
- **Checkpoint-based logging** at critical pipeline transition points
- **REST API dashboard** for real-time monitoring and historical analysis
- **Automated reconciliation reports** with discrepancy detection
- **Oracle database persistence** with optimized indexing for audit queries
- **Liquibase schema management** for database version control
- **Spring Boot architecture** with comprehensive error handling and resilience

## Technology Stack

### Core Framework
- **Spring Boot 2.7+**: Main application framework with auto-configuration
- **Spring JdbcTemplate**: Data access layer with Oracle database integration
- **Spring Web MVC**: REST API endpoints for dashboard and reporting
- **Spring Security**: Authentication and authorization for audit APIs
- **Spring Boot Actuator**: Health monitoring and metrics

### Database & Persistence
- **Oracle Database 19c/21c**: Primary audit data storage with optimized indexing
- **Spring JdbcTemplate**: Direct SQL operations with Oracle database integration
- **HikariCP**: High-performance connection pooling for Oracle
- **Liquibase**: Database schema migration and version control
- **UUID**: Primary keys for audit events ensuring uniqueness across systems

### Build System & Dependencies
- **Maven**: Build automation and dependency management
- **Java 11+**: Target runtime environment
- **Jackson**: JSON serialization for audit details and API responses
- **JUnit 5**: Unit and integration testing framework
- **Mockito**: Mocking framework for service layer testing

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/company/audit/
│   │       ├── BatchAuditApplication.java          # Main Spring Boot application class
│   │       ├── config/                             # Configuration classes
│   │       ├── controller/                         # REST API controllers
│   │       ├── service/                            # Business logic layer
│   │       ├── repository/                         # Data access layer
│   │       ├── entity/                             # JPA entities
│   │       ├── model/                              # DTOs and data models
│   │       ├── enums/                              # Enumeration classes
│   │       └── exception/                          # Custom exceptions
│   └── resources/
│       ├── application.yml                         # Main configuration
│       ├── application-dev.yml                     # Development profile
│       ├── application-prod.yml                    # Production profile
│       └── db/changelog/                           # Liquibase database migration scripts
│           ├── db.changelog-master.xml             # Master changelog file
│           ├── 001-create-audit-table.xml          # Initial audit table creation
│           └── 002-create-audit-indexes.xml        # Audit table indexes
└── test/
    ├── java/
    │   └── com/company/audit/
    │       ├── service/                            # Service layer unit tests
    │       ├── repository/                         # Repository integration tests
    │       ├── controller/                         # REST API tests
    │       └── integration/                        # End-to-end integration tests
    └── resources/
        ├── application-test.yml                    # Test configuration
        └── test-data/                              # Test data files

docs/
├── implementation-guide.md                         # Comprehensive implementation guide
├── task-reference.md                               # Task breakdown and reference
├── database-setup.md                               # Database configuration guide
└── development-guidelines.md                       # Code generation principles and development standards
```

## Implementation Status

The project is currently in the initial development phase following a structured 45-task implementation plan:

### Current Status: Phase 1 - Foundation Setup
- ✅ **Task 1 Complete**: Basic Maven project structure with Spring Boot parent and core dependencies
- 🔄 **Next Tasks**: Oracle database dependencies, Spring Boot application class, and configuration setup

### Implementation Phases
- **Phase 1 (Tasks 1-15)**: Foundation - Maven setup, Oracle configuration, core entities *(1/15 complete)*
- **Phase 2 (Tasks 16-25)**: Data Layer - Models, repositories, database integration *(0/10 complete)*
- **Phase 3 (Tasks 26-33)**: Service Layer - Business logic and audit services *(0/8 complete)*
- **Phase 4 (Tasks 34-45)**: API & Integration - REST APIs, security, comprehensive testing *(0/12 complete)*

### What's Implemented
- Maven project structure with Spring Boot 2.7.18
- Core dependencies: Spring Boot Web, JDBC, Test starters
- SpringDoc OpenAPI for Swagger UI documentation
- Basic application-local.properties for Oracle connection

### Next Steps
1. Add Oracle JDBC driver and HikariCP dependencies (Task 2)
2. Create main Spring Boot application class (Task 3)
3. Configure Oracle database connection properties (Task 4)
4. Set up Liquibase for database schema management (Tasks 5-8)

**Documentation References**:
- Detailed task breakdown: `.kiro/specs/batch-audit-system/tasks.md`
- Development guidelines: `docs/development-guidelines.md`
- Current development status: `docs/development-status.md`
- Implementation guide: `docs/implementation-guide.md`

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+
- Oracle Database 19c/21c
- Access to mainframe source systems (for production use)

### Local Development Setup

**Note**: The project is in early development. Complete setup requires implementing Tasks 2-8 first.

1. **Clone the repository**
   ```bash
   git clone https://github.com/pkanduri1/batch-audit-system.git
   cd batch-audit-system
   ```

2. **Current State Verification**
   ```bash
   # Verify Maven structure
   mvn clean compile
   
   # Check current dependencies
   mvn dependency:tree
   ```

3. **Next Development Steps**
   
   To continue development, implement the following tasks in order:
   
   **Task 2**: Add Oracle dependencies to `pom.xml`:
   ```xml
   <!-- Oracle JDBC Driver -->
   <dependency>
       <groupId>com.oracle.database.jdbc</groupId>
       <artifactId>ojdbc11</artifactId>
       <scope>runtime</scope>
   </dependency>
   
   <!-- HikariCP Connection Pool -->
   <dependency>
       <groupId>com.zaxxer</groupId>
       <artifactId>HikariCP</artifactId>
   </dependency>
   
   <!-- Liquibase for Database Migration -->
   <dependency>
       <groupId>org.liquibase</groupId>
       <artifactId>liquibase-core</artifactId>
   </dependency>
   ```

4. **Future Configuration** (after Task 4)
   
   Oracle database configuration will be in `src/main/resources/application-local.properties`:
   ```properties
   # Oracle Database Configuration
   spring.datasource.url=jdbc:oracle:thin:@localhost:1521/ORCLPDB1
   spring.datasource.username=cm3int
   spring.datasource.password=${ORACLE_PASSWORD}
   spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
   
   # Liquibase Configuration (after Task 6)
   spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml
   spring.liquibase.contexts=local
   ```

5. **Full Setup** (available after Task 8)
   ```bash
   export ORACLE_PASSWORD=your_oracle_password
   mvn liquibase:update
   mvn clean compile test
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

## Database Schema Management

The project uses Liquibase for database schema version control and migrations.

### Key Liquibase Files

- **`db.changelog-master.xml`**: Master changelog that includes all migration files
- **`001-create-audit-table.xml`**: Creates the main PIPELINE_AUDIT_LOG table
- **`002-create-audit-indexes.xml`**: Creates optimized indexes for audit queries

### Common Liquibase Commands

```bash
# Run database migrations
mvn liquibase:update

# Generate changelog from existing database
mvn liquibase:generateChangeLog

# Validate changelog
mvn liquibase:validate

# Rollback changes
mvn liquibase:rollback -Dliquibase.rollbackCount=1

# Show pending changes
mvn liquibase:status
```

## Build and Test Commands

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

## API Endpoints

The system provides REST API endpoints for dashboard and reporting functionality:

### Audit Events
- `GET /api/audit/events` - Retrieve audit events with filtering and pagination
- `GET /api/audit/events/{auditId}` - Get specific audit event details

### Reconciliation Reports
- `GET /api/audit/reconciliation/{correlationId}` - Get reconciliation report for a pipeline run
- `GET /api/audit/reconciliation/reports` - List all reconciliation reports with filtering

### Statistics and Monitoring
- `GET /api/audit/statistics` - Get audit statistics and summary data
- `GET /api/audit/discrepancies` - Retrieve data discrepancies with filtering

## Configuration

### Application Properties

The system supports multiple configuration profiles:

- **`application.yml`**: Base configuration
- **`application-dev.yml`**: Development environment overrides
- **`application-prod.yml`**: Production environment overrides
- **`application-local.properties`**: Local development configuration

### Key Configuration Sections

```yaml
audit:
  database:
    batch-size: 100
    connection-pool-size: 10
  retention:
    days: 365
  reconciliation:
    auto-generate: true
    schedule: "0 0 6 * * ?"
  dashboard:
    page-size: 50
    max-export-records: 10000

spring:
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    contexts: ${spring.profiles.active}
    drop-first: false
    enabled: true
```

## Architecture Patterns

- **Layered Architecture**: Clear separation between presentation, service, repository, and entity layers with strict dependency flow
- **Repository Pattern**: Data access abstraction using Spring JdbcTemplate with Oracle optimization
- **Checkpoint-Based Logging**: Structured audit capture at critical pipeline transition points using @Auditable annotation
- **Correlation ID Management**: Thread-safe tracking of related audit events across pipeline stages
- **REST API Design**: Comprehensive dashboard and reporting endpoints with pagination and filtering
- **Aspect-Oriented Programming (AOP)**: Automatic audit capture with @Auditable annotation processing
- **Event-Driven Architecture**: Spring Application Events for decoupled audit logging
- **Circuit Breaker Pattern**: Resilience for audit system failures without breaking main business flow

## Development Workflow

### Task-Based Development
The project follows a structured 44-task implementation plan organized into four phases:

1. **Foundation Phase** (Tasks 1-15): Project structure, Maven setup, and core entities
2. **Data Layer Phase** (Tasks 16-25): Models, repositories, and database integration  
3. **Service Layer Phase** (Tasks 26-33): Business logic and audit services
4. **API & Integration Phase** (Tasks 34-44): REST APIs, security, and comprehensive testing

See `docs/implementation-guide.md` for detailed phase breakdown and implementation guidelines.

### Code Quality Standards
- Follow Spring Boot best practices with minimal implementation first approach
- Leverage Spring's dependency injection and auto-configuration
- Use @Component, @Service, @Repository annotations appropriately
- Implement proper exception handling with @ControllerAdvice
- Write comprehensive unit and integration tests (minimum 80% coverage)
- Use Liquibase for all database schema changes with proper Oracle-specific annotations
- Prefix audit-related classes with "Audit" and use UPPER_CASE for enum values
- Update documentation for any API or configuration changes

### Testing Strategy
```bash
# Run tests by phase
mvn test -Dtest="**/entity/**,**/repository/**"  # Data layer tests
mvn test -Dtest="**/service/**"                   # Service layer tests  
mvn test -Dtest="**/controller/**"                # API layer tests
mvn verify                                        # Full integration tests
```

## License

This project is proprietary software. All rights reserved.