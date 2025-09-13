package com.company.audit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify the Spring Boot application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class BatchAuditApplicationTest {

    @Test
    void contextLoads() {
        // This test will pass if the Spring application context loads successfully
    }
}