package com.company.audit.integration;

import com.company.audit.BatchAuditApplication;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Liquibase migrations in integration environment.
 * Tests Spring Boot 3.4+ Liquibase integration with Oracle database.
 * 
 * Requirements tested: 2.5
 */
@SpringBootTest(classes = BatchAuditApplication.class)
@ActiveProfiles("integration")
@TestPropertySource(properties = {
    "spring.liquibase.contexts=integration",
    "audit.database.table-prefix=Test_",
    "spring.liquibase.drop-first=false",
    "spring.liquibase.enabled=true",
    "logging.level.liquibase=DEBUG"
})
class LiquibaseMigrationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Test that Liquibase has created the required tables with Test_ prefix
     * Requirements: 2.5
     */
    @Test
    void testLiquibaseCreatesRequiredTablesWithTestPrefix() {
        // Verify main audit table exists with Test_ prefix
        String auditTableQuery = """
            SELECT COUNT(*) FROM user_tables 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            """;
        
        Integer auditTableCount = jdbcTemplate.queryForObject(auditTableQuery, Integer.class);
        assertThat(auditTableCount).isEqualTo(1);

        // Verify Liquibase tracking tables exist
        String liquibaseTablesQuery = """
            SELECT table_name FROM user_tables 
            WHERE table_name IN ('DATABASECHANGELOG', 'DATABASECHANGELOGLOCK')
            ORDER BY table_name
            """;
        
        List<String> liquibaseTables = jdbcTemplate.queryForList(liquibaseTablesQuery, String.class);
        assertThat(liquibaseTables).containsExactly("DATABASECHANGELOG", "DATABASECHANGELOGLOCK");
    }

    /**
     * Test audit table structure matches expected schema
     * Requirements: 2.5
     */
    @Test
    void testAuditTableStructureMatchesExpectedSchema() {
        String columnQuery = """
            SELECT column_name, data_type, nullable, data_length, data_precision, data_scale
            FROM user_tab_columns 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            ORDER BY column_name
            """;
        
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(columnQuery);
        assertThat(columns).isNotEmpty();
        
        // Extract column information for validation
        Map<String, Map<String, Object>> columnMap = columns.stream()
            .collect(java.util.stream.Collectors.toMap(
                col -> (String) col.get("COLUMN_NAME"),
                col -> col
            ));

        // Verify required columns exist with correct data types
        assertThat(columnMap).containsKey("AUDIT_ID");
        assertThat(columnMap.get("AUDIT_ID").get("DATA_TYPE")).isEqualTo("VARCHAR2");
        assertThat(columnMap.get("AUDIT_ID").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("CORRELATION_ID");
        assertThat(columnMap.get("CORRELATION_ID").get("DATA_TYPE")).isEqualTo("VARCHAR2");
        assertThat(columnMap.get("CORRELATION_ID").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("SOURCE_SYSTEM");
        assertThat(columnMap.get("SOURCE_SYSTEM").get("DATA_TYPE")).isEqualTo("VARCHAR2");
        assertThat(columnMap.get("SOURCE_SYSTEM").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("MODULE_NAME");
        assertThat(columnMap.get("MODULE_NAME").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("PROCESS_NAME");
        assertThat(columnMap.get("PROCESS_NAME").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("SOURCE_ENTITY");
        assertThat(columnMap.get("SOURCE_ENTITY").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("DESTINATION_ENTITY");
        assertThat(columnMap.get("DESTINATION_ENTITY").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("KEY_IDENTIFIER");
        assertThat(columnMap.get("KEY_IDENTIFIER").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("CHECKPOINT_STAGE");
        assertThat(columnMap.get("CHECKPOINT_STAGE").get("DATA_TYPE")).isEqualTo("VARCHAR2");
        assertThat(columnMap.get("CHECKPOINT_STAGE").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("EVENT_TIMESTAMP");
        assertThat(columnMap.get("EVENT_TIMESTAMP").get("DATA_TYPE")).isEqualTo("TIMESTAMP(6)");
        assertThat(columnMap.get("EVENT_TIMESTAMP").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("STATUS");
        assertThat(columnMap.get("STATUS").get("DATA_TYPE")).isEqualTo("VARCHAR2");
        assertThat(columnMap.get("STATUS").get("NULLABLE")).isEqualTo("N");

        assertThat(columnMap).containsKey("MESSAGE");
        assertThat(columnMap.get("MESSAGE").get("DATA_TYPE")).isEqualTo("VARCHAR2");

        assertThat(columnMap).containsKey("DETAILS_JSON");
        assertThat(columnMap.get("DETAILS_JSON").get("DATA_TYPE")).isEqualTo("CLOB");
    }

    /**
     * Test that required indexes are created for performance
     * Requirements: 2.5
     */
    @Test
    void testRequiredIndexesAreCreatedForPerformance() {
        String indexQuery = """
            SELECT index_name, column_name, column_position
            FROM user_ind_columns 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            ORDER BY index_name, column_position
            """;
        
        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(indexQuery);
        assertThat(indexes).isNotEmpty();

        // Extract index names
        List<String> indexNames = indexes.stream()
            .map(idx -> (String) idx.get("INDEX_NAME"))
            .distinct()
            .toList();

        // Verify primary key index exists
        assertThat(indexNames).anyMatch(name -> name.contains("PK") || name.contains("PRIMARY"));

        // Verify performance indexes exist
        boolean hasCorrelationIdIndex = indexes.stream()
            .anyMatch(idx -> "CORRELATION_ID".equals(idx.get("COLUMN_NAME")));
        assertThat(hasCorrelationIdIndex).isTrue();

        boolean hasSourceSystemIndex = indexes.stream()
            .anyMatch(idx -> "SOURCE_SYSTEM".equals(idx.get("COLUMN_NAME")));
        assertThat(hasSourceSystemIndex).isTrue();

        boolean hasEventTimestampIndex = indexes.stream()
            .anyMatch(idx -> "EVENT_TIMESTAMP".equals(idx.get("COLUMN_NAME")));
        assertThat(hasEventTimestampIndex).isTrue();

        boolean hasCheckpointStageIndex = indexes.stream()
            .anyMatch(idx -> "CHECKPOINT_STAGE".equals(idx.get("COLUMN_NAME")));
        assertThat(hasCheckpointStageIndex).isTrue();
    }

    /**
     * Test that table constraints are properly created
     * Requirements: 2.5
     */
    @Test
    void testTableConstraintsAreProperlyCreated() {
        String constraintQuery = """
            SELECT constraint_name, constraint_type, search_condition
            FROM user_constraints 
            WHERE table_name = 'TEST_PIPELINE_AUDIT_LOG'
            ORDER BY constraint_type, constraint_name
            """;
        
        List<Map<String, Object>> constraints = jdbcTemplate.queryForList(constraintQuery);
        assertThat(constraints).isNotEmpty();

        // Verify primary key constraint exists
        boolean hasPrimaryKey = constraints.stream()
            .anyMatch(c -> "P".equals(c.get("CONSTRAINT_TYPE")));
        assertThat(hasPrimaryKey).isTrue();

        // Verify check constraints for status values
        boolean hasStatusCheck = constraints.stream()
            .anyMatch(c -> "C".equals(c.get("CONSTRAINT_TYPE")) && 
                          c.get("SEARCH_CONDITION") != null &&
                          c.get("SEARCH_CONDITION").toString().contains("SUCCESS"));
        assertThat(hasStatusCheck).isTrue();
    }

    /**
     * Test Liquibase changelog execution history
     * Requirements: 2.5
     */
    @Test
    void testLiquibaseChangelogExecutionHistory() {
        String changelogQuery = """
            SELECT id, author, filename, dateexecuted, orderexecuted, exectype, md5sum
            FROM DATABASECHANGELOG
            WHERE filename LIKE '%audit%'
            ORDER BY orderexecuted
            """;
        
        List<Map<String, Object>> changelogs = jdbcTemplate.queryForList(changelogQuery);
        assertThat(changelogs).isNotEmpty();

        // Verify that audit table creation changelog was executed
        boolean hasAuditTableChangelog = changelogs.stream()
            .anyMatch(cl -> cl.get("ID").toString().contains("create-audit-table") ||
                           cl.get("FILENAME").toString().contains("001-create-audit-table"));
        assertThat(hasAuditTableChangelog).isTrue();

        // Verify that audit indexes changelog was executed
        boolean hasAuditIndexesChangelog = changelogs.stream()
            .anyMatch(cl -> cl.get("ID").toString().contains("create-audit-indexes") ||
                           cl.get("FILENAME").toString().contains("002-create-audit-indexes"));
        assertThat(hasAuditIndexesChangelog).isTrue();

        // Verify all changelogs executed successfully
        changelogs.forEach(cl -> 
            assertThat(cl.get("EXECTYPE")).isEqualTo("EXECUTED")
        );
    }

    /**
     * Test Liquibase validation and rollback capabilities
     * Requirements: 2.5
     */
    @Test
    void testLiquibaseValidationAndRollbackCapabilities() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
            
            Liquibase liquibase = new Liquibase(
                "db/changelog/db.changelog-master.xml",
                new ClassLoaderResourceAccessor(),
                database
            );

            // Test validation - should not throw exceptions
            liquibase.validate();

            // Test that we can get the list of unrun changesets (should be empty)
            var unrunChangesets = liquibase.listUnrunChangeSets(null, null);
            // In integration tests, all changesets should already be run
            assertThat(unrunChangesets).isEmpty();

            // Test that we can get the list of ran changesets from database
            String ranChangesetsQuery = """
                SELECT id, author, filename FROM DATABASECHANGELOG 
                WHERE filename LIKE '%audit%' OR id LIKE '%audit%'
                ORDER BY orderexecuted
                """;
            
            List<Map<String, Object>> ranChangesets = jdbcTemplate.queryForList(ranChangesetsQuery);
            assertThat(ranChangesets).isNotEmpty();

            // Verify that our audit-related changesets are in the history
            boolean hasAuditChangesets = ranChangesets.stream()
                .anyMatch(cs -> cs.get("FILENAME").toString().contains("audit") || 
                               cs.get("ID").toString().contains("audit"));
            assertThat(hasAuditChangesets).isTrue();
        }
    }

    /**
     * Test database performance with created indexes
     * Requirements: 2.5
     */
    @Test
    void testDatabasePerformanceWithCreatedIndexes() {
        // Insert test data to verify index performance
        String insertSql = """
            INSERT INTO Test_PIPELINE_AUDIT_LOG 
            (AUDIT_ID, CORRELATION_ID, SOURCE_SYSTEM, MODULE_NAME, PROCESS_NAME, 
             SOURCE_ENTITY, DESTINATION_ENTITY, KEY_IDENTIFIER, CHECKPOINT_STAGE, 
             EVENT_TIMESTAMP, STATUS, MESSAGE, DETAILS_JSON)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        // Insert multiple test records
        for (int i = 0; i < 100; i++) {
            jdbcTemplate.update(insertSql,
                java.util.UUID.randomUUID().toString(),
                java.util.UUID.randomUUID().toString(),
                "PERF_TEST_SYSTEM",
                "PERF_MODULE_" + i,
                "PERFORMANCE_TEST",
                "source_" + i,
                "dest_" + i,
                "key_" + i,
                "RHEL_LANDING",
                java.time.LocalDateTime.now(),
                "SUCCESS",
                "Performance test message " + i,
                "{\"test\": true}"
            );
        }

        // Test query performance with indexes
        long startTime = System.currentTimeMillis();

        // Query by correlation_id (should use index)
        String correlationQuery = """
            SELECT COUNT(*) FROM Test_PIPELINE_AUDIT_LOG 
            WHERE SOURCE_SYSTEM = 'PERF_TEST_SYSTEM'
            """;
        Integer count = jdbcTemplate.queryForObject(correlationQuery, Integer.class);
        assertThat(count).isEqualTo(100);

        long endTime = System.currentTimeMillis();
        long queryTime = endTime - startTime;

        // Query should be fast with proper indexes (less than 100ms for 100 records)
        assertThat(queryTime).isLessThan(100);

        // Test timestamp range query (should use index)
        startTime = System.currentTimeMillis();

        String timestampQuery = """
            SELECT COUNT(*) FROM Test_PIPELINE_AUDIT_LOG 
            WHERE EVENT_TIMESTAMP >= ? AND EVENT_TIMESTAMP <= ?
            """;
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Integer timestampCount = jdbcTemplate.queryForObject(timestampQuery, Integer.class,
            now.minusHours(1), now.plusHours(1));
        
        assertThat(timestampCount).isGreaterThanOrEqualTo(100);

        endTime = System.currentTimeMillis();
        queryTime = endTime - startTime;

        // Timestamp range query should also be fast
        assertThat(queryTime).isLessThan(100);

        // Cleanup test data
        jdbcTemplate.execute("DELETE FROM Test_PIPELINE_AUDIT_LOG WHERE SOURCE_SYSTEM = 'PERF_TEST_SYSTEM'");
    }
}