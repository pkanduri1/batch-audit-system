# Batch Audit System Integration Guide

## Overview

This guide provides comprehensive instructions for integrating the Batch Audit System into existing data processing pipelines. The system provides end-to-end audit logging capabilities for tracking data lineage from mainframe source systems through Oracle staging databases, Java module transformations, and final output file generation.

## Table of Contents

1. [Integration Architecture](#integration-architecture)
2. [Prerequisites](#prerequisites)
3. [Integration Patterns](#integration-patterns)
4. [Checkpoint Implementation](#checkpoint-implementation)
5. [Code Examples](#code-examples)
6. [Configuration](#configuration)
7. [Error Handling](#error-handling)
8. [Performance Considerations](#performance-considerations)
9. [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)
10. [Best Practices](#best-practices)

## Integration Architecture

### System Components

The Batch Audit System consists of:

- **AuditService**: Core service for logging audit events
- **CorrelationIdManager**: Thread-safe correlation ID management
- **AuditRepository**: Oracle database persistence layer
- **REST API**: Dashboard and reporting endpoints
- **Database Schema**: Optimized Oracle tables and indexes

### Integration Points

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Checkpoint 1  │    │   Checkpoint 2  │    │   Checkpoint 3  │    │   Checkpoint 4  │
│  File Transfer  │───▶│  SQL*Loader     │───▶│ Business Rules  │───▶│ File Generation │
│   (RHEL_LANDING)│    │(SQLLOADER_START)│    │ (LOGIC_APPLIED) │    │(FILE_GENERATED) │
└─────────────────┘    │(SQLLOADER_COMP) │    └─────────────────┘    └─────────────────┘
                       └─────────────────┘
```

## Prerequisites

### Dependencies

Add the audit system as a dependency to your existing project:

```xml
<dependency>
    <groupId>com.company</groupId>
    <artifactId>batch-audit-system</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Database Setup

1. **Oracle Database**: Ensure Oracle 19c/21c is available
2. **Schema Access**: Grant necessary permissions to the audit schema
3. **Liquibase**: Run database migrations to create audit tables

```bash
# Run Liquibase migrations
mvn liquibase:update -Dspring.profiles.active=your-environment
```

### Configuration

Add audit system configuration to your `application.yml`:

```yaml
# Audit System Configuration
audit:
  database:
    url: jdbc:oracle:thin:@localhost:1521:ORCLPDB1
    username: ${AUDIT_DB_USERNAME:audit_user}
    password: ${AUDIT_DB_PASSWORD:audit_password}
    schema: ${AUDIT_DB_SCHEMA:AUDIT_SCHEMA}
  
  # Connection pool settings
  hikari:
    maximum-pool-size: 20
    minimum-idle: 5
    connection-timeout: 30000
    idle-timeout: 600000
    max-lifetime: 1800000
  
  # Retry configuration
  retry:
    max-attempts: 3
    backoff-delay: 1000
    max-delay: 5000
  
  # Performance settings
  batch-size: 100
  async-processing: true
```

## Integration Patterns

### Pattern 1: Service Injection

Inject the audit service into your existing components:

```java
@Service
public class YourExistingService {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    public YourExistingService(AuditService auditService, 
                              CorrelationIdManager correlationIdManager) {
        this.auditService = auditService;
        this.correlationIdManager = correlationIdManager;
    }
    
    public void processData() {
        // Generate correlation ID for this pipeline run
        UUID correlationId = correlationIdManager.generateCorrelationId();
        
        try {
            // Your existing business logic
            performDataProcessing();
            
            // Log successful completion
            auditService.logBusinessRuleApplication(
                correlationId,
                "YOUR_SOURCE_SYSTEM",
                "YourModule",
                "processData",
                "input_data",
                "processed_data",
                "business_key_123",
                AuditStatus.SUCCESS,
                "Data processing completed successfully",
                buildAuditDetails()
            );
            
        } catch (Exception e) {
            // Log failure
            auditService.logBusinessRuleApplication(
                correlationId,
                "YOUR_SOURCE_SYSTEM",
                "YourModule",
                "processData",
                "input_data",
                "processed_data",
                "business_key_123",
                AuditStatus.FAILURE,
                "Data processing failed: " + e.getMessage(),
                buildErrorAuditDetails(e)
            );
            throw e;
        } finally {
            // Clean up correlation ID
            correlationIdManager.clearCorrelationId();
        }
    }
}
```

### Pattern 2: Aspect-Oriented Programming (AOP)

Use AOP for automatic audit logging:

```java
@Aspect
@Component
public class YourAuditAspect {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        UUID correlationId = correlationIdManager.getCurrentCorrelationId();
        if (correlationId == null) {
            correlationId = correlationIdManager.generateCorrelationId();
        }
        
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        try {
            Object result = joinPoint.proceed();
            
            // Log success
            auditService.logBusinessRuleApplication(
                correlationId,
                auditable.sourceSystem(),
                className,
                methodName,
                auditable.sourceEntity(),
                auditable.destinationEntity(),
                extractKeyIdentifier(joinPoint.getArgs()),
                AuditStatus.SUCCESS,
                "Method executed successfully",
                buildMethodAuditDetails(joinPoint.getArgs(), result)
            );
            
            return result;
            
        } catch (Exception e) {
            // Log failure
            auditService.logBusinessRuleApplication(
                correlationId,
                auditable.sourceSystem(),
                className,
                methodName,
                auditable.sourceEntity(),
                auditable.destinationEntity(),
                extractKeyIdentifier(joinPoint.getArgs()),
                AuditStatus.FAILURE,
                "Method execution failed: " + e.getMessage(),
                buildErrorAuditDetails(e)
            );
            throw e;
        }
    }
}

// Custom annotation for audit-enabled methods
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String sourceSystem();
    String sourceEntity() default "";
    String destinationEntity() default "";
    CheckpointStage stage() default CheckpointStage.LOGIC_APPLIED;
}
```

### Pattern 3: Event-Driven Integration

Use Spring Application Events for decoupled audit logging:

```java
@Component
public class AuditEventListener {
    
    private final AuditService auditService;
    
    @EventListener
    public void handleFileTransferEvent(FileTransferEvent event) {
        auditService.logFileTransfer(
            event.getCorrelationId(),
            event.getSourceSystem(),
            event.getFileName(),
            event.getProcessName(),
            event.getSourcePath(),
            event.getDestinationPath(),
            event.getFileHash(),
            event.getStatus(),
            event.getMessage(),
            event.getAuditDetails()
        );
    }
    
    @EventListener
    public void handleBusinessRuleEvent(BusinessRuleEvent event) {
        auditService.logBusinessRuleApplication(
            event.getCorrelationId(),
            event.getSourceSystem(),
            event.getModuleName(),
            event.getProcessName(),
            event.getSourceEntity(),
            event.getDestinationEntity(),
            event.getKeyIdentifier(),
            event.getStatus(),
            event.getMessage(),
            event.getAuditDetails()
        );
    }
}

// Publish events from your existing code
@Service
public class YourExistingService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public void processFile(String fileName) {
        try {
            // Your file processing logic
            processFileInternal(fileName);
            
            // Publish success event
            eventPublisher.publishEvent(new FileTransferEvent(
                correlationId, "YOUR_SYSTEM", fileName, AuditStatus.SUCCESS
            ));
            
        } catch (Exception e) {
            // Publish failure event
            eventPublisher.publishEvent(new FileTransferEvent(
                correlationId, "YOUR_SYSTEM", fileName, AuditStatus.FAILURE
            ));
            throw e;
        }
    }
}
```

## Checkpoint Implementation

### Checkpoint 1: File Transfer (RHEL_LANDING)

Integrate file transfer audit logging:

```java
@Service
public class FileTransferService {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    public void transferFile(String sourceSystem, String fileName, 
                           String sourcePath, String destinationPath) {
        UUID correlationId = correlationIdManager.generateCorrelationId();
        
        try {
            // Perform file transfer
            FileTransferResult result = performFileTransfer(sourcePath, destinationPath);
            
            // Build audit details
            AuditDetails auditDetails = AuditDetails.builder()
                .fileSize(result.getFileSize())
                .fileHash(result.getFileHash())
                .transferStartTime(result.getStartTime())
                .transferEndTime(result.getEndTime())
                .transferDurationMs(result.getDurationMs())
                .build();
            
            // Log successful transfer
            auditService.logFileTransfer(
                correlationId,
                sourceSystem,
                fileName,
                "FileTransferService.transferFile",
                sourcePath,
                destinationPath,
                result.getFileHash(), // Use file hash as key identifier
                AuditStatus.SUCCESS,
                String.format("File transferred successfully: %d bytes in %d ms", 
                    result.getFileSize(), result.getDurationMs()),
                auditDetails
            );
            
        } catch (Exception e) {
            // Log failed transfer
            auditService.logFileTransfer(
                correlationId,
                sourceSystem,
                fileName,
                "FileTransferService.transferFile",
                sourcePath,
                destinationPath,
                fileName, // Use filename as fallback key identifier
                AuditStatus.FAILURE,
                "File transfer failed: " + e.getMessage(),
                AuditDetails.builder()
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .build()
            );
            throw e;
        }
    }
}
```

### Checkpoint 2: SQL*Loader Operations

Integrate SQL*Loader audit logging:

```java
@Service
public class DataLoadService {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    public void loadDataToOracle(String sourceSystem, String fileName, String tableName) {
        UUID correlationId = correlationIdManager.getCurrentCorrelationId();
        if (correlationId == null) {
            correlationId = correlationIdManager.generateCorrelationId();
        }
        
        // Log SQL*Loader start
        auditService.logSqlLoaderOperation(
            correlationId,
            sourceSystem,
            tableName,
            "DataLoadService.loadDataToOracle.start",
            fileName,
            tableName,
            fileName,
            AuditStatus.SUCCESS,
            "SQL*Loader operation started",
            AuditDetails.builder()
                .sqlLoaderStartTime(LocalDateTime.now())
                .build()
        );
        
        try {
            // Perform SQL*Loader operation
            SqlLoaderResult result = executeSqlLoader(fileName, tableName);
            
            // Build detailed audit information
            AuditDetails auditDetails = AuditDetails.builder()
                .rowsRead(result.getRowsRead())
                .rowsLoaded(result.getRowsLoaded())
                .rowsRejected(result.getRowsRejected())
                .sqlLoaderStartTime(result.getStartTime())
                .sqlLoaderEndTime(result.getEndTime())
                .sqlLoaderDurationMs(result.getDurationMs())
                .controlTotalAmount(result.getControlTotal())
                .build();
            
            // Determine status based on results
            AuditStatus status = result.getRowsRejected() > 0 ? AuditStatus.WARNING : AuditStatus.SUCCESS;
            
            // Log SQL*Loader completion
            auditService.logSqlLoaderOperation(
                correlationId,
                sourceSystem,
                tableName,
                "DataLoadService.loadDataToOracle.complete",
                fileName,
                tableName,
                fileName,
                status,
                String.format("SQL*Loader completed: %d rows loaded, %d rejected", 
                    result.getRowsLoaded(), result.getRowsRejected()),
                auditDetails
            );
            
        } catch (Exception e) {
            // Log SQL*Loader failure
            auditService.logSqlLoaderOperation(
                correlationId,
                sourceSystem,
                tableName,
                "DataLoadService.loadDataToOracle.error",
                fileName,
                tableName,
                fileName,
                AuditStatus.FAILURE,
                "SQL*Loader operation failed: " + e.getMessage(),
                AuditDetails.builder()
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .sqlLoaderEndTime(LocalDateTime.now())
                    .build()
            );
            throw e;
        }
    }
}
```

### Checkpoint 3: Business Rule Application

Integrate business rule audit logging:

```java
@Service
public class BusinessRuleProcessor {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    @Auditable(sourceSystem = "MAINFRAME_A", stage = CheckpointStage.LOGIC_APPLIED)
    public ProcessingResult applyBusinessRules(DataRecord inputRecord) {
        UUID correlationId = correlationIdManager.getCurrentCorrelationId();
        
        try {
            // Apply business rules
            ProcessingResult result = processBusinessLogic(inputRecord);
            
            // Build audit details with rule-specific information
            AuditDetails auditDetails = AuditDetails.builder()
                .businessRuleInputData(serializeInputData(inputRecord))
                .businessRuleOutputData(serializeOutputData(result))
                .recordsProcessed(1L)
                .processingStartTime(result.getStartTime())
                .processingEndTime(result.getEndTime())
                .businessRulesApplied(result.getRulesApplied())
                .build();
            
            // Determine status based on validation results
            AuditStatus status = result.hasValidationWarnings() ? AuditStatus.WARNING : AuditStatus.SUCCESS;
            
            auditService.logBusinessRuleApplication(
                correlationId,
                "MAINFRAME_A",
                "BusinessRuleProcessor",
                "applyBusinessRules",
                "input_record_" + inputRecord.getId(),
                "processed_record_" + result.getId(),
                inputRecord.getBusinessKey(),
                status,
                String.format("Business rules applied: %s", result.getSummary()),
                auditDetails
            );
            
            return result;
            
        } catch (BusinessRuleException e) {
            // Log business rule failure
            auditService.logBusinessRuleApplication(
                correlationId,
                "MAINFRAME_A",
                "BusinessRuleProcessor",
                "applyBusinessRules",
                "input_record_" + inputRecord.getId(),
                null,
                inputRecord.getBusinessKey(),
                AuditStatus.FAILURE,
                "Business rule validation failed: " + e.getMessage(),
                AuditDetails.builder()
                    .businessRuleInputData(serializeInputData(inputRecord))
                    .errorMessage(e.getMessage())
                    .validationErrors(e.getValidationErrors())
                    .build()
            );
            throw e;
        }
    }
}
```

### Checkpoint 4: File Generation

Integrate file generation audit logging:

```java
@Service
public class FileGenerationService {
    
    private final AuditService auditService;
    private final CorrelationIdManager correlationIdManager;
    
    public void generateOutputFile(String sourceSystem, String outputFileName, 
                                 List<ProcessedRecord> records) {
        UUID correlationId = correlationIdManager.getCurrentCorrelationId();
        
        try {
            // Generate output file
            FileGenerationResult result = createOutputFile(outputFileName, records);
            
            // Build comprehensive audit details
            AuditDetails auditDetails = AuditDetails.builder()
                .recordsProcessed((long) records.size())
                .fileSize(result.getFileSize())
                .fileHash(result.getFileHash())
                .outputRecordCount(result.getRecordCount())
                .controlTotalAmount(result.getControlTotal())
                .fileGenerationStartTime(result.getStartTime())
                .fileGenerationEndTime(result.getEndTime())
                .build();
            
            auditService.logFileGeneration(
                correlationId,
                sourceSystem,
                outputFileName,
                "FileGenerationService.generateOutputFile",
                "processed_records",
                result.getFilePath(),
                result.getFileHash(),
                AuditStatus.SUCCESS,
                String.format("Output file generated: %d records, %d bytes", 
                    result.getRecordCount(), result.getFileSize()),
                auditDetails
            );
            
        } catch (Exception e) {
            // Log file generation failure
            auditService.logFileGeneration(
                correlationId,
                sourceSystem,
                outputFileName,
                "FileGenerationService.generateOutputFile",
                "processed_records",
                null,
                null,
                AuditStatus.FAILURE,
                "File generation failed: " + e.getMessage(),
                AuditDetails.builder()
                    .recordsProcessed((long) records.size())
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .build()
            );
            throw e;
        }
    }
}
```

## Configuration

### Spring Boot Configuration

```java
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
public class AuditIntegrationConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public AuditService auditService(AuditRepository auditRepository, ObjectMapper objectMapper) {
        return new AuditServiceImpl(auditRepository, objectMapper);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdManager correlationIdManager() {
        return new CorrelationIdManagerImpl();
    }
    
    @Bean
    public AuditEventListener auditEventListener(AuditService auditService) {
        return new AuditEventListener(auditService);
    }
}

@ConfigurationProperties(prefix = "audit")
@Data
public class AuditProperties {
    private Database database = new Database();
    private Retry retry = new Retry();
    private boolean asyncProcessing = true;
    private int batchSize = 100;
    
    @Data
    public static class Database {
        private String url;
        private String username;
        private String password;
        private String schema;
    }
    
    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private long backoffDelay = 1000;
        private long maxDelay = 5000;
    }
}
```

### Environment-Specific Configuration

```yaml
# application-dev.yml
audit:
  database:
    url: jdbc:oracle:thin:@dev-oracle:1521:DEVDB
    username: audit_dev
    password: ${AUDIT_DEV_PASSWORD}
  async-processing: false
  batch-size: 10

# application-prod.yml
audit:
  database:
    url: jdbc:oracle:thin:@prod-oracle:1521:PRODDB
    username: audit_prod
    password: ${AUDIT_PROD_PASSWORD}
  async-processing: true
  batch-size: 500
  hikari:
    maximum-pool-size: 50
    minimum-idle: 10
```

## Error Handling

### Graceful Degradation

Implement graceful degradation when audit system is unavailable:

```java
@Service
public class ResilientAuditService {
    
    private final AuditService auditService;
    private final CircuitBreaker circuitBreaker;
    
    public void logAuditEventSafely(AuditEvent auditEvent) {
        try {
            circuitBreaker.executeSupplier(() -> {
                auditService.logAuditEvent(auditEvent);
                return null;
            });
        } catch (Exception e) {
            // Log to alternative system or queue for retry
            logToFallbackSystem(auditEvent, e);
        }
    }
    
    private void logToFallbackSystem(AuditEvent auditEvent, Exception error) {
        // Option 1: Log to file system
        writeToAuditFile(auditEvent);
        
        // Option 2: Send to message queue for later processing
        sendToRetryQueue(auditEvent);
        
        // Option 3: Log to application logs
        logger.warn("Audit system unavailable, event logged locally: {}", auditEvent, error);
    }
}
```

### Retry Configuration

```java
@Configuration
public class AuditRetryConfig {
    
    @Bean
    @Retryable(
        value = {AuditPersistenceException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public RetryTemplate auditRetryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(AuditPersistenceException.class)
            .build();
    }
}
```

## Performance Considerations

### Asynchronous Processing

```java
@Service
public class AsyncAuditService {
    
    private final AuditService auditService;
    private final TaskExecutor auditTaskExecutor;
    
    @Async("auditTaskExecutor")
    public CompletableFuture<Void> logAuditEventAsync(AuditEvent auditEvent) {
        try {
            auditService.logAuditEvent(auditEvent);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Bean("auditTaskExecutor")
    public TaskExecutor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("audit-");
        executor.initialize();
        return executor;
    }
}
```

### Batch Processing

```java
@Service
public class BatchAuditService {
    
    private final AuditRepository auditRepository;
    private final List<AuditEvent> auditEventBuffer = new ArrayList<>();
    private final Object bufferLock = new Object();
    
    @Scheduled(fixedDelay = 5000) // Flush every 5 seconds
    public void flushAuditEvents() {
        List<AuditEvent> eventsToFlush;
        synchronized (bufferLock) {
            if (auditEventBuffer.isEmpty()) {
                return;
            }
            eventsToFlush = new ArrayList<>(auditEventBuffer);
            auditEventBuffer.clear();
        }
        
        try {
            auditRepository.saveAll(eventsToFlush);
        } catch (Exception e) {
            // Re-add events to buffer for retry
            synchronized (bufferLock) {
                auditEventBuffer.addAll(0, eventsToFlush);
            }
            throw e;
        }
    }
    
    public void bufferAuditEvent(AuditEvent auditEvent) {
        synchronized (bufferLock) {
            auditEventBuffer.add(auditEvent);
            if (auditEventBuffer.size() >= 100) { // Flush when buffer is full
                flushAuditEvents();
            }
        }
    }
}
```

## Monitoring and Troubleshooting

### Health Checks

```java
@Component
public class AuditSystemHealthIndicator implements HealthIndicator {
    
    private final AuditRepository auditRepository;
    
    @Override
    public Health health() {
        try {
            // Test database connectivity
            auditRepository.findById(UUID.randomUUID());
            return Health.up()
                .withDetail("database", "connected")
                .withDetail("timestamp", LocalDateTime.now())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "disconnected")
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", LocalDateTime.now())
                .build();
        }
    }
}
```

### Metrics

```java
@Component
public class AuditMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter auditEventsCounter;
    private final Timer auditProcessingTimer;
    
    public AuditMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.auditEventsCounter = Counter.builder("audit.events.total")
            .description("Total number of audit events processed")
            .register(meterRegistry);
        this.auditProcessingTimer = Timer.builder("audit.processing.duration")
            .description("Time taken to process audit events")
            .register(meterRegistry);
    }
    
    public void recordAuditEvent(AuditStatus status, CheckpointStage stage) {
        auditEventsCounter.increment(
            Tags.of(
                Tag.of("status", status.name()),
                Tag.of("stage", stage.name())
            )
        );
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
}
```

## Best Practices

### 1. Correlation ID Management

- Always generate a correlation ID at the start of each pipeline run
- Propagate correlation IDs across all system boundaries
- Clear correlation IDs when processing completes to prevent memory leaks

### 2. Error Handling

- Never let audit failures break your main business logic
- Implement circuit breakers for audit system resilience
- Use fallback mechanisms when audit system is unavailable

### 3. Performance Optimization

- Use asynchronous processing for non-critical audit events
- Implement batch processing for high-volume scenarios
- Configure appropriate connection pool sizes

### 4. Data Quality

- Include meaningful business keys in audit events
- Capture sufficient detail in AuditDetails for troubleshooting
- Use consistent naming conventions across all systems

### 5. Security

- Encrypt sensitive data in audit details
- Implement proper access controls for audit data
- Follow data retention policies

### 6. Testing

- Include audit verification in your integration tests
- Test audit system failure scenarios
- Validate correlation ID propagation

## Example Integration Test

```java
@SpringBootTest
@TestPropertySource(properties = {
    "audit.database.url=jdbc:h2:mem:testdb",
    "audit.async-processing=false"
})
class AuditIntegrationTest {
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private CorrelationIdManager correlationIdManager;
    
    @Autowired
    private YourExistingService yourService;
    
    @Test
    void shouldCreateCompleteAuditTrail() {
        // Given
        UUID correlationId = correlationIdManager.generateCorrelationId();
        
        // When
        yourService.processCompleteWorkflow(correlationId);
        
        // Then
        List<AuditEvent> auditTrail = auditService.getAuditTrail(correlationId);
        
        assertThat(auditTrail).hasSize(4); // One for each checkpoint
        assertThat(auditTrail).extracting(AuditEvent::getCheckpointStage)
            .containsExactly(
                CheckpointStage.RHEL_LANDING,
                CheckpointStage.SQLLOADER_START,
                CheckpointStage.SQLLOADER_COMPLETE,
                CheckpointStage.LOGIC_APPLIED,
                CheckpointStage.FILE_GENERATED
            );
        
        // Verify all events have the same correlation ID
        assertThat(auditTrail).allMatch(event -> 
            event.getCorrelationId().equals(correlationId));
    }
}
```

This integration guide provides a comprehensive approach to incorporating the Batch Audit System into existing data processing pipelines, ensuring complete end-to-end audit logging with minimal impact on existing business logic.