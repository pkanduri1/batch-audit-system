package com.company.audit.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AuditDetails class focusing on JSON serialization, 
 * builder pattern functionality, and field validation with Spring Boot 3.4+ compatibility.
 * Tests Jackson 2.15+ features and Java 17+ language enhancements.
 * 
 * Requirements addressed:
 * - 2.4: Store details_json field for audit events
 */
@DisplayName("AuditDetails Tests")
class AuditDetailsTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize AuditDetails to JSON with all fields")
        void shouldSerializeToJsonWithAllFields() throws JsonProcessingException {
            // Given
            Map<String, Object> ruleInput = new HashMap<>();
            ruleInput.put("accountNumber", "12345");
            ruleInput.put("amount", 100.50);

            Map<String, Object> ruleOutput = new HashMap<>();
            ruleOutput.put("validationResult", "PASSED");
            ruleOutput.put("processedAmount", 100.50);

            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("abc123def456")
                    .rowsRead(100L)
                    .rowsLoaded(95L)
                    .rowsRejected(5L)
                    .recordCount(95L)
                    .recordCountBefore(100L)
                    .recordCountAfter(95L)
                    .controlTotalDebits(new BigDecimal("1000.00"))
                    .controlTotalCredits(new BigDecimal("950.00"))
                    .controlTotalAmount(new BigDecimal("50.00"))
                    .ruleApplied("ValidationRule")
                    .entityIdentifier("ACC-12345")
                    .transformationDetails("Applied business validation")
                    .ruleInput(ruleInput)
                    .ruleOutput(ruleOutput)
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"fileSizeBytes\":1024"));
            assertTrue(json.contains("\"fileHashSha256\":\"abc123def456\""));
            assertTrue(json.contains("\"rowsRead\":100"));
            assertTrue(json.contains("\"rowsLoaded\":95"));
            assertTrue(json.contains("\"rowsRejected\":5"));
            assertTrue(json.contains("\"recordCount\":95"));
            assertTrue(json.contains("\"controlTotalDebits\":1000.00"));
            assertTrue(json.contains("\"ruleApplied\":\"ValidationRule\""));
            assertTrue(json.contains("\"entityIdentifier\":\"ACC-12345\""));
            assertTrue(json.contains("\"ruleInput\":{"));
            assertTrue(json.contains("\"ruleOutput\":{"));
        }

        @Test
        @DisplayName("Should serialize AuditDetails to JSON excluding null fields")
        void shouldSerializeToJsonExcludingNullFields() throws JsonProcessingException {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .rowsRead(100L)
                    // Other fields are null
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"fileSizeBytes\":1024"));
            assertTrue(json.contains("\"rowsRead\":100"));
            // Null fields should not be included due to @JsonInclude(JsonInclude.Include.NON_NULL)
            assertFalse(json.contains("\"fileHashSha256\""));
            assertFalse(json.contains("\"rowsLoaded\""));
            assertFalse(json.contains("\"controlTotalDebits\""));
            assertFalse(json.contains("\"ruleInput\""));
        }

        @Test
        @DisplayName("Should deserialize JSON to AuditDetails using builder")
        void shouldDeserializeJsonToAuditDetails() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "fileSizeBytes": 2048,
                    "fileHashSha256": "def789ghi012",
                    "rowsRead": 200,
                    "rowsLoaded": 190,
                    "rowsRejected": 10,
                    "recordCount": 190,
                    "controlTotalAmount": 150.75,
                    "ruleApplied": "TransformationRule",
                    "entityIdentifier": "ACC-67890",
                    "ruleInput": {
                        "inputField": "testValue"
                    }
                }
                """;

            // When
            AuditDetails auditDetails = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(auditDetails);
            assertEquals(2048L, auditDetails.getFileSizeBytes());
            assertEquals("def789ghi012", auditDetails.getFileHashSha256());
            assertEquals(200L, auditDetails.getRowsRead());
            assertEquals(190L, auditDetails.getRowsLoaded());
            assertEquals(10L, auditDetails.getRowsRejected());
            assertEquals(190L, auditDetails.getRecordCount());
            assertEquals(new BigDecimal("150.75"), auditDetails.getControlTotalAmount());
            assertEquals("TransformationRule", auditDetails.getRuleApplied());
            assertEquals("ACC-67890", auditDetails.getEntityIdentifier());
            assertNotNull(auditDetails.getRuleInput());
            assertEquals("testValue", auditDetails.getRuleInput().get("inputField"));
        }

        @Test
        @DisplayName("Should handle empty JSON object")
        void shouldHandleEmptyJsonObject() throws JsonProcessingException {
            // Given
            String json = "{}";

            // When
            AuditDetails auditDetails = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(auditDetails);
            assertNull(auditDetails.getFileSizeBytes());
            assertNull(auditDetails.getFileHashSha256());
            assertNull(auditDetails.getRowsRead());
            assertNull(auditDetails.getRuleInput());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should create AuditDetails using builder pattern")
        void shouldCreateAuditDetailsUsingBuilder() {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(512L)
                    .fileHashSha256("hash123")
                    .rowsRead(50L)
                    .rowsLoaded(45L)
                    .rowsRejected(5L)
                    .recordCount(45L)
                    .controlTotalDebits(new BigDecimal("500.00"))
                    .ruleApplied("TestRule")
                    .entityIdentifier("TEST-123")
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(512L, auditDetails.getFileSizeBytes());
            assertEquals("hash123", auditDetails.getFileHashSha256());
            assertEquals(50L, auditDetails.getRowsRead());
            assertEquals(45L, auditDetails.getRowsLoaded());
            assertEquals(5L, auditDetails.getRowsRejected());
            assertEquals(45L, auditDetails.getRecordCount());
            assertEquals(new BigDecimal("500.00"), auditDetails.getControlTotalDebits());
            assertEquals("TestRule", auditDetails.getRuleApplied());
            assertEquals("TEST-123", auditDetails.getEntityIdentifier());
        }

        @Test
        @DisplayName("Should create AuditDetails with partial fields using builder")
        void shouldCreateAuditDetailsWithPartialFields() {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(256L)
                    .ruleApplied("PartialRule")
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(256L, auditDetails.getFileSizeBytes());
            assertEquals("PartialRule", auditDetails.getRuleApplied());
            assertNull(auditDetails.getFileHashSha256());
            assertNull(auditDetails.getRowsRead());
            assertNull(auditDetails.getControlTotalDebits());
        }

        @Test
        @DisplayName("Should create empty AuditDetails using builder")
        void shouldCreateEmptyAuditDetailsUsingBuilder() {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder().build();

            // Then
            assertNotNull(auditDetails);
            assertNull(auditDetails.getFileSizeBytes());
            assertNull(auditDetails.getFileHashSha256());
            assertNull(auditDetails.getRowsRead());
            assertNull(auditDetails.getRuleInput());
        }

        @Test
        @DisplayName("Should support fluent builder chaining")
        void shouldSupportFluentBuilderChaining() {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("fluent123")
                    .rowsRead(100L)
                    .rowsLoaded(95L)
                    .recordCount(95L)
                    .ruleApplied("FluentRule")
                    .entityIdentifier("FLUENT-456")
                    .transformationDetails("Fluent transformation")
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(1024L, auditDetails.getFileSizeBytes());
            assertEquals("fluent123", auditDetails.getFileHashSha256());
            assertEquals(100L, auditDetails.getRowsRead());
            assertEquals(95L, auditDetails.getRowsLoaded());
            assertEquals(95L, auditDetails.getRecordCount());
            assertEquals("FluentRule", auditDetails.getRuleApplied());
            assertEquals("FLUENT-456", auditDetails.getEntityIdentifier());
            assertEquals("Fluent transformation", auditDetails.getTransformationDetails());
        }
    }

    @Nested
    @DisplayName("Jackson 2.15+ Features Tests")
    class Jackson215FeaturesTests {

        @Test
        @DisplayName("Should respect JsonPropertyOrder annotation")
        void shouldRespectJsonPropertyOrder() throws JsonProcessingException {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .ruleApplied("OrderTest")
                    .fileSizeBytes(1024L)
                    .rowsRead(100L)
                    .entityIdentifier("ORDER-123")
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);

            // Then
            assertNotNull(json);
            // Verify that fileSizeBytes appears before rowsRead (as per @JsonPropertyOrder)
            int fileSizeIndex = json.indexOf("\"fileSizeBytes\"");
            int rowsReadIndex = json.indexOf("\"rowsRead\"");
            int ruleAppliedIndex = json.indexOf("\"ruleApplied\"");
            int entityIdentifierIndex = json.indexOf("\"entityIdentifier\"");
            
            assertTrue(fileSizeIndex < rowsReadIndex, "fileSizeBytes should appear before rowsRead");
            assertTrue(rowsReadIndex < ruleAppliedIndex, "rowsRead should appear before ruleApplied");
            assertTrue(ruleAppliedIndex < entityIdentifierIndex, "ruleApplied should appear before entityIdentifier");
        }

        @Test
        @DisplayName("Should ignore unknown properties during deserialization")
        void shouldIgnoreUnknownProperties() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "fileSizeBytes": 1024,
                    "unknownField": "shouldBeIgnored",
                    "anotherUnknownField": 12345,
                    "rowsRead": 100
                }
                """;

            // When & Then (should not throw exception due to @JsonIgnoreProperties(ignoreUnknown = true))
            AuditDetails auditDetails = objectMapper.readValue(json, AuditDetails.class);

            assertNotNull(auditDetails);
            assertEquals(1024L, auditDetails.getFileSizeBytes());
            assertEquals(100L, auditDetails.getRowsRead());
        }

        @Test
        @DisplayName("Should handle Jackson 2.15+ @JsonDeserialize with builder")
        void shouldHandleJsonDeserializeWithBuilder() throws JsonProcessingException {
            // Given
            String json = """
                {
                    "fileSizeBytes": 4096,
                    "fileHashSha256": "jackson215test",
                    "controlTotalAmount": 999.99,
                    "ruleApplied": "Jackson215Rule"
                }
                """;

            // When
            AuditDetails auditDetails = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(auditDetails);
            assertEquals(4096L, auditDetails.getFileSizeBytes());
            assertEquals("jackson215test", auditDetails.getFileHashSha256());
            assertEquals(new BigDecimal("999.99"), auditDetails.getControlTotalAmount());
            assertEquals("Jackson215Rule", auditDetails.getRuleApplied());
        }
    }

    @Nested
    @DisplayName("Field Validation and Constraints Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should handle null values for all fields")
        void shouldHandleNullValuesForAllFields() {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(null)
                    .fileHashSha256(null)
                    .rowsRead(null)
                    .rowsLoaded(null)
                    .rowsRejected(null)
                    .recordCount(null)
                    .recordCountBefore(null)
                    .recordCountAfter(null)
                    .controlTotalDebits(null)
                    .controlTotalCredits(null)
                    .controlTotalAmount(null)
                    .ruleInput(null)
                    .ruleOutput(null)
                    .ruleApplied(null)
                    .entityIdentifier(null)
                    .transformationDetails(null)
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertNull(auditDetails.getFileSizeBytes());
            assertNull(auditDetails.getFileHashSha256());
            assertNull(auditDetails.getRowsRead());
            assertNull(auditDetails.getRowsLoaded());
            assertNull(auditDetails.getRowsRejected());
            assertNull(auditDetails.getRecordCount());
            assertNull(auditDetails.getRecordCountBefore());
            assertNull(auditDetails.getRecordCountAfter());
            assertNull(auditDetails.getControlTotalDebits());
            assertNull(auditDetails.getControlTotalCredits());
            assertNull(auditDetails.getControlTotalAmount());
            assertNull(auditDetails.getRuleInput());
            assertNull(auditDetails.getRuleOutput());
            assertNull(auditDetails.getRuleApplied());
            assertNull(auditDetails.getEntityIdentifier());
            assertNull(auditDetails.getTransformationDetails());
        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, 1000L, Long.MAX_VALUE})
        @DisplayName("Should handle valid positive long values for numeric fields")
        void shouldHandleValidPositiveLongValues(Long value) {
            // Given & When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(value)
                    .rowsRead(value)
                    .rowsLoaded(value)
                    .rowsRejected(value)
                    .recordCount(value)
                    .recordCountBefore(value)
                    .recordCountAfter(value)
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(value, auditDetails.getFileSizeBytes());
            assertEquals(value, auditDetails.getRowsRead());
            assertEquals(value, auditDetails.getRowsLoaded());
            assertEquals(value, auditDetails.getRowsRejected());
            assertEquals(value, auditDetails.getRecordCount());
            assertEquals(value, auditDetails.getRecordCountBefore());
            assertEquals(value, auditDetails.getRecordCountAfter());
        }

        @Test
        @DisplayName("Should handle BigDecimal precision for control totals")
        void shouldHandleBigDecimalPrecisionForControlTotals() {
            // Given
            BigDecimal preciseDebit = new BigDecimal("12345.6789");
            BigDecimal preciseCredit = new BigDecimal("98765.4321");
            BigDecimal preciseAmount = new BigDecimal("0.0001");

            // When
            AuditDetails auditDetails = AuditDetails.builder()
                    .controlTotalDebits(preciseDebit)
                    .controlTotalCredits(preciseCredit)
                    .controlTotalAmount(preciseAmount)
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(preciseDebit, auditDetails.getControlTotalDebits());
            assertEquals(preciseCredit, auditDetails.getControlTotalCredits());
            assertEquals(preciseAmount, auditDetails.getControlTotalAmount());
        }

        @Test
        @DisplayName("Should handle empty and complex Map structures for rule input/output")
        void shouldHandleComplexMapStructures() {
            // Given
            Map<String, Object> emptyMap = new HashMap<>();
            Map<String, Object> complexMap = new HashMap<>();
            complexMap.put("stringValue", "test");
            complexMap.put("intValue", 42);
            complexMap.put("doubleValue", 3.14159);
            complexMap.put("booleanValue", true);
            complexMap.put("listValue", Arrays.asList("item1", "item2", "item3"));
            complexMap.put("nestedMap", Map.of("nested", "value"));

            // When
            AuditDetails auditDetails = AuditDetails.builder()
                    .ruleInput(emptyMap)
                    .ruleOutput(complexMap)
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertNotNull(auditDetails.getRuleInput());
            assertTrue(auditDetails.getRuleInput().isEmpty());
            assertNotNull(auditDetails.getRuleOutput());
            assertEquals("test", auditDetails.getRuleOutput().get("stringValue"));
            assertEquals(42, auditDetails.getRuleOutput().get("intValue"));
            assertEquals(3.14159, auditDetails.getRuleOutput().get("doubleValue"));
            assertEquals(true, auditDetails.getRuleOutput().get("booleanValue"));
            assertTrue(auditDetails.getRuleOutput().get("listValue") instanceof List);
            assertTrue(auditDetails.getRuleOutput().get("nestedMap") instanceof Map);
        }

        @Test
        @DisplayName("Should handle special characters and Unicode in string fields")
        void shouldHandleSpecialCharactersAndUnicode() {
            // Given
            String specialHash = "abc123!@#$%^&*()_+-=[]{}|;':\",./<>?";
            String unicodeRule = "ValidationRule_测试_🔍";
            String unicodeEntity = "ENTITY_测试_123";
            String unicodeTransformation = "Transformation with émojis 🚀 and spëcial chars";

            // When
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileHashSha256(specialHash)
                    .ruleApplied(unicodeRule)
                    .entityIdentifier(unicodeEntity)
                    .transformationDetails(unicodeTransformation)
                    .build();

            // Then
            assertNotNull(auditDetails);
            assertEquals(specialHash, auditDetails.getFileHashSha256());
            assertEquals(unicodeRule, auditDetails.getRuleApplied());
            assertEquals(unicodeEntity, auditDetails.getEntityIdentifier());
            assertEquals(unicodeTransformation, auditDetails.getTransformationDetails());
        }
    }

    @Nested
    @DisplayName("Spring Boot 3.4+ Compatibility Tests")
    class SpringBoot34CompatibilityTests {

        @Test
        @DisplayName("Should serialize with standard Jackson configuration")
        void shouldSerializeWithStandardJacksonConfig() throws JsonProcessingException {
            // Given - Using standard ObjectMapper configuration
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .controlTotalAmount(new BigDecimal("123.45"))
                    .ruleApplied("StandardJacksonTest")
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);

            // Then
            assertNotNull(json);
            assertTrue(json.contains("\"fileSizeBytes\":1024"));
            assertTrue(json.contains("\"controlTotalAmount\":123.45"));
            assertTrue(json.contains("\"ruleApplied\":\"StandardJacksonTest\""));
            // Verify null fields are excluded (default behavior)
            assertFalse(json.contains("\"fileHashSha256\""));
            assertFalse(json.contains("\"rowsRead\""));
        }

        @Test
        @DisplayName("Should handle malformed JSON gracefully")
        void shouldHandleMalformedJsonGracefully() {
            // Given
            String malformedJson = """
                {
                    "fileSizeBytes": "not_a_number",
                    "rowsRead": 100,
                    "validField": "validValue"
                }
                """;

            // When & Then
            assertThrows(JsonProcessingException.class, () -> {
                objectMapper.readValue(malformedJson, AuditDetails.class);
            });
        }

        @Test
        @DisplayName("Should preserve field order in JSON output")
        void shouldPreserveFieldOrderInJsonOutput() throws JsonProcessingException {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .transformationDetails("Last field")
                    .fileSizeBytes(1024L)
                    .ruleApplied("Middle field")
                    .fileHashSha256("Second field")
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);

            // Then
            assertNotNull(json);
            // Verify field order matches @JsonPropertyOrder annotation
            int fileSizeIndex = json.indexOf("\"fileSizeBytes\"");
            int hashIndex = json.indexOf("\"fileHashSha256\"");
            int ruleIndex = json.indexOf("\"ruleApplied\"");
            int transformationIndex = json.indexOf("\"transformationDetails\"");

            assertTrue(fileSizeIndex < hashIndex, "fileSizeBytes should come before fileHashSha256");
            assertTrue(ruleIndex < transformationIndex, "ruleApplied should come before transformationDetails");
        }

        @Test
        @DisplayName("Should work with standard ObjectMapper configuration")
        void shouldWorkWithStandardObjectMapperConfiguration() {
            // Given - This test verifies standard ObjectMapper configuration
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .ruleApplied("StandardObjectMapperTest")
                    .entityIdentifier("TEST-STANDARD-MAPPER")
                    .build();

            // When & Then
            assertNotNull(auditDetails);
            assertEquals(1024L, auditDetails.getFileSizeBytes());
            assertEquals("StandardObjectMapperTest", auditDetails.getRuleApplied());
            assertEquals("TEST-STANDARD-MAPPER", auditDetails.getEntityIdentifier());
            
            // Verify the ObjectMapper is available for testing
            assertNotNull(objectMapper, "ObjectMapper should be available for testing");
        }
    }

    @Nested
    @DisplayName("Java 17+ Language Features Tests")
    class Java17FeaturesTests {

        @Test
        @DisplayName("Should work with Java 17+ text blocks in JSON")
        void shouldWorkWithTextBlocksInJson() throws JsonProcessingException {
            // Given - Using Java 17+ text blocks for JSON
            String jsonTextBlock = """
                {
                    "fileSizeBytes": 2048,
                    "fileHashSha256": "textblock123hash",
                    "rowsRead": 150,
                    "ruleApplied": "TextBlockRule",
                    "transformationDetails": "Multi-line\\ntransformation\\ndetails"
                }
                """;

            // When
            AuditDetails auditDetails = objectMapper.readValue(jsonTextBlock, AuditDetails.class);

            // Then
            assertNotNull(auditDetails);
            assertEquals(2048L, auditDetails.getFileSizeBytes());
            assertEquals("textblock123hash", auditDetails.getFileHashSha256());
            assertEquals(150L, auditDetails.getRowsRead());
            assertEquals("TextBlockRule", auditDetails.getRuleApplied());
            assertTrue(auditDetails.getTransformationDetails().contains("Multi-line"));
        }

        @Test
        @DisplayName("Should support pattern matching in validation logic")
        void shouldSupportPatternMatchingInValidation() {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .rowsRead(100L)
                    .rowsLoaded(95L)
                    .rowsRejected(5L)
                    .build();

            // When & Then - Using Java 17+ pattern matching concepts
            Object fileSizeObj = auditDetails.getFileSizeBytes();
            if (fileSizeObj instanceof Long fileSize) {
                assertTrue(fileSize > 0, "File size should be positive");
                assertEquals(1024L, fileSize);
            }

            // Verify SQL loader statistics consistency
            Long totalProcessed = auditDetails.getRowsLoaded() + auditDetails.getRowsRejected();
            assertEquals(auditDetails.getRowsRead(), totalProcessed, 
                "Rows read should equal loaded + rejected");
        }

        @Test
        @DisplayName("Should handle record-like data structures in rule input/output")
        void shouldHandleRecordLikeDataStructures() throws JsonProcessingException {
            // Given - Simulating record-like structures with Maps
            Map<String, Object> recordLikeInput = Map.of(
                "accountId", "ACC-123",
                "amount", new BigDecimal("1000.00"),
                "currency", "USD",
                "timestamp", "2024-01-15T10:30:00Z"
            );

            Map<String, Object> recordLikeOutput = Map.of(
                "validationResult", "PASSED",
                "processedAmount", new BigDecimal("1000.00"),
                "fees", new BigDecimal("5.00"),
                "netAmount", new BigDecimal("995.00")
            );

            AuditDetails auditDetails = AuditDetails.builder()
                    .ruleInput(recordLikeInput)
                    .ruleOutput(recordLikeOutput)
                    .ruleApplied("RecordValidationRule")
                    .entityIdentifier("ACC-123")
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);
            AuditDetails deserialized = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(deserialized);
            assertNotNull(deserialized.getRuleInput());
            assertNotNull(deserialized.getRuleOutput());
            assertEquals("ACC-123", deserialized.getRuleInput().get("accountId"));
            assertEquals("PASSED", deserialized.getRuleOutput().get("validationResult"));
            assertEquals(995.0, ((Number) deserialized.getRuleOutput().get("netAmount")).doubleValue());
        }
    }

    @Nested
    @DisplayName("Equals, HashCode, and ToString Tests")
    class ObjectMethodsTests {

        @Test
        @DisplayName("Should implement equals correctly")
        void shouldImplementEqualsCorrectly() {
            // Given
            Map<String, Object> ruleData = Map.of("key", "value");
            
            AuditDetails auditDetails1 = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("hash123")
                    .rowsRead(100L)
                    .ruleInput(ruleData)
                    .build();

            AuditDetails auditDetails2 = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("hash123")
                    .rowsRead(100L)
                    .ruleInput(ruleData)
                    .build();

            AuditDetails auditDetails3 = AuditDetails.builder()
                    .fileSizeBytes(2048L)
                    .fileHashSha256("hash123")
                    .rowsRead(100L)
                    .ruleInput(ruleData)
                    .build();

            // Then
            assertEquals(auditDetails1, auditDetails2);
            assertNotEquals(auditDetails1, auditDetails3);
            assertNotEquals(auditDetails1, null);
            assertNotEquals(auditDetails1, "not an AuditDetails");
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void shouldImplementHashCodeCorrectly() {
            // Given
            AuditDetails auditDetails1 = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("hash123")
                    .build();

            AuditDetails auditDetails2 = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("hash123")
                    .build();

            // Then
            assertEquals(auditDetails1.hashCode(), auditDetails2.hashCode());
        }

        @Test
        @DisplayName("Should implement toString correctly")
        void shouldImplementToStringCorrectly() {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(1024L)
                    .fileHashSha256("hash123")
                    .rowsRead(100L)
                    .ruleApplied("TestRule")
                    .build();

            // When
            String toString = auditDetails.toString();

            // Then
            assertNotNull(toString);
            assertTrue(toString.contains("AuditDetails{"));
            assertTrue(toString.contains("fileSizeBytes=1024"));
            assertTrue(toString.contains("fileHashSha256='hash123'"));
            assertTrue(toString.contains("rowsRead=100"));
            assertTrue(toString.contains("ruleApplied='TestRule'"));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should round-trip serialize and deserialize complex AuditDetails")
        void shouldRoundTripSerializeAndDeserialize() throws JsonProcessingException {
            // Given
            Map<String, Object> complexRuleInput = new HashMap<>();
            complexRuleInput.put("nestedObject", Map.of("key1", "value1", "key2", 42));
            complexRuleInput.put("arrayField", new String[]{"item1", "item2", "item3"});
            complexRuleInput.put("numericField", 123.45);

            Map<String, Object> complexRuleOutput = new HashMap<>();
            complexRuleOutput.put("result", "SUCCESS");
            complexRuleOutput.put("processedItems", 3);

            AuditDetails original = AuditDetails.builder()
                    .fileSizeBytes(4096L)
                    .fileHashSha256("complex123hash456")
                    .rowsRead(1000L)
                    .rowsLoaded(950L)
                    .rowsRejected(50L)
                    .recordCount(950L)
                    .recordCountBefore(1000L)
                    .recordCountAfter(950L)
                    .controlTotalDebits(new BigDecimal("10000.50"))
                    .controlTotalCredits(new BigDecimal("9500.25"))
                    .controlTotalAmount(new BigDecimal("500.25"))
                    .ruleApplied("ComplexValidationRule")
                    .entityIdentifier("COMPLEX-789")
                    .transformationDetails("Complex transformation with multiple steps")
                    .ruleInput(complexRuleInput)
                    .ruleOutput(complexRuleOutput)
                    .build();

            // When
            String json = objectMapper.writeValueAsString(original);
            AuditDetails deserialized = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(deserialized);
            assertEquals(original.getFileSizeBytes(), deserialized.getFileSizeBytes());
            assertEquals(original.getFileHashSha256(), deserialized.getFileHashSha256());
            assertEquals(original.getRowsRead(), deserialized.getRowsRead());
            assertEquals(original.getRowsLoaded(), deserialized.getRowsLoaded());
            assertEquals(original.getRowsRejected(), deserialized.getRowsRejected());
            assertEquals(original.getRecordCount(), deserialized.getRecordCount());
            assertEquals(original.getRecordCountBefore(), deserialized.getRecordCountBefore());
            assertEquals(original.getRecordCountAfter(), deserialized.getRecordCountAfter());
            assertEquals(original.getControlTotalDebits(), deserialized.getControlTotalDebits());
            assertEquals(original.getControlTotalCredits(), deserialized.getControlTotalCredits());
            assertEquals(original.getControlTotalAmount(), deserialized.getControlTotalAmount());
            assertEquals(original.getRuleApplied(), deserialized.getRuleApplied());
            assertEquals(original.getEntityIdentifier(), deserialized.getEntityIdentifier());
            assertEquals(original.getTransformationDetails(), deserialized.getTransformationDetails());
            assertNotNull(deserialized.getRuleInput());
            assertNotNull(deserialized.getRuleOutput());
            assertEquals("SUCCESS", deserialized.getRuleOutput().get("result"));
        }

        @Test
        @DisplayName("Should validate data consistency across all fields")
        void shouldValidateDataConsistencyAcrossAllFields() {
            // Given
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(2048L)
                    .fileHashSha256("consistency123test")
                    .rowsRead(200L)
                    .rowsLoaded(180L)
                    .rowsRejected(20L)
                    .recordCount(180L)
                    .recordCountBefore(200L)
                    .recordCountAfter(180L)
                    .controlTotalDebits(new BigDecimal("5000.00"))
                    .controlTotalCredits(new BigDecimal("4800.00"))
                    .controlTotalAmount(new BigDecimal("200.00"))
                    .ruleApplied("ConsistencyValidationRule")
                    .entityIdentifier("CONSISTENCY-TEST")
                    .transformationDetails("Data consistency validation test")
                    .build();

            // When & Then - Validate data consistency
            assertNotNull(auditDetails);
            
            // Verify SQL loader statistics consistency
            Long totalProcessed = auditDetails.getRowsLoaded() + auditDetails.getRowsRejected();
            assertEquals(auditDetails.getRowsRead(), totalProcessed, 
                "Rows read should equal loaded + rejected");
            
            // Verify record count consistency
            assertEquals(auditDetails.getRowsLoaded(), auditDetails.getRecordCount(),
                "Record count should match rows loaded");
            assertEquals(auditDetails.getRecordCountBefore(), auditDetails.getRowsRead(),
                "Record count before should match rows read");
            assertEquals(auditDetails.getRecordCountAfter(), auditDetails.getRowsLoaded(),
                "Record count after should match rows loaded");
            
            // Verify control total consistency
            BigDecimal expectedDifference = auditDetails.getControlTotalDebits()
                .subtract(auditDetails.getControlTotalCredits());
            assertEquals(expectedDifference, auditDetails.getControlTotalAmount(),
                "Control total amount should equal debits minus credits");
        }

        @Test
        @DisplayName("Should handle edge cases and boundary values")
        void shouldHandleEdgeCasesAndBoundaryValues() throws JsonProcessingException {
            // Given - Edge case values
            AuditDetails auditDetails = AuditDetails.builder()
                    .fileSizeBytes(0L)  // Minimum file size
                    .fileHashSha256("")  // Empty hash
                    .rowsRead(Long.MAX_VALUE)  // Maximum long value
                    .rowsLoaded(0L)  // No rows loaded
                    .rowsRejected(Long.MAX_VALUE)  // All rows rejected
                    .recordCount(0L)  // No records
                    .controlTotalAmount(new BigDecimal("0.00"))  // Zero amount
                    .ruleApplied("")  // Empty rule name
                    .entityIdentifier("")  // Empty entity identifier
                    .transformationDetails("")  // Empty transformation details
                    .ruleInput(new HashMap<>())  // Empty input map
                    .ruleOutput(new HashMap<>())  // Empty output map
                    .build();

            // When
            String json = objectMapper.writeValueAsString(auditDetails);
            AuditDetails deserialized = objectMapper.readValue(json, AuditDetails.class);

            // Then
            assertNotNull(deserialized);
            assertEquals(0L, deserialized.getFileSizeBytes());
            assertEquals("", deserialized.getFileHashSha256());
            assertEquals(Long.MAX_VALUE, deserialized.getRowsRead());
            assertEquals(0L, deserialized.getRowsLoaded());
            assertEquals(Long.MAX_VALUE, deserialized.getRowsRejected());
            assertEquals(0L, deserialized.getRecordCount());
            assertEquals(new BigDecimal("0.00"), deserialized.getControlTotalAmount());
            assertEquals("", deserialized.getRuleApplied());
            assertEquals("", deserialized.getEntityIdentifier());
            assertEquals("", deserialized.getTransformationDetails());
            assertNotNull(deserialized.getRuleInput());
            assertTrue(deserialized.getRuleInput().isEmpty());
            assertNotNull(deserialized.getRuleOutput());
            assertTrue(deserialized.getRuleOutput().isEmpty());
        }
    }
}