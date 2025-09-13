package com.company.audit.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditStatus enum
 */
class AuditStatusTest {

    @Test
    void testEnumValues() {
        // Verify all expected enum values exist
        AuditStatus[] values = AuditStatus.values();
        assertEquals(3, values.length, "AuditStatus should have exactly 3 values");
        
        // Verify specific enum values
        assertNotNull(AuditStatus.SUCCESS);
        assertNotNull(AuditStatus.FAILURE);
        assertNotNull(AuditStatus.WARNING);
    }

    @Test
    void testEnumValueOf() {
        // Test valueOf method works correctly
        assertEquals(AuditStatus.SUCCESS, AuditStatus.valueOf("SUCCESS"));
        assertEquals(AuditStatus.FAILURE, AuditStatus.valueOf("FAILURE"));
        assertEquals(AuditStatus.WARNING, AuditStatus.valueOf("WARNING"));
    }

    @Test
    void testEnumToString() {
        // Test toString method returns expected values
        assertEquals("SUCCESS", AuditStatus.SUCCESS.toString());
        assertEquals("FAILURE", AuditStatus.FAILURE.toString());
        assertEquals("WARNING", AuditStatus.WARNING.toString());
    }

    @Test
    void testEnumName() {
        // Test name method returns expected values
        assertEquals("SUCCESS", AuditStatus.SUCCESS.name());
        assertEquals("FAILURE", AuditStatus.FAILURE.name());
        assertEquals("WARNING", AuditStatus.WARNING.name());
    }

    @Test
    void testInvalidValueOf() {
        // Test that invalid enum values throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            AuditStatus.valueOf("INVALID");
        });
    }
}