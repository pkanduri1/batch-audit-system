package com.company.audit.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CheckpointStage enum
 */
class CheckpointStageTest {

    @Test
    @DisplayName("Should have all expected checkpoint stage values")
    void shouldHaveAllExpectedValues() {
        CheckpointStage[] stages = CheckpointStage.values();
        
        assertEquals(5, stages.length, "Should have exactly 5 checkpoint stages");
        
        // Verify all expected values exist
        assertNotNull(CheckpointStage.RHEL_LANDING);
        assertNotNull(CheckpointStage.SQLLOADER_START);
        assertNotNull(CheckpointStage.SQLLOADER_COMPLETE);
        assertNotNull(CheckpointStage.LOGIC_APPLIED);
        assertNotNull(CheckpointStage.FILE_GENERATED);
    }

    @ParameterizedTest
    @EnumSource(CheckpointStage.class)
    @DisplayName("Should return correct string value for each checkpoint stage")
    void shouldReturnCorrectStringValue(CheckpointStage stage) {
        String value = stage.getValue();
        
        assertNotNull(value, "Value should not be null");
        assertFalse(value.isEmpty(), "Value should not be empty");
        assertEquals(stage.name(), value, "Value should match enum name");
    }

    @Test
    @DisplayName("Should return correct values for specific checkpoint stages")
    void shouldReturnCorrectValuesForSpecificStages() {
        assertEquals("RHEL_LANDING", CheckpointStage.RHEL_LANDING.getValue());
        assertEquals("SQLLOADER_START", CheckpointStage.SQLLOADER_START.getValue());
        assertEquals("SQLLOADER_COMPLETE", CheckpointStage.SQLLOADER_COMPLETE.getValue());
        assertEquals("LOGIC_APPLIED", CheckpointStage.LOGIC_APPLIED.getValue());
        assertEquals("FILE_GENERATED", CheckpointStage.FILE_GENERATED.getValue());
    }

    @Test
    @DisplayName("Should convert from string value to enum correctly")
    void shouldConvertFromStringValueToEnum() {
        assertEquals(CheckpointStage.RHEL_LANDING, CheckpointStage.fromValue("RHEL_LANDING"));
        assertEquals(CheckpointStage.SQLLOADER_START, CheckpointStage.fromValue("SQLLOADER_START"));
        assertEquals(CheckpointStage.SQLLOADER_COMPLETE, CheckpointStage.fromValue("SQLLOADER_COMPLETE"));
        assertEquals(CheckpointStage.LOGIC_APPLIED, CheckpointStage.fromValue("LOGIC_APPLIED"));
        assertEquals(CheckpointStage.FILE_GENERATED, CheckpointStage.fromValue("FILE_GENERATED"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid string value")
    void shouldThrowExceptionForInvalidStringValue() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CheckpointStage.fromValue("INVALID_STAGE")
        );
        
        assertEquals("Unknown checkpoint stage: INVALID_STAGE", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null string value")
    void shouldThrowExceptionForNullStringValue() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CheckpointStage.fromValue(null)
        );
    }

    @ParameterizedTest
    @EnumSource(CheckpointStage.class)
    @DisplayName("Should return correct toString representation")
    void shouldReturnCorrectToStringRepresentation(CheckpointStage stage) {
        assertEquals(stage.getValue(), stage.toString());
    }

    @Test
    @DisplayName("Should maintain enum ordering as defined")
    void shouldMaintainEnumOrdering() {
        CheckpointStage[] stages = CheckpointStage.values();
        
        assertEquals(CheckpointStage.RHEL_LANDING, stages[0]);
        assertEquals(CheckpointStage.SQLLOADER_START, stages[1]);
        assertEquals(CheckpointStage.SQLLOADER_COMPLETE, stages[2]);
        assertEquals(CheckpointStage.LOGIC_APPLIED, stages[3]);
        assertEquals(CheckpointStage.FILE_GENERATED, stages[4]);
    }

    @Test
    @DisplayName("Should support valueOf method")
    void shouldSupportValueOfMethod() {
        assertEquals(CheckpointStage.RHEL_LANDING, CheckpointStage.valueOf("RHEL_LANDING"));
        assertEquals(CheckpointStage.SQLLOADER_START, CheckpointStage.valueOf("SQLLOADER_START"));
        assertEquals(CheckpointStage.SQLLOADER_COMPLETE, CheckpointStage.valueOf("SQLLOADER_COMPLETE"));
        assertEquals(CheckpointStage.LOGIC_APPLIED, CheckpointStage.valueOf("LOGIC_APPLIED"));
        assertEquals(CheckpointStage.FILE_GENERATED, CheckpointStage.valueOf("FILE_GENERATED"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid valueOf")
    void shouldThrowExceptionForInvalidValueOf() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CheckpointStage.valueOf("INVALID_STAGE")
        );
    }
}