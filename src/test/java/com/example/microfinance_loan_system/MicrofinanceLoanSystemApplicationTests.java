package com.example.microfinance_loan_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic application context loads test.
 * DB connection is not required — datasource auto-config is disabled for this test.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "jwt.secret=test-secret-key-that-is-long-enough-for-hs256",
    "jwt.expiration-ms=86400000"
})
class MicrofinanceLoanSystemApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts without errors
    }
}
