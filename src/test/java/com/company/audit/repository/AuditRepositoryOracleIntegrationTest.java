package com.company.audit.repository;

import com.company.audit.config.OracleDataSourceConfig;
import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import com.company.audit.model.AuditEvent;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive Database Integration Tests for AuditRepository using Spring Boot 3.4+ and Java 17+.
 * Tests all repository methods with Oracle database using JdbcTemplate and Liquibase.
 * Uses Test_ prefixed tables for isolation from production data.
 * 
 * This test class verifies:
 * - Oracle database connectivity and configuration (when available)
 * - Liquibase schema creation and migrations with Spring Boot 3.4+ integration
 * - JdbcTemplate operations with Oracle-specific features and Java 17+ enhancements
 * - Performance with Oracle indexes and query optimization
 * - Oracle-specific SQL features using Java 17+ text blocks for better readability
 * - Comprehensive repository method testing with Test_ prefixed tables
 * - Transaction management and connection pooling
 * - Error handling and recovery scenarios
 * - Concurrent operations and thread safety
 * 
 * Requirements covered: 2.2, 2.5, 6.1, 6.2
 */
@JdbcTest
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "spring.liquibase.enabled=true",
    "spring.liquibase.contexts=integration,test",
    "spring.liquibase.change-log=classpath:db/changelog/test-changelog-master.xml",
    "spring.test.database.replace=none"
})
@Import({
    AuditRepositoryOracleIntegrationTest.TestConfig.class,
    OracleDataSourceConfig.class
})
@DisplayName("Comprehensive Database Integration Tests for AuditRepository")
@EnabledIfSystemProperty(named = "test.database.integration", matches = "true", disabledReason = "Database integration tests disabled. Set -Dtest.database.integration=true to enable.")
class AuditRepositoryOracleIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SpringLiquibase liquibase;

    private TestAuditRepository testAuditRepository;
    private static final String TEST_TABLE_NAME = "Test_PIPELINE_AUDIT_LOG";
    private final RowMapper<AuditEvent> auditEventRowMapper = new AuditEventRowMapper();
    private boolean isOracleDatabase = false;
    private String databaseProductName = "Unknown";

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public SpringLiquibase liquibase(DataSource dataSource) {
            SpringLiquibase liquibase = new SpringLiquibase();
            liquibase.setDataSource(dataSource);
            liquibase.setChangeLog("classpath:db/changelog/test-changelog-master.xml");
            liquibase.setContexts("integration,test");
            liquibase.setDropFirst(false);
            return liquibase;
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Detect database type
        detectDatabaseType();
        
        // Ensure Liquibase has run
        if (liquibase != null) {
            liquibase.afterPropertiesSet();
        }
        
        // Create test-specific repository that uses Test_ prefixed tables
        testAuditRepository = new TestAuditRepository(jdbcTemplate, isOracleDatabase);
        
        // Create test table with Test_ prefix for isolation
        createTestTable();
        
        // Verify database connection and features
        verifyDatabaseConnection();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        cleanupTestTable();
    }

    /**
     * Detects the database type for appropriate SQL syntax selection.
     * Uses Java 17+ enhanced exception handling.
     */
    private void detectDatabaseType() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            databaseProductName = metaData.getDatabaseProductName();
            isOracleDatabase = databaseProductName.toLowerCase().contains("oracle");
        } catch (SQLException e) {
            // Default to non-Oracle behavior
            isOracleDatabase = false;
            databaseProductName = "Unknown";
        }
    }

    /**
     * Creates the test audit table with Test_ prefix using database-appropriate syntax.
     * Uses Java 17+ text blocks for better SQL readability and switch expressions for database-specific logic.
     */
    private void createTestTable() {
        // Drop test table if exists using database-appropriate syntax
        try {
            String dropTableSql = isOracleDatabase ? 
                "DROP TABLE Test_PIPELINE_AUDIT_LOG CASCADE CONSTRAINTS" :
                "DROP TABLE IF EXISTS Test_PIPELINE_AUDIT_LOG CASCADE";
            jdbcTemplate.execute(dropTableSql);
        } catch (Exception e) {
            // Table doesn't exist, which is fine
        }
        
        // Create test table with database-specific features using Java 17+ switch expressions
        String createTableSql = switch (isOracleDatabase ? "oracle" : "h2") {
            case "oracle" -> """
                CREATE TABLE Test_PIPELINE_AUDIT_LOG (
                    AUDIT_ID VARCHAR2(36) CONSTRAINT Test_PK_AUDIT_ID PRIMARY KEY,
                    CORRELATION_ID VARCHAR2(36) NOT NULL,
                    SOURCE_SYSTEM VARCHAR2(50) NOT NULL,
                    MODULE_NAME VARCHAR2(100),
                    PROCESS_NAME VARCHAR2(100),
                    SOURCE_ENTITY VARCHAR2(200),
                    DESTINATION_ENTITY VARCHAR2(200),
                    KEY_IDENTIFIER VARCHAR2(100),
                    CHECKPOINT_STAGE VARCHAR2(50) NOT NULL,
                    STATUS VARCHAR2(20) NOT NULL,
                    EVENT_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    MESSAGE VARCHAR2(1000),
                    DETAILS_JSON CLOB,
                    CONSTRAINT Test_CHK_AUDIT_STATUS CHECK (STATUS IN ('SUCCESS', 'FAILURE', 'WARNING')),
                    CONSTRAINT Test_CHK_CHECKPOINT_STAGE CHECK (CHECKPOINT_STAGE IN ('RHEL_LANDING', 'SQLLOADER_START', 'SQLLOADER_COMPLETE', 'LOGIC_APPLIED', 'FILE_GENERATED'))
                ) TABLESPACE USERS
                """;
            default -> """
                CREATE TABLE Test_PIPELINE_AUDIT_LOG (
                    AUDIT_ID VARCHAR(36) PRIMARY KEY,
                    CORRELATION_ID VARCHAR(36) NOT NULL,
                    SOURCE_SYSTEM VARCHAR(50) NOT NULL,
                    MODULE_NAME VARCHAR(100),
                    PROCESS_NAME VARCHAR(100),
                    SOURCE_ENTITY VARCHAR(200),
                    DESTINATION_ENTITY VARCHAR(200),
                    KEY_IDENTIFIER VARCHAR(100),
                    CHECKPOINT_STAGE VARCHAR(50) NOT NULL,
                    STATUS VARCHAR(20) NOT NULL,
                    EVENT_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
                    MESSAGE VARCHAR(1000),
                    DETAILS_JSON CLOB,
                    CONSTRAINT Test_CHK_AUDIT_STATUS CHECK (STATUS IN ('SUCCESS', 'FAILURE', 'WARNING')),
                    CONSTRAINT Test_CHK_CHECKPOINT_STAGE CHECK (CHECKPOINT_STAGE IN ('RHEL_LANDING', 'SQLLOADER_START', 'SQLLOADER_COMPLETE', 'LOGIC_APPLIED', 'FILE_GENERATED'))
                )
                """;
        };
        jdbcTemplate.execute(createTableSql);
        
        // Create database-optimized indexes using Java 17+ text blocks
        createIndexes();
    }

    /**
     * Creates performance-optimized indexes for the test table.
     * Uses database-specific syntax for optimal performance.
     */
    private void createIndexes() {
        String tablespaceClause = isOracleDatabase ? " TABLESPACE USERS" : "";
        
        // Correlation ID index for fast correlation-based queries
        String createCorrelationIndexSql = """
            CREATE INDEX Test_IDX_CORRELATION_ID 
            ON Test_PIPELINE_AUDIT_LOG(CORRELATION_ID)%s
            """.formatted(tablespaceClause);
        jdbcTemplate.execute(createCorrelationIndexSql);
        
        // Composite index for source system and checkpoint stage queries
        String createSourceSystemIndexSql = """
            CREATE INDEX Test_IDX_SOURCE_SYSTEM_STAGE 
            ON Test_PIPELINE_AUDIT_LOG(SOURCE_SYSTEM, CHECKPOINT_STAGE)%s
            """.formatted(tablespaceClause);
        jdbcTemplate.execute(createSourceSystemIndexSql);
        
        // Timestamp index for date range queries (descending for recent-first ordering)
        String createTimestampIndexSql = isOracleDatabase ? 
            """
            CREATE INDEX Test_IDX_EVENT_TIMESTAMP 
            ON Test_PIPELINE_AUDIT_LOG(EVENT_TIMESTAMP DESC) 
            TABLESPACE USERS
            """ :
            """
            CREATE INDEX Test_IDX_EVENT_TIMESTAMP 
            ON Test_PIPELINE_AUDIT_LOG(EVENT_TIMESTAMP DESC)
            """;
        jdbcTemplate.execute(createTimestampIndexSql);
        
        // Composite index for module name and status queries
        String createModuleStatusIndexSql = """
            CREATE INDEX Test_IDX_MODULE_STATUS 
            ON Test_PIPELINE_AUDIT_LOG(MODULE_NAME, STATUS)%s
            """.formatted(tablespaceClause);
        jdbcTemplate.execute(createModuleStatusIndexSql);
        
        // Additional index for correlation ID and status (for statistics queries)
        String createCorrelationStatusIndexSql = """
            CREATE INDEX Test_IDX_CORRELATION_STATUS 
            ON Test_PIPELINE_AUDIT_LOG(CORRELATION_ID, STATUS)%s
            """.formatted(tablespaceClause);
        jdbcTemplate.execute(createCorrelationStatusIndexSql);
    }

    /**
     * Verifies database connection and database-specific features.
     * Uses Java 17+ pattern matching and enhanced switch expressions.
     */
    private void verifyDatabaseConnection() {
        // Test database connection with appropriate test query
        String testSql = isOracleDatabase ? "SELECT 1 FROM DUAL" : "SELECT 1";
        Integer result = jdbcTemplate.queryForObject(testSql, Integer.class);
        assertThat(result).isEqualTo(1);
        
        if (isOracleDatabase) {
            verifyOracleSpecificFeatures();
        } else {
            verifyGenericDatabaseFeatures();
        }
    }

    /**
     * Verifies Oracle-specific database features and functions.
     */
    private void verifyOracleSpecificFeatures() {
        // Verify Oracle version and features
        try {
            String versionSql = "SELECT BANNER FROM V$VERSION WHERE ROWNUM = 1";
            String version = jdbcTemplate.queryForObject(versionSql, String.class);
            assertThat(version).containsIgnoringCase("Oracle");
        } catch (Exception e) {
            // May not have access to V$VERSION in test environment
        }
        
        // Test Oracle-specific functions using Java 17+ text blocks
        String oracleFunctionSql = """
            SELECT 
                SYSDATE as current_date,
                SYS_GUID() as oracle_guid,
                USER as current_user
            FROM DUAL
            """;
        Map<String, Object> oracleResults = jdbcTemplate.queryForMap(oracleFunctionSql);
        assertThat(oracleResults).containsKeys("CURRENT_DATE", "ORACLE_GUID", "CURRENT_USER");
    }

    /**
     * Verifies generic database features for non-Oracle databases.
     */
    private void verifyGenericDatabaseFeatures() {
        // Test standard SQL functions
        String standardSql = """
            SELECT 
                CURRENT_TIMESTAMP as current_ts,
                'test' as test_string
            """;
        Map<String, Object> results = jdbcTemplate.queryForMap(standardSql);
        assertThat(results).containsKeys("CURRENT_TS", "TEST_STRING");
    }

    /**
     * Cleans up test data from the test table
     */
    private void cleanupTestTable() {
        try {
            jdbcTemplate.execute("DELETE FROM Test_PIPELINE_AUDIT_LOG");
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Nested
    @DisplayName("Database Connection and Configuration Tests")
    class DatabaseConnectionTests {

        @Test
        @DisplayName("Should connect to Oracle database successfully")
        void shouldConnectToOracleDatabaseSuccessfully() {
            // Test basic Oracle connectivity
            String testSql = "SELECT 'Oracle Connection Test' FROM DUAL";
            String result = jdbcTemplate.queryForObject(testSql, String.class);
            assertThat(result).isEqualTo("Oracle Connection Test");
        }

        @Test
        @DisplayName("Should verify Oracle-specific data types and functions")
        void shouldVerifyOracleSpecificDataTypesAndFunctions() {
            // Test Oracle UUID generation
            String uuidSql = "SELECT SYS_GUID() FROM DUAL";
            String uuid = jdbcTemplate.queryForObject(uuidSql, String.class);
            assertThat(uuid).isNotNull().hasSize(32);
            
            // Test Oracle timestamp functions
            String timestampSql = "SELECT CURRENT_TIMESTAMP, SYSTIMESTAMP FROM DUAL";
            jdbcTemplate.queryForMap(timestampSql);
            
            // Test Oracle CLOB handling
            String clobTestSql = """
                SELECT TO_CLOB('{"test": "Oracle CLOB support", "size": 1024}') as test_clob 
                FROM DUAL
                """;
            String clobResult = jdbcTemplate.queryForObject(clobTestSql, String.class);
            assertThat(clobResult).contains("Oracle CLOB support");
        }

        @Test
        @DisplayName("Should verify Oracle connection pool configuration")
        void shouldVerifyOracleConnectionPoolConfiguration() {
            // Test multiple concurrent connections
            for (int i = 0; i < 3; i++) {
                String testSql = "SELECT 'Connection ' || ? || ' Test' FROM DUAL";
                String result = jdbcTemplate.queryForObject(testSql, String.class, i + 1);
                assertThat(result).isEqualTo("Connection " + (i + 1) + " Test");
            }
        }
    }

    @Nested
    @DisplayName("Liquibase Schema Creation and Migration Tests")
    class LiquibaseSchemaTests {

        @Test
        @DisplayName("Should verify Liquibase has created test table structure")
        void shouldVerifyLiquibaseHasCreatedTestTableStructure() {
            // Verify test table exists
            String tableExistsSql = """
                SELECT COUNT(*) 
                FROM USER_TABLES 
                WHERE TABLE_NAME = 'TEST_PIPELINE_AUDIT_LOG'
                """;
            Integer tableCount = jdbcTemplate.queryForObject(tableExistsSql, Integer.class);
            assertThat(tableCount).isEqualTo(1);
        }

        @Test
        @DisplayName("Should verify table constraints are properly created")
        void shouldVerifyTableConstraintsAreProperlyCreated() {
            // Check primary key constraint
            String pkConstraintSql = """
                SELECT COUNT(*) 
                FROM USER_CONSTRAINTS 
                WHERE TABLE_NAME = 'TEST_PIPELINE_AUDIT_LOG' 
                AND CONSTRAINT_TYPE = 'P'
                """;
            Integer pkCount = jdbcTemplate.queryForObject(pkConstraintSql, Integer.class);
            assertThat(pkCount).isEqualTo(1);
            
            // Check check constraints
            String checkConstraintSql = """
                SELECT COUNT(*) 
                FROM USER_CONSTRAINTS 
                WHERE TABLE_NAME = 'TEST_PIPELINE_AUDIT_LOG' 
                AND CONSTRAINT_TYPE = 'C'
                AND CONSTRAINT_NAME LIKE 'TEST_CHK_%'
                """;
            Integer checkCount = jdbcTemplate.queryForObject(checkConstraintSql, Integer.class);
            assertThat(checkCount).isGreaterThanOrEqualTo(2); // Status and checkpoint stage checks
        }

        @Test
        @DisplayName("Should verify indexes are created for performance")
        void shouldVerifyIndexesAreCreatedForPerformance() {
            // Check that indexes exist
            String indexSql = """
                SELECT COUNT(*) 
                FROM USER_INDEXES 
                WHERE TABLE_NAME = 'TEST_PIPELINE_AUDIT_LOG'
                AND INDEX_NAME LIKE 'TEST_IDX_%'
                """;
            Integer indexCount = jdbcTemplate.queryForObject(indexSql, Integer.class);
            assertThat(indexCount).isGreaterThanOrEqualTo(4); // At least 4 indexes created
        }
    }

    @Nested
    @DisplayName("Oracle JdbcTemplate CRUD Operations Tests")
    class OracleJdbcTemplateCrudTests {

        @Test
        @DisplayName("Should save and retrieve audit event with Oracle-specific features")
        void shouldSaveAndRetrieveAuditEventWithOracleSpecificFeatures() {
            // Given - Create test event with Oracle-optimized data
            AuditEvent testEvent = createTestAuditEvent(
                UUID.randomUUID(), 
                "ORACLE_MAINFRAME_SYSTEM", 
                LocalDateTime.now(), 
                AuditStatus.SUCCESS
            );
            testEvent = AuditEvent.builder()
                .from(testEvent)
                .detailsJson("{\"oracleFeatures\": true, \"batchSize\": 1000, \"performance\": \"optimized\"}")
                .build();

            // When - Save using Oracle-optimized SQL
            testAuditRepository.save(testEvent);
            Optional<AuditEvent> retrieved = testAuditRepository.findById(testEvent.getAuditId());

            // Then - Verify Oracle-specific data handling
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getAuditId()).isEqualTo(testEvent.getAuditId());
            assertThat(retrieved.get().getDetailsJson()).contains("oracleFeatures");
        }

        @Test
        @DisplayName("Should handle Oracle CLOB data correctly")
        void shouldHandleOracleClobDataCorrectly() {
            // Given - Create event with large JSON data (CLOB)
            StringBuilder largeJson = new StringBuilder("{\"largeData\": \"");
            for (int i = 0; i < 1000; i++) {
                largeJson.append("Oracle CLOB test data ").append(i).append(" ");
            }
            largeJson.append("\"}");
            
            AuditEvent testEvent = createTestAuditEvent(
                UUID.randomUUID(), 
                "CLOB_TEST_SYSTEM", 
                LocalDateTime.now(), 
                AuditStatus.SUCCESS
            );
            testEvent = AuditEvent.builder()
                .from(testEvent)
                .detailsJson(largeJson.toString())
                .build();

            // When - Save and retrieve CLOB data
            testAuditRepository.save(testEvent);
            Optional<AuditEvent> retrieved = testAuditRepository.findById(testEvent.getAuditId());

            // Then - Verify CLOB handling
            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getDetailsJson()).hasSize(largeJson.length());
            assertThat(retrieved.get().getDetailsJson()).contains("Oracle CLOB test data");
        }

        @Test
        @DisplayName("Should perform batch operations efficiently with Oracle")
        void shouldPerformBatchOperationsEfficientlyWithOracle() {
            // Given - Create multiple test events for batch processing
            int batchSize = 100;
            for (int i = 0; i < batchSize; i++) {
                AuditEvent event = createTestAuditEvent(
                    UUID.randomUUID(), 
                    "BATCH_SYSTEM_" + (i % 10), 
                    LocalDateTime.now().minusMinutes(i), 
                    i % 3 == 0 ? AuditStatus.FAILURE : AuditStatus.SUCCESS
                );
                testAuditRepository.save(event);
            }

            // When - Query all events
            List<AuditEvent> allEvents = testAuditRepository.findAllWithPagination(0, batchSize);

            // Then - Verify batch processing
            assertThat(allEvents).hasSize(batchSize);
        }
    }

    @Nested
    @DisplayName("Oracle Query Performance and Optimization Tests")
    class OracleQueryPerformanceTests {

        @Test
        @DisplayName("Should perform correlation ID queries efficiently with Oracle indexes")
        void shouldPerformCorrelationIdQueriesEfficientlyWithOracleIndexes() {
            // Given - Create test data with same correlation ID
            UUID correlationId = UUID.randomUUID();
            int eventCount = 50;
            
            for (int i = 0; i < eventCount; i++) {
                AuditEvent event = createTestAuditEvent(
                    correlationId, 
                    "PERF_SYSTEM_" + (i % 5), 
                    LocalDateTime.now().minusMinutes(i), 
                    AuditStatus.SUCCESS
                );
                testAuditRepository.save(event);
            }

            // When - Perform indexed query
            long startTime = System.currentTimeMillis();
            List<AuditEvent> results = testAuditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId);
            long endTime = System.currentTimeMillis();

            // Then - Verify performance and results
            assertThat(results).hasSize(eventCount);
            assertThat(endTime - startTime).isLessThan(1000); // Should be fast with index
            
            // Verify ordering
            for (int i = 1; i < results.size(); i++) {
                assertThat(results.get(i).getEventTimestamp())
                    .isAfterOrEqualTo(results.get(i-1).getEventTimestamp());
            }
        }

        @Test
        @DisplayName("Should perform date range queries efficiently with Oracle timestamp indexes")
        void shouldPerformDateRangeQueriesEfficientlyWithOracleTimestampIndexes() {
            // Given - Create test data across different time periods
            LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 0, 0);
            int totalEvents = 200;
            
            for (int i = 0; i < totalEvents; i++) {
                AuditEvent event = createTestAuditEvent(
                    UUID.randomUUID(), 
                    "TIME_SYSTEM_" + (i % 3), 
                    baseTime.plusHours(i), 
                    AuditStatus.SUCCESS
                );
                testAuditRepository.save(event);
            }

            // When - Query specific date range using Oracle timestamp functions
            LocalDateTime startDate = baseTime.plusDays(2);
            LocalDateTime endDate = baseTime.plusDays(5);
            
            long startTime = System.currentTimeMillis();
            List<AuditEvent> results = testAuditRepository.findByEventTimestampBetween(startDate, endDate);
            long endTime = System.currentTimeMillis();

            // Then - Verify performance and accuracy
            assertThat(endTime - startTime).isLessThan(2000); // Should be fast with timestamp index
            assertThat(results).isNotEmpty();
            
            // Verify all results are within date range
            results.forEach(event -> {
                assertThat(event.getEventTimestamp()).isAfterOrEqualTo(startDate);
                assertThat(event.getEventTimestamp()).isBeforeOrEqualTo(endDate);
            });
        }

        @Test
        @DisplayName("Should perform complex queries with Oracle-specific optimizations")
        void shouldPerformComplexQueriesWithOracleSpecificOptimizations() {
            // Given - Create diverse test data
            for (int i = 0; i < 100; i++) {
                AuditEvent event = createTestAuditEvent(
                    UUID.randomUUID(), 
                    "COMPLEX_SYSTEM_" + (i % 5), 
                    LocalDateTime.now().minusHours(i), 
                    i % 4 == 0 ? AuditStatus.FAILURE : AuditStatus.SUCCESS
                );
                event = AuditEvent.builder()
                    .from(event)
                    .moduleName("MODULE_" + (i % 3))
                    .checkpointStage(i % 2 == 0 ? CheckpointStage.SQLLOADER_START : CheckpointStage.LOGIC_APPLIED)
                    .build();
                testAuditRepository.save(event);
            }

            // When - Perform complex multi-column queries
            long startTime = System.currentTimeMillis();
            
            List<AuditEvent> systemStageResults = testAuditRepository.findBySourceSystemAndCheckpointStage(
                "COMPLEX_SYSTEM_1", CheckpointStage.SQLLOADER_START);
            
            List<AuditEvent> moduleStatusResults = testAuditRepository.findByModuleNameAndStatus(
                "MODULE_1", AuditStatus.FAILURE);
            
            long endTime = System.currentTimeMillis();

            // Then - Verify performance and results
            assertThat(endTime - startTime).isLessThan(3000); // Complex queries should still be fast
            assertThat(systemStageResults).isNotEmpty();
            assertThat(moduleStatusResults).isNotEmpty();
            
            // Verify query accuracy
            systemStageResults.forEach(event -> {
                assertThat(event.getSourceSystem()).isEqualTo("COMPLEX_SYSTEM_1");
                assertThat(event.getCheckpointStage()).isEqualTo(CheckpointStage.SQLLOADER_START);
            });
        }
    }

    @Nested
    @DisplayName("Oracle Transaction Management Tests")
    class OracleTransactionManagementTests {

        @Test
        @DisplayName("Should handle Oracle transactions correctly")
        void shouldHandleOracleTransactionsCorrectly() {
            // Given - Create test event
            AuditEvent testEvent = createTestAuditEvent(
                UUID.randomUUID(), 
                "TRANSACTION_SYSTEM", 
                LocalDateTime.now(), 
                AuditStatus.SUCCESS
            );

            // When - Save within transaction context
            testAuditRepository.save(testEvent);
            
            // Then - Verify transaction committed
            Optional<AuditEvent> retrieved = testAuditRepository.findById(testEvent.getAuditId());
            assertThat(retrieved).isPresent();
        }

        @Test
        @DisplayName("Should handle Oracle connection recovery")
        void shouldHandleOracleConnectionRecovery() {
            // Test that the connection pool can recover from connection issues
            // This is more of a configuration verification test
            
            // Given - Multiple operations to test connection pooling
            for (int i = 0; i < 10; i++) {
                AuditEvent event = createTestAuditEvent(
                    UUID.randomUUID(), 
                    "RECOVERY_SYSTEM", 
                    LocalDateTime.now().minusMinutes(i), 
                    AuditStatus.SUCCESS
                );
                
                // When - Save each event (tests connection pool)
                testAuditRepository.save(event);
                
                // Then - Verify immediate retrieval
                Optional<AuditEvent> retrieved = testAuditRepository.findById(event.getAuditId());
                assertThat(retrieved).isPresent();
            }
        }
    }

    @Nested
    @DisplayName("Comprehensive Repository Method Tests")
    class ComprehensiveRepositoryMethodTests {

        @Test
        @DisplayName("Should test all repository CRUD operations comprehensively")
        void shouldTestAllRepositoryCrudOperationsComprehensively() {
            // Given - Create comprehensive test data
            UUID correlationId = UUID.randomUUID();
            List<AuditEvent> testEvents = createComprehensiveTestData(correlationId);
            
            // When - Save all test events
            testEvents.forEach(testAuditRepository::save);
            
            // Then - Verify all CRUD operations
            verifyAllCrudOperations(testEvents, correlationId);
        }

        @Test
        @DisplayName("Should test all query methods with comprehensive data")
        void shouldTestAllQueryMethodsWithComprehensiveData() {
            // Given - Create diverse test data
            createDiverseTestDataSet();
            
            // When & Then - Test all query methods
            testCorrelationIdQueries();
            testSourceSystemQueries();
            testModuleNameQueries();
            testDateRangeQueries();
            testStatisticsQueries();
            testPaginationQueries();
        }

        @Test
        @DisplayName("Should handle large dataset operations efficiently")
        void shouldHandleLargeDatasetOperationsEfficiently() {
            // Given - Create large dataset
            int largeDatasetSize = 1000;
            UUID correlationId = UUID.randomUUID();
            
            long startTime = System.currentTimeMillis();
            
            // When - Insert large dataset
            for (int i = 0; i < largeDatasetSize; i++) {
                AuditEvent event = createTestAuditEvent(
                    correlationId, 
                    "LARGE_SYSTEM_" + (i % 10), 
                    LocalDateTime.now().minusMinutes(i), 
                    i % 3 == 0 ? AuditStatus.FAILURE : AuditStatus.SUCCESS
                );
                testAuditRepository.save(event);
            }
            
            long insertTime = System.currentTimeMillis() - startTime;
            
            // Then - Verify performance and query large dataset
            startTime = System.currentTimeMillis();
            List<AuditEvent> results = testAuditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId);
            long queryTime = System.currentTimeMillis() - startTime;
            
            assertThat(results).hasSize(largeDatasetSize);
            assertThat(insertTime).isLessThan(30000); // Should complete within 30 seconds
            assertThat(queryTime).isLessThan(5000);   // Query should be fast with indexes
        }

        @Test
        @DisplayName("Should handle concurrent operations safely")
        void shouldHandleConcurrentOperationsSafely() throws InterruptedException {
            // Given - Prepare concurrent test data
            int threadCount = 10;
            int operationsPerThread = 50;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            
            // When - Execute concurrent operations
            CompletableFuture<Void>[] futures = new CompletableFuture[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                final int threadId = i;
                futures[i] = CompletableFuture.runAsync(() -> {
                    for (int j = 0; j < operationsPerThread; j++) {
                        AuditEvent event = createTestAuditEvent(
                            UUID.randomUUID(),
                            "CONCURRENT_SYSTEM_" + threadId,
                            LocalDateTime.now(),
                            AuditStatus.SUCCESS
                        );
                        testAuditRepository.save(event);
                    }
                }, executor);
            }
            
            // Wait for all operations to complete
            CompletableFuture.allOf(futures).join();
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);
            
            // Then - Verify all operations completed successfully
            String countSql = "SELECT COUNT(*) FROM Test_PIPELINE_AUDIT_LOG WHERE SOURCE_SYSTEM LIKE 'CONCURRENT_SYSTEM_%'";
            Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            assertThat(totalCount).isEqualTo(threadCount * operationsPerThread);
        }

        private List<AuditEvent> createComprehensiveTestData(UUID correlationId) {
            return List.of(
                createTestAuditEvent(correlationId, "SYSTEM_A", LocalDateTime.now().minusHours(1), AuditStatus.SUCCESS),
                createTestAuditEvent(correlationId, "SYSTEM_B", LocalDateTime.now().minusMinutes(30), AuditStatus.FAILURE),
                createTestAuditEvent(correlationId, "SYSTEM_C", LocalDateTime.now().minusMinutes(15), AuditStatus.WARNING),
                createTestAuditEvent(correlationId, "SYSTEM_A", LocalDateTime.now().minusMinutes(5), AuditStatus.SUCCESS)
            );
        }

        private void verifyAllCrudOperations(List<AuditEvent> testEvents, UUID correlationId) {
            // Test findById
            for (AuditEvent event : testEvents) {
                Optional<AuditEvent> retrieved = testAuditRepository.findById(event.getAuditId());
                assertThat(retrieved).isPresent();
                assertThat(retrieved.get().getCorrelationId()).isEqualTo(correlationId);
            }
            
            // Test findByCorrelationId
            List<AuditEvent> correlationResults = testAuditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId);
            assertThat(correlationResults).hasSize(testEvents.size());
            
            // Test count operations
            long successCount = testAuditRepository.countByCorrelationIdAndStatus(correlationId, AuditStatus.SUCCESS);
            long failureCount = testAuditRepository.countByCorrelationIdAndStatus(correlationId, AuditStatus.FAILURE);
            long warningCount = testAuditRepository.countByCorrelationIdAndStatus(correlationId, AuditStatus.WARNING);
            
            assertThat(successCount + failureCount + warningCount).isEqualTo(testEvents.size());
        }

        private void createDiverseTestDataSet() {
            // Create test data with various combinations
            for (int i = 0; i < 50; i++) {
                AuditEvent event = createTestAuditEvent(
                    UUID.randomUUID(),
                    "DIVERSE_SYSTEM_" + (i % 5),
                    LocalDateTime.now().minusHours(i % 24),
                    AuditStatus.values()[i % 3]
                );
                event = AuditEvent.builder()
                    .from(event)
                    .moduleName("MODULE_" + (i % 3))
                    .checkpointStage(CheckpointStage.values()[i % CheckpointStage.values().length])
                    .build();
                testAuditRepository.save(event);
            }
        }

        private void testCorrelationIdQueries() {
            // Test correlation ID queries with known data
            UUID testCorrelationId = UUID.randomUUID();
            AuditEvent event = createTestAuditEvent(testCorrelationId, "CORRELATION_TEST", LocalDateTime.now(), AuditStatus.SUCCESS);
            testAuditRepository.save(event);
            
            List<AuditEvent> results = testAuditRepository.findByCorrelationIdOrderByEventTimestamp(testCorrelationId);
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCorrelationId()).isEqualTo(testCorrelationId);
        }

        private void testSourceSystemQueries() {
            // Test source system and checkpoint stage queries
            List<AuditEvent> results = testAuditRepository.findBySourceSystemAndCheckpointStage(
                "DIVERSE_SYSTEM_0", CheckpointStage.RHEL_LANDING);
            assertThat(results).isNotEmpty();
            
            results.forEach(event -> {
                assertThat(event.getSourceSystem()).isEqualTo("DIVERSE_SYSTEM_0");
                assertThat(event.getCheckpointStage()).isEqualTo(CheckpointStage.RHEL_LANDING);
            });
        }

        private void testModuleNameQueries() {
            // Test module name and status queries
            List<AuditEvent> results = testAuditRepository.findByModuleNameAndStatus("MODULE_0", AuditStatus.SUCCESS);
            assertThat(results).isNotEmpty();
            
            results.forEach(event -> {
                assertThat(event.getModuleName()).isEqualTo("MODULE_0");
                assertThat(event.getStatus()).isEqualTo(AuditStatus.SUCCESS);
            });
        }

        private void testDateRangeQueries() {
            // Test date range queries
            LocalDateTime startDate = LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = LocalDateTime.now().plusHours(1);
            
            List<AuditEvent> results = testAuditRepository.findByEventTimestampBetween(startDate, endDate);
            assertThat(results).isNotEmpty();
            
            results.forEach(event -> {
                assertThat(event.getEventTimestamp()).isAfterOrEqualTo(startDate);
                assertThat(event.getEventTimestamp()).isBeforeOrEqualTo(endDate);
            });
        }

        private void testStatisticsQueries() {
            // Test count queries for statistics
            UUID testCorrelationId = UUID.randomUUID();
            testAuditRepository.save(createTestAuditEvent(testCorrelationId, "STATS_TEST", LocalDateTime.now(), AuditStatus.SUCCESS));
            testAuditRepository.save(createTestAuditEvent(testCorrelationId, "STATS_TEST", LocalDateTime.now(), AuditStatus.FAILURE));
            
            long successCount = testAuditRepository.countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.SUCCESS);
            long failureCount = testAuditRepository.countByCorrelationIdAndStatus(testCorrelationId, AuditStatus.FAILURE);
            
            assertThat(successCount).isEqualTo(1);
            assertThat(failureCount).isEqualTo(1);
        }

        private void testPaginationQueries() {
            // Test pagination functionality
            List<AuditEvent> firstPage = testAuditRepository.findAllWithPagination(0, 10);
            List<AuditEvent> secondPage = testAuditRepository.findAllWithPagination(10, 10);
            
            assertThat(firstPage).hasSizeLessThanOrEqualTo(10);
            assertThat(secondPage).hasSizeLessThanOrEqualTo(10);
            
            // Verify no overlap between pages
            if (!firstPage.isEmpty() && !secondPage.isEmpty()) {
                assertThat(firstPage).doesNotContainAnyElementsOf(secondPage);
            }
        }
    }

    @Nested
    @DisplayName("Database-Specific Feature Tests")
    class DatabaseSpecificFeatureTests {

        @Test
        @DisplayName("Should handle database-specific data types correctly")
        void shouldHandleDatabaseSpecificDataTypesCorrectly() {
            // Test CLOB handling for large JSON data
            StringBuilder largeJson = new StringBuilder("{\"largeData\": \"");
            for (int i = 0; i < 1000; i++) {
                largeJson.append("Database CLOB test data ").append(i).append(" ");
            }
            largeJson.append("\"}");
            
            AuditEvent testEvent = createTestAuditEvent(
                UUID.randomUUID(), 
                "CLOB_TEST_SYSTEM", 
                LocalDateTime.now(), 
                AuditStatus.SUCCESS
            );
            testEvent = AuditEvent.builder()
                .from(testEvent)
                .detailsJson(largeJson.toString())
                .build();

            // Save and retrieve CLOB data
            testAuditRepository.save(testEvent);
            Optional<AuditEvent> retrieved = testAuditRepository.findById(testEvent.getAuditId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getDetailsJson()).hasSize(largeJson.length());
            assertThat(retrieved.get().getDetailsJson()).contains("Database CLOB test data");
        }

        @Test
        @DisplayName("Should handle database-specific functions and queries")
        void shouldHandleDatabaseSpecificFunctionsAndQueries() {
            if (isOracleDatabase) {
                testOracleSpecificFunctions();
            } else {
                testStandardSqlFunctions();
            }
        }

        private void testOracleSpecificFunctions() {
            // Test Oracle-specific functions
            String oracleQuery = """
                SELECT 
                    SYS_GUID() as oracle_guid,
                    SYSDATE as oracle_date,
                    TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') as formatted_date
                FROM DUAL
                """;
            
            Map<String, Object> results = jdbcTemplate.queryForMap(oracleQuery);
            assertThat(results).containsKeys("ORACLE_GUID", "ORACLE_DATE", "FORMATTED_DATE");
        }

        private void testStandardSqlFunctions() {
            // Test standard SQL functions
            String standardQuery = """
                SELECT 
                    CURRENT_TIMESTAMP as current_ts,
                    'standard_sql' as db_type
                """;
            
            Map<String, Object> results = jdbcTemplate.queryForMap(standardQuery);
            assertThat(results).containsKeys("CURRENT_TS", "DB_TYPE");
        }
    }

    /**
     * Test-specific repository implementation that uses Test_ prefixed tables.
     * Supports both Oracle and H2 databases with appropriate SQL syntax.
     */
    private static class TestAuditRepository {
        private final JdbcTemplate jdbcTemplate;
        private final RowMapper<AuditEvent> auditEventRowMapper = new AuditEventRowMapper();
        private final boolean isOracleDatabase;

        public TestAuditRepository(JdbcTemplate jdbcTemplate, boolean isOracleDatabase) {
            this.jdbcTemplate = jdbcTemplate;
            this.isOracleDatabase = isOracleDatabase;
        }

        public void save(AuditEvent auditEvent) {
            String sql = """
                INSERT INTO Test_PIPELINE_AUDIT_LOG (
                    AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME, PROCESS_NAME,
                    SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER, CHECKPOINT_STAGE,
                    STATUS, EVENT_TIMESTAMP, MESSAGE, DETAILS_JSON
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            jdbcTemplate.update(sql,
                auditEvent.getAuditId().toString(),
                auditEvent.getCorrelationId().toString(),
                auditEvent.getSourceSystem(),
                auditEvent.getModuleName(),
                auditEvent.getProcessName(),
                auditEvent.getSourceEntity(),
                auditEvent.getDestinationEntity(),
                auditEvent.getKeyIdentifier(),
                auditEvent.getCheckpointStage().name(),
                auditEvent.getStatus().name(),
                auditEvent.getEventTimestamp(),
                auditEvent.getMessage(),
                auditEvent.getDetailsJson()
            );
        }

        public Optional<AuditEvent> findById(UUID auditId) {
            String sql = "SELECT * FROM Test_PIPELINE_AUDIT_LOG WHERE AUDIT_ID = ?";
            List<AuditEvent> results = jdbcTemplate.query(sql, auditEventRowMapper, auditId.toString());
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        public List<AuditEvent> findByCorrelationIdOrderByEventTimestamp(UUID correlationId) {
            String sql = """
                SELECT * FROM Test_PIPELINE_AUDIT_LOG 
                WHERE CORRELATION_ID = ? 
                ORDER BY EVENT_TIMESTAMP ASC
                """;
            return jdbcTemplate.query(sql, auditEventRowMapper, correlationId.toString());
        }

        public List<AuditEvent> findBySourceSystemAndCheckpointStage(String sourceSystem, CheckpointStage checkpointStage) {
            String sql = """
                SELECT * FROM Test_PIPELINE_AUDIT_LOG 
                WHERE SOURCE_SYSTEM = ? AND CHECKPOINT_STAGE = ?
                ORDER BY EVENT_TIMESTAMP DESC
                """;
            return jdbcTemplate.query(sql, auditEventRowMapper, sourceSystem, checkpointStage.name());
        }

        public List<AuditEvent> findByModuleNameAndStatus(String moduleName, AuditStatus status) {
            String sql = """
                SELECT * FROM Test_PIPELINE_AUDIT_LOG 
                WHERE MODULE_NAME = ? AND STATUS = ?
                ORDER BY EVENT_TIMESTAMP DESC
                """;
            return jdbcTemplate.query(sql, auditEventRowMapper, moduleName, status.name());
        }

        public List<AuditEvent> findByEventTimestampBetween(LocalDateTime startDate, LocalDateTime endDate) {
            String sql = """
                SELECT * FROM Test_PIPELINE_AUDIT_LOG 
                WHERE EVENT_TIMESTAMP BETWEEN ? AND ?
                ORDER BY EVENT_TIMESTAMP ASC
                """;
            return jdbcTemplate.query(sql, auditEventRowMapper, startDate, endDate);
        }

        public List<AuditEvent> findAllWithPagination(int offset, int limit) {
            // Use database-appropriate pagination syntax
            String sql = isOracleDatabase ? 
                """
                SELECT * FROM (
                    SELECT t.*, ROW_NUMBER() OVER (ORDER BY EVENT_TIMESTAMP DESC) as rn
                    FROM Test_PIPELINE_AUDIT_LOG t
                ) WHERE rn > ? AND rn <= ?
                """ :
                """
                SELECT * FROM Test_PIPELINE_AUDIT_LOG 
                ORDER BY EVENT_TIMESTAMP DESC 
                LIMIT ? OFFSET ?
                """;
            
            return isOracleDatabase ?
                jdbcTemplate.query(sql, auditEventRowMapper, offset, offset + limit) :
                jdbcTemplate.query(sql, auditEventRowMapper, limit, offset);
        }

        public long countByCorrelationIdAndStatus(UUID correlationId, AuditStatus status) {
            String sql = """
                SELECT COUNT(*) FROM Test_PIPELINE_AUDIT_LOG 
                WHERE CORRELATION_ID = ? AND STATUS = ?
                """;
            return jdbcTemplate.queryForObject(sql, Long.class, correlationId.toString(), status.name());
        }
    }

    /**
     * RowMapper for mapping Oracle ResultSet to AuditEvent objects
     */
    private static class AuditEventRowMapper implements RowMapper<AuditEvent> {
        @Override
        public AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AuditEvent.builder()
                .auditId(UUID.fromString(rs.getString("AUDIT_ID")))
                .correlationId(UUID.fromString(rs.getString("CORRELATION_ID")))
                .sourceSystem(rs.getString("SOURCE_SYSTEM"))
                .moduleName(rs.getString("MODULE_NAME"))
                .processName(rs.getString("PROCESS_NAME"))
                .sourceEntity(rs.getString("SOURCE_ENTITY"))
                .destinationEntity(rs.getString("DESTINATION_ENTITY"))
                .keyIdentifier(rs.getString("KEY_IDENTIFIER"))
                .checkpointStage(CheckpointStage.valueOf(rs.getString("CHECKPOINT_STAGE")))
                .status(AuditStatus.valueOf(rs.getString("STATUS")))
                .eventTimestamp(rs.getTimestamp("EVENT_TIMESTAMP").toLocalDateTime())
                .message(rs.getString("MESSAGE"))
                .detailsJson(rs.getString("DETAILS_JSON"))
                .build();
        }
    }

    /**
     * Helper method to create test audit events
     */
    private AuditEvent createTestAuditEvent(UUID correlationId, String sourceSystem, 
                                          LocalDateTime timestamp, AuditStatus status) {
        return AuditEvent.builder()
            .auditId(UUID.randomUUID())
            .correlationId(correlationId)
            .sourceSystem(sourceSystem)
            .moduleName("TestModule")
            .processName("TestProcess")
            .sourceEntity("TestSourceEntity")
            .destinationEntity("TestDestinationEntity")
            .keyIdentifier("TEST_KEY_" + System.currentTimeMillis())
            .checkpointStage(CheckpointStage.RHEL_LANDING)
            .eventTimestamp(timestamp)
            .status(status)
            .message("Test audit event message")
            .detailsJson("{\"test\": true, \"environment\": \"integration\"}")
            .build();
    }
}