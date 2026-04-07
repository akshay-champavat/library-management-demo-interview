package com.library.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring application context loads correctly.
 * Run with: mvn test
 */
@SpringBootTest
class LibraryApiApplicationTests {

    @Test
    void contextLoads() {
        // If this test passes, the entire Spring context (JPA, H2, Async, etc.)
        // initialised successfully. It's a good smoke test before running the app.
    }
}
