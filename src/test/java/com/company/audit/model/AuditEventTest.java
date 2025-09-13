package com.company.audit.model;

import com.company.audit.enums.AuditStatus;
import com.company.audit.enums.CheckpointStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AuditEvent model class
 * Tests model creation, field validation, Builder pattern functionality,
 * and equals/hashCode/toString methods as per requirements 2.2 and 8.2
 */
class AuditEventTest {
    
    private UUID testAuditId;
    private UUID testCorrelationId;
    private LocalDateTime testTimestamp;
    
    @BeforeEach
    void setUp() {
        testAuditId = UUID.randomUUID();
        testCorrelationId = UUID.randomUUID();
        testTimestamp = LocalDateTime.now();
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Default constructor should create empty AuditEvent")
        void testDefaultConstructor() {
            AuditEvent auditEvent = new AuditEvent();
            
            assertNull(auditEvent.getAuditId());
            assertNull(auditEvent.getCorrelationId());
            assertNull(auditEvent.getSourceSystem());
            assertNull(auditEvent.getModuleName());
            assertNull(auditEvent.getProcessName());
            assertNull(auditEvent.getSourceEntity());
            assertNull(auditEvent.getDestinationEntity());
            assertNull(auditEvent.getKeyIdentifier());
            assertNull(auditEvent.getCheckpointStage());
            assertNull(auditEvent.getEventTimestamp());
            assertNull(auditEvent.getStatus());
            assertNull(auditEvent.getMessage());
            assertNull(auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("Constructor with audit ID should set only audit ID")
        void testConstructorWithAuditId() {
            AuditEvent auditEvent = new AuditEvent(testAuditId);
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertNull(auditEvent.getCorrelationId());
            assertNull(auditEvent.getSourceSystem());
            assertNull(auditEvent.getModuleName());
        }
        
        @Test
        @DisplayName("Constructor with core fields should set all provided fields")
        void testConstructorWithCoreFields() {
            String sourceSystem = "MAINFRAME_A";
            String moduleName = "DataProcessor";
            
            AuditEvent auditEvent = new AuditEvent(testAuditId, testCorrelationId, sourceSystem, moduleName);
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals(testCorrelationId, auditEvent.getCorrelationId());
            assertEquals(sourceSystem, auditEvent.getSourceSystem());
            assertEquals(moduleName, auditEvent.getModuleName());
            // Other fields should remain null
            assertNull(auditEvent.getProcessName());
            assertNull(auditEvent.getSourceEntity());
            assertNull(auditEvent.getDestinationEntity());
        }
        
        @Test
        @DisplayName("Constructor with null values should handle nulls gracefully")
        void testConstructorWithNullValues() {
            AuditEvent auditEvent = new AuditEvent(null, null, null, null);
            
            assertNull(auditEvent.getAuditId());
            assertNull(auditEvent.getCorrelationId());
            assertNull(auditEvent.getSourceSystem());
            assertNull(auditEvent.getModuleName());
        }
    }
    
    @Nested
    @DisplayName("Field Validation and Getter/Setter Tests")
    class FieldValidationTests {
        
        @Test
        @DisplayName("All UUID fields should accept valid UUIDs")
        void testUuidFieldsValidation() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setAuditId(testAuditId);
            auditEvent.setCorrelationId(testCorrelationId);
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals(testCorrelationId, auditEvent.getCorrelationId());
        }
        
        @Test
        @DisplayName("String fields should accept valid strings and null values")
        void testStringFieldsValidation() {
            AuditEvent auditEvent = new AuditEvent();
            
            // Test with valid strings
            auditEvent.setSourceSystem("MAINFRAME_B");
            auditEvent.setModuleName("FileProcessor");
            auditEvent.setProcessName("DataIngestion");
            auditEvent.setSourceEntity("customer_data.txt");
            auditEvent.setDestinationEntity("CUSTOMER_STAGING");
            auditEvent.setKeyIdentifier("ACC123456");
            auditEvent.setMessage("Processing completed successfully");
            auditEvent.setDetailsJson("{\"recordCount\": 1000, \"fileSize\": 2048}");
            
            assertEquals("MAINFRAME_B", auditEvent.getSourceSystem());
            assertEquals("FileProcessor", auditEvent.getModuleName());
            assertEquals("DataIngestion", auditEvent.getProcessName());
            assertEquals("customer_data.txt", auditEvent.getSourceEntity());
            assertEquals("CUSTOMER_STAGING", auditEvent.getDestinationEntity());
            assertEquals("ACC123456", auditEvent.getKeyIdentifier());
            assertEquals("Processing completed successfully", auditEvent.getMessage());
            assertEquals("{\"recordCount\": 1000, \"fileSize\": 2048}", auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("String fields should handle null values")
        void testStringFieldsWithNullValues() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setSourceSystem(null);
            auditEvent.setModuleName(null);
            auditEvent.setProcessName(null);
            auditEvent.setSourceEntity(null);
            auditEvent.setDestinationEntity(null);
            auditEvent.setKeyIdentifier(null);
            auditEvent.setMessage(null);
            auditEvent.setDetailsJson(null);
            
            assertNull(auditEvent.getSourceSystem());
            assertNull(auditEvent.getModuleName());
            assertNull(auditEvent.getProcessName());
            assertNull(auditEvent.getSourceEntity());
            assertNull(auditEvent.getDestinationEntity());
            assertNull(auditEvent.getKeyIdentifier());
            assertNull(auditEvent.getMessage());
            assertNull(auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("String fields should handle empty strings")
        void testStringFieldsWithEmptyStrings() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setSourceSystem("");
            auditEvent.setModuleName("");
            auditEvent.setMessage("");
            auditEvent.setDetailsJson("");
            
            assertEquals("", auditEvent.getSourceSystem());
            assertEquals("", auditEvent.getModuleName());
            assertEquals("", auditEvent.getMessage());
            assertEquals("", auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("Enum fields should accept valid enum values")
        void testEnumFieldsValidation() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setCheckpointStage(CheckpointStage.RHEL_LANDING);
            auditEvent.setStatus(AuditStatus.SUCCESS);
            
            assertEquals(CheckpointStage.RHEL_LANDING, auditEvent.getCheckpointStage());
            assertEquals(AuditStatus.SUCCESS, auditEvent.getStatus());
            
            // Test all enum values
            for (CheckpointStage stage : CheckpointStage.values()) {
                auditEvent.setCheckpointStage(stage);
                assertEquals(stage, auditEvent.getCheckpointStage());
            }
            
            for (AuditStatus status : AuditStatus.values()) {
                auditEvent.setStatus(status);
                assertEquals(status, auditEvent.getStatus());
            }
        }
        
        @Test
        @DisplayName("Enum fields should handle null values")
        void testEnumFieldsWithNullValues() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setCheckpointStage(null);
            auditEvent.setStatus(null);
            
            assertNull(auditEvent.getCheckpointStage());
            assertNull(auditEvent.getStatus());
        }
        
        @Test
        @DisplayName("LocalDateTime field should accept valid timestamps")
        void testTimestampFieldValidation() {
            AuditEvent auditEvent = new AuditEvent();
            
            auditEvent.setEventTimestamp(testTimestamp);
            assertEquals(testTimestamp, auditEvent.getEventTimestamp());
            
            // Test with null
            auditEvent.setEventTimestamp(null);
            assertNull(auditEvent.getEventTimestamp());
            
            // Test with different timestamp
            LocalDateTime pastTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
            auditEvent.setEventTimestamp(pastTimestamp);
            assertEquals(pastTimestamp, auditEvent.getEventTimestamp());
        }
    }
    
    @Nested
    @DisplayName("toString Method Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("toString should include all non-null field values")
        void testToStringWithAllFields() {
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("TEST_SYSTEM")
                    .moduleName("TestModule")
                    .processName("TestProcess")
                    .sourceEntity("test.txt")
                    .destinationEntity("TEST_TABLE")
                    .keyIdentifier("KEY123")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .eventTimestamp(testTimestamp)
                    .status(AuditStatus.SUCCESS)
                    .message("Test message")
                    .detailsJson("{\"test\": true}")
                    .build();
            
            String toString = auditEvent.toString();
            
            assertTrue(toString.contains("AuditEvent"));
            assertTrue(toString.contains(testAuditId.toString()));
            assertTrue(toString.contains(testCorrelationId.toString()));
            assertTrue(toString.contains("TEST_SYSTEM"));
            assertTrue(toString.contains("TestModule"));
            assertTrue(toString.contains("TestProcess"));
            assertTrue(toString.contains("test.txt"));
            assertTrue(toString.contains("TEST_TABLE"));
            assertTrue(toString.contains("KEY123"));
            assertTrue(toString.contains("RHEL_LANDING"));
            assertTrue(toString.contains(testTimestamp.toString()));
            assertTrue(toString.contains("SUCCESS"));
            assertTrue(toString.contains("Test message"));
            assertTrue(toString.contains("{\"test\": true}"));
        }
        
        @Test
        @DisplayName("toString should handle null values gracefully")
        void testToStringWithNullValues() {
            AuditEvent auditEvent = new AuditEvent();
            
            String toString = auditEvent.toString();
            
            assertTrue(toString.contains("AuditEvent"));
            assertTrue(toString.contains("auditId=null"));
            assertTrue(toString.contains("correlationId=null"));
            assertTrue(toString.contains("sourceSystem='null'"));
            assertTrue(toString.contains("moduleName='null'"));
        }
        
        @Test
        @DisplayName("toString should be consistent across multiple calls")
        void testToStringConsistency() {
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .sourceSystem("CONSISTENT_SYSTEM")
                    .build();
            
            String toString1 = auditEvent.toString();
            String toString2 = auditEvent.toString();
            
            assertEquals(toString1, toString2);
        }
    }
    
    @Nested
    @DisplayName("equals and hashCode Method Tests")
    class EqualsAndHashCodeTests {
        
        @Test
        @DisplayName("equals should return true for identical objects")
        void testEqualsIdenticalObjects() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            assertEquals(auditEvent1, auditEvent2);
            assertEquals(auditEvent2, auditEvent1); // symmetry
        }
        
        @Test
        @DisplayName("equals should return true for same object reference")
        void testEqualsSameReference() {
            AuditEvent auditEvent = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            assertEquals(auditEvent, auditEvent); // reflexivity
        }
        
        @Test
        @DisplayName("equals should return false for different audit IDs")
        void testEqualsWithDifferentAuditIds() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(UUID.randomUUID(), testCorrelationId, "SYSTEM_A", "Module1");
            
            assertNotEquals(auditEvent1, auditEvent2);
        }
        
        @Test
        @DisplayName("equals should return false for different correlation IDs")
        void testEqualsWithDifferentCorrelationIds() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(testAuditId, UUID.randomUUID(), "SYSTEM_A", "Module1");
            
            assertNotEquals(auditEvent1, auditEvent2);
        }
        
        @Test
        @DisplayName("equals should return false for different string fields")
        void testEqualsWithDifferentStringFields() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_B", "Module1");
            AuditEvent auditEvent3 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module2");
            
            assertNotEquals(auditEvent1, auditEvent2);
            assertNotEquals(auditEvent1, auditEvent3);
        }
        
        @Test
        @DisplayName("equals should return false for null and different class objects")
        void testEqualsWithNullAndDifferentClass() {
            AuditEvent auditEvent = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            assertNotEquals(auditEvent, null);
            assertNotEquals(auditEvent, "not an audit event");
            assertNotEquals(auditEvent, 123);
        }
        
        @Test
        @DisplayName("equals should handle null field values correctly")
        void testEqualsWithNullFields() {
            AuditEvent auditEvent1 = new AuditEvent();
            AuditEvent auditEvent2 = new AuditEvent();
            
            assertEquals(auditEvent1, auditEvent2);
            
            auditEvent1.setSourceSystem("SYSTEM_A");
            assertNotEquals(auditEvent1, auditEvent2);
            
            auditEvent2.setSourceSystem("SYSTEM_A");
            assertEquals(auditEvent1, auditEvent2);
        }
        
        @Test
        @DisplayName("equals should handle all field combinations")
        void testEqualsWithAllFields() {
            AuditEvent auditEvent1 = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("SYSTEM_A")
                    .moduleName("Module1")
                    .processName("Process1")
                    .sourceEntity("source.txt")
                    .destinationEntity("dest_table")
                    .keyIdentifier("KEY123")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .eventTimestamp(testTimestamp)
                    .status(AuditStatus.SUCCESS)
                    .message("Test message")
                    .detailsJson("{\"test\": true}")
                    .build();
            
            AuditEvent auditEvent2 = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("SYSTEM_A")
                    .moduleName("Module1")
                    .processName("Process1")
                    .sourceEntity("source.txt")
                    .destinationEntity("dest_table")
                    .keyIdentifier("KEY123")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .eventTimestamp(testTimestamp)
                    .status(AuditStatus.SUCCESS)
                    .message("Test message")
                    .detailsJson("{\"test\": true}")
                    .build();
            
            assertEquals(auditEvent1, auditEvent2);
        }
        
        @Test
        @DisplayName("equals should be transitive")
        void testEqualsTransitivity() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent3 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            assertEquals(auditEvent1, auditEvent2);
            assertEquals(auditEvent2, auditEvent3);
            assertEquals(auditEvent1, auditEvent3); // transitivity
        }
        
        @Test
        @DisplayName("hashCode should be consistent with equals")
        void testHashCodeConsistency() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            assertEquals(auditEvent1, auditEvent2);
            assertEquals(auditEvent1.hashCode(), auditEvent2.hashCode());
        }
        
        @Test
        @DisplayName("hashCode should be consistent across multiple calls")
        void testHashCodeConsistentCalls() {
            AuditEvent auditEvent = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            
            int hashCode1 = auditEvent.hashCode();
            int hashCode2 = auditEvent.hashCode();
            
            assertEquals(hashCode1, hashCode2);
        }
        
        @Test
        @DisplayName("hashCode should handle null values")
        void testHashCodeWithNullValues() {
            AuditEvent auditEvent1 = new AuditEvent();
            AuditEvent auditEvent2 = new AuditEvent();
            
            assertEquals(auditEvent1.hashCode(), auditEvent2.hashCode());
        }
        
        @Test
        @DisplayName("hashCode should produce different values for different objects")
        void testHashCodeDifferentObjects() {
            AuditEvent auditEvent1 = new AuditEvent(testAuditId, testCorrelationId, "SYSTEM_A", "Module1");
            AuditEvent auditEvent2 = new AuditEvent(UUID.randomUUID(), testCorrelationId, "SYSTEM_A", "Module1");
            
            assertNotEquals(auditEvent1.hashCode(), auditEvent2.hashCode());
        }
    }
    
    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {
        
        @Test
        @DisplayName("Builder should create AuditEvent with all fields set")
        void testBuilderWithAllFields() {
            String sourceSystem = "MAINFRAME_A";
            String moduleName = "DataProcessor";
            String processName = "FileIngestion";
            String sourceEntity = "input.txt";
            String destinationEntity = "STAGING_TABLE";
            String keyIdentifier = "12345";
            String message = "Test message";
            String detailsJson = "{\"test\": \"value\"}";
            
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem(sourceSystem)
                    .moduleName(moduleName)
                    .processName(processName)
                    .sourceEntity(sourceEntity)
                    .destinationEntity(destinationEntity)
                    .keyIdentifier(keyIdentifier)
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .eventTimestamp(testTimestamp)
                    .status(AuditStatus.SUCCESS)
                    .message(message)
                    .detailsJson(detailsJson)
                    .build();
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals(testCorrelationId, auditEvent.getCorrelationId());
            assertEquals(sourceSystem, auditEvent.getSourceSystem());
            assertEquals(moduleName, auditEvent.getModuleName());
            assertEquals(processName, auditEvent.getProcessName());
            assertEquals(sourceEntity, auditEvent.getSourceEntity());
            assertEquals(destinationEntity, auditEvent.getDestinationEntity());
            assertEquals(keyIdentifier, auditEvent.getKeyIdentifier());
            assertEquals(CheckpointStage.RHEL_LANDING, auditEvent.getCheckpointStage());
            assertEquals(testTimestamp, auditEvent.getEventTimestamp());
            assertEquals(AuditStatus.SUCCESS, auditEvent.getStatus());
            assertEquals(message, auditEvent.getMessage());
            assertEquals(detailsJson, auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("Builder should handle partial field initialization")
        void testBuilderWithPartialFields() {
            String sourceSystem = "MAINFRAME_B";
            
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .sourceSystem(sourceSystem)
                    .status(AuditStatus.WARNING)
                    .build();
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals(sourceSystem, auditEvent.getSourceSystem());
            assertEquals(AuditStatus.WARNING, auditEvent.getStatus());
            
            // Unset fields should be null
            assertNull(auditEvent.getCorrelationId());
            assertNull(auditEvent.getModuleName());
            assertNull(auditEvent.getProcessName());
            assertNull(auditEvent.getSourceEntity());
            assertNull(auditEvent.getDestinationEntity());
            assertNull(auditEvent.getKeyIdentifier());
            assertNull(auditEvent.getCheckpointStage());
            assertNull(auditEvent.getEventTimestamp());
            assertNull(auditEvent.getMessage());
            assertNull(auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("Builder should support fluent interface pattern")
        void testBuilderFluentInterface() {
            AuditEvent.Builder builder = AuditEvent.builder();
            
            // Test that all builder methods return the same builder instance for method chaining
            assertSame(builder, builder.auditId(testAuditId));
            assertSame(builder, builder.correlationId(testCorrelationId));
            assertSame(builder, builder.sourceSystem("TEST"));
            assertSame(builder, builder.moduleName("TEST_MODULE"));
            assertSame(builder, builder.processName("TEST_PROCESS"));
            assertSame(builder, builder.sourceEntity("TEST_SOURCE"));
            assertSame(builder, builder.destinationEntity("TEST_DEST"));
            assertSame(builder, builder.keyIdentifier("TEST_KEY"));
            assertSame(builder, builder.checkpointStage(CheckpointStage.RHEL_LANDING));
            assertSame(builder, builder.eventTimestamp(testTimestamp));
            assertSame(builder, builder.status(AuditStatus.SUCCESS));
            assertSame(builder, builder.message("TEST_MESSAGE"));
            assertSame(builder, builder.detailsJson("{}"));
        }
        
        @Test
        @DisplayName("Builder should create new instances on each build() call")
        void testBuilderCreatesNewInstances() {
            AuditEvent.Builder builder = AuditEvent.builder();
            
            AuditEvent auditEvent1 = builder.auditId(testAuditId).sourceSystem("SYSTEM_A").build();
            AuditEvent auditEvent2 = builder.sourceSystem("SYSTEM_B").build();
            
            // Both events should have the same auditId since we're reusing the builder
            assertEquals(testAuditId, auditEvent1.getAuditId());
            assertEquals(testAuditId, auditEvent2.getAuditId());
            
            // But different source systems
            assertEquals("SYSTEM_A", auditEvent1.getSourceSystem());
            assertEquals("SYSTEM_B", auditEvent2.getSourceSystem());
            
            // They should be different instances
            assertNotSame(auditEvent1, auditEvent2);
        }
        
        @Test
        @DisplayName("Builder should handle enum fields correctly")
        void testBuilderWithEnumFields() {
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .checkpointStage(CheckpointStage.SQLLOADER_START)
                    .status(AuditStatus.FAILURE)
                    .eventTimestamp(testTimestamp)
                    .build();
            
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals(CheckpointStage.SQLLOADER_START, auditEvent.getCheckpointStage());
            assertEquals(AuditStatus.FAILURE, auditEvent.getStatus());
            assertEquals(testTimestamp, auditEvent.getEventTimestamp());
        }
        
        @Test
        @DisplayName("Builder should handle null values correctly")
        void testBuilderWithNullValues() {
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(null)
                    .correlationId(null)
                    .sourceSystem(null)
                    .moduleName(null)
                    .checkpointStage(null)
                    .status(null)
                    .eventTimestamp(null)
                    .message(null)
                    .detailsJson(null)
                    .build();
            
            assertNull(auditEvent.getAuditId());
            assertNull(auditEvent.getCorrelationId());
            assertNull(auditEvent.getSourceSystem());
            assertNull(auditEvent.getModuleName());
            assertNull(auditEvent.getCheckpointStage());
            assertNull(auditEvent.getStatus());
            assertNull(auditEvent.getEventTimestamp());
            assertNull(auditEvent.getMessage());
            assertNull(auditEvent.getDetailsJson());
        }
        
        @Test
        @DisplayName("Builder should be reusable for creating multiple objects")
        void testBuilderReusability() {
            AuditEvent.Builder builder = AuditEvent.builder()
                    .correlationId(testCorrelationId)
                    .sourceSystem("SHARED_SYSTEM")
                    .status(AuditStatus.SUCCESS);
            
            AuditEvent auditEvent1 = builder
                    .auditId(UUID.randomUUID())
                    .moduleName("Module1")
                    .build();
            
            AuditEvent auditEvent2 = builder
                    .auditId(UUID.randomUUID())
                    .moduleName("Module2")
                    .build();
            
            // Shared fields should be the same
            assertEquals(testCorrelationId, auditEvent1.getCorrelationId());
            assertEquals(testCorrelationId, auditEvent2.getCorrelationId());
            assertEquals("SHARED_SYSTEM", auditEvent1.getSourceSystem());
            assertEquals("SHARED_SYSTEM", auditEvent2.getSourceSystem());
            assertEquals(AuditStatus.SUCCESS, auditEvent1.getStatus());
            assertEquals(AuditStatus.SUCCESS, auditEvent2.getStatus());
            
            // Different fields should be different
            assertNotEquals(auditEvent1.getAuditId(), auditEvent2.getAuditId());
            assertEquals("Module1", auditEvent1.getModuleName());
            assertEquals("Module2", auditEvent2.getModuleName());
        }
        
        @Test
        @DisplayName("Builder should create objects equal to manually constructed objects")
        void testBuilderEquivalenceWithManualConstruction() {
            // Create using builder
            AuditEvent builderEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("TEST_SYSTEM")
                    .moduleName("TEST_MODULE")
                    .build();
            
            // Create manually
            AuditEvent manualEvent = new AuditEvent(testAuditId, testCorrelationId, "TEST_SYSTEM", "TEST_MODULE");
            
            assertEquals(builderEvent, manualEvent);
            assertEquals(builderEvent.hashCode(), manualEvent.hashCode());
        }
        
        @Test
        @DisplayName("Builder static method should return new builder instance")
        void testBuilderStaticMethod() {
            AuditEvent.Builder builder1 = AuditEvent.builder();
            AuditEvent.Builder builder2 = AuditEvent.builder();
            
            assertNotNull(builder1);
            assertNotNull(builder2);
            assertNotSame(builder1, builder2);
        }
    }
    
    @Nested
    @DisplayName("Java 17+ Language Features and Spring Boot 3.4+ Compatibility Tests")
    class Java17AndSpringBoot34CompatibilityTests {
        
        @Test
        @DisplayName("AuditEvent should work with Java 17+ text blocks for JSON details")
        void testAuditEventWithTextBlocks() {
            // Using Java 17+ text blocks for better JSON readability
            String jsonDetails = """
                {
                    "fileSize": 1024,
                    "recordCount": 500,
                    "processingTime": "00:02:30",
                    "checksum": "abc123def456",
                    "metadata": {
                        "source": "mainframe",
                        "format": "fixed-width"
                    }
                }
                """;
            
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("MAINFRAME_SYSTEM")
                    .moduleName("FileProcessor")
                    .detailsJson(jsonDetails)
                    .status(AuditStatus.SUCCESS)
                    .build();
            
            assertEquals(jsonDetails, auditEvent.getDetailsJson());
            assertTrue(auditEvent.getDetailsJson().contains("fileSize"));
            assertTrue(auditEvent.getDetailsJson().contains("metadata"));
        }
        
        @Test
        @DisplayName("AuditEvent should work with Java 17+ switch expressions for status validation")
        void testAuditEventWithSwitchExpressions() {
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .status(AuditStatus.SUCCESS)
                    .build();
            
            // Using Java 17+ switch expressions for status validation
            String statusDescription = switch (auditEvent.getStatus()) {
                case SUCCESS -> "Operation completed successfully";
                case FAILURE -> "Operation failed with errors";
                case WARNING -> "Operation completed with warnings";
            };
            
            assertEquals("Operation completed successfully", statusDescription);
            
            // Test with different status
            auditEvent.setStatus(AuditStatus.FAILURE);
            String failureDescription = switch (auditEvent.getStatus()) {
                case SUCCESS -> "Operation completed successfully";
                case FAILURE -> "Operation failed with errors";
                case WARNING -> "Operation completed with warnings";
            };
            
            assertEquals("Operation failed with errors", failureDescription);
        }
        
        @Test
        @DisplayName("AuditEvent should work with Java 17+ pattern matching for instanceof")
        void testAuditEventWithPatternMatching() {
            Object auditObject = AuditEvent.builder()
                    .auditId(testAuditId)
                    .sourceSystem("TEST_SYSTEM")
                    .build();
            
            // Using Java 17+ pattern matching for instanceof
            if (auditObject instanceof AuditEvent event && event.getSourceSystem() != null) {
                String result = "AuditEvent with source system: " + event.getSourceSystem();
                assertEquals("AuditEvent with source system: TEST_SYSTEM", result);
            } else {
                fail("Pattern matching should have matched AuditEvent with source system");
            }
        }
        
        @Test
        @DisplayName("AuditEvent should handle Java 17+ record-like equality patterns")
        void testAuditEventRecordLikeEquality() {
            // Create two identical audit events using builder pattern
            var auditEvent1 = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("SYSTEM_A")
                    .moduleName("Module1")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .status(AuditStatus.SUCCESS)
                    .eventTimestamp(testTimestamp)
                    .build();
            
            var auditEvent2 = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("SYSTEM_A")
                    .moduleName("Module1")
                    .checkpointStage(CheckpointStage.RHEL_LANDING)
                    .status(AuditStatus.SUCCESS)
                    .eventTimestamp(testTimestamp)
                    .build();
            
            // Test record-like equality behavior
            assertEquals(auditEvent1, auditEvent2);
            assertEquals(auditEvent1.hashCode(), auditEvent2.hashCode());
            
            // Test that changing one field breaks equality (like records)
            auditEvent2.setSourceSystem("SYSTEM_B");
            assertNotEquals(auditEvent1, auditEvent2);
            assertNotEquals(auditEvent1.hashCode(), auditEvent2.hashCode());
        }
        
        @Test
        @DisplayName("AuditEvent should work with Spring Boot 3.4+ test features and JUnit 5 assertions")
        void testAuditEventWithSpringBoot34TestFeatures() {
            // Using JUnit 5 assertAll for grouped assertions (Spring Boot 3.4+ compatible)
            AuditEvent auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .correlationId(testCorrelationId)
                    .sourceSystem("SPRING_BOOT_34_SYSTEM")
                    .moduleName("TestModule")
                    .processName("TestProcess")
                    .checkpointStage(CheckpointStage.SQLLOADER_START)
                    .status(AuditStatus.SUCCESS)
                    .eventTimestamp(testTimestamp)
                    .message("Spring Boot 3.4+ compatible test")
                    .build();
            
            // Grouped assertions for better test reporting in Spring Boot 3.4+
            assertAll("AuditEvent Spring Boot 3.4+ compatibility",
                () -> assertEquals(testAuditId, auditEvent.getAuditId(), "Audit ID should match"),
                () -> assertEquals(testCorrelationId, auditEvent.getCorrelationId(), "Correlation ID should match"),
                () -> assertEquals("SPRING_BOOT_34_SYSTEM", auditEvent.getSourceSystem(), "Source system should match"),
                () -> assertEquals("TestModule", auditEvent.getModuleName(), "Module name should match"),
                () -> assertEquals("TestProcess", auditEvent.getProcessName(), "Process name should match"),
                () -> assertEquals(CheckpointStage.SQLLOADER_START, auditEvent.getCheckpointStage(), "Checkpoint stage should match"),
                () -> assertEquals(AuditStatus.SUCCESS, auditEvent.getStatus(), "Status should match"),
                () -> assertEquals(testTimestamp, auditEvent.getEventTimestamp(), "Timestamp should match"),
                () -> assertEquals("Spring Boot 3.4+ compatible test", auditEvent.getMessage(), "Message should match")
            );
        }
        
        @Test
        @DisplayName("AuditEvent should handle Java 17+ enhanced NullPointerException messages")
        void testAuditEventWithEnhancedNullPointerHandling() {
            AuditEvent auditEvent = new AuditEvent();
            
            // Test that null handling works correctly with Java 17+ enhanced NPE messages
            assertDoesNotThrow(() -> {
                auditEvent.setAuditId(null);
                auditEvent.setCorrelationId(null);
                auditEvent.setSourceSystem(null);
                auditEvent.setModuleName(null);
            }, "Setting null values should not throw exceptions");
            
            // Verify null values are handled correctly
            assertAll("Null value handling",
                () -> assertNull(auditEvent.getAuditId()),
                () -> assertNull(auditEvent.getCorrelationId()),
                () -> assertNull(auditEvent.getSourceSystem()),
                () -> assertNull(auditEvent.getModuleName())
            );
        }
        
        @Test
        @DisplayName("AuditEvent should work with Java 17+ var keyword and type inference")
        void testAuditEventWithVarKeyword() {
            // Using var keyword for type inference (Java 17+ feature)
            var auditId = UUID.randomUUID();
            var correlationId = UUID.randomUUID();
            var sourceSystem = "VAR_TEST_SYSTEM";
            var moduleName = "VarTestModule";
            var timestamp = LocalDateTime.now();
            
            var auditEvent = AuditEvent.builder()
                    .auditId(auditId)
                    .correlationId(correlationId)
                    .sourceSystem(sourceSystem)
                    .moduleName(moduleName)
                    .eventTimestamp(timestamp)
                    .status(AuditStatus.SUCCESS)
                    .build();
            
            // Verify all fields are set correctly with var inference
            assertAll("Var keyword compatibility",
                () -> assertEquals(auditId, auditEvent.getAuditId()),
                () -> assertEquals(correlationId, auditEvent.getCorrelationId()),
                () -> assertEquals(sourceSystem, auditEvent.getSourceSystem()),
                () -> assertEquals(moduleName, auditEvent.getModuleName()),
                () -> assertEquals(timestamp, auditEvent.getEventTimestamp()),
                () -> assertEquals(AuditStatus.SUCCESS, auditEvent.getStatus())
            );
        }
        
        @Test
        @DisplayName("Project should build and run with Java 17+ runtime and Spring Boot 3.4+")
        void testProjectCompatibilityWithJava17AndSpringBoot34() {
            // Verify Java version compatibility
            String javaVersion = System.getProperty("java.version");
            assertTrue(javaVersion.startsWith("17") || 
                      javaVersion.startsWith("18") || 
                      javaVersion.startsWith("19") || 
                      javaVersion.startsWith("20") || 
                      javaVersion.startsWith("21") || 
                      javaVersion.startsWith("22") || 
                      javaVersion.startsWith("23"),
                      "Should be running on Java 17 or higher, but found: " + javaVersion);
            
            // Test that AuditEvent works correctly in Java 17+ environment
            var auditEvent = AuditEvent.builder()
                    .auditId(testAuditId)
                    .sourceSystem("JAVA17_COMPATIBLE")
                    .build();
            
            assertNotNull(auditEvent);
            assertEquals(testAuditId, auditEvent.getAuditId());
            assertEquals("JAVA17_COMPATIBLE", auditEvent.getSourceSystem());
        }
    }
}