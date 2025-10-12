package com.vehicletelemetry.streaming;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Telemetry Streaming Service Test
 * 
 * Basic integration test to verify the service starts correctly
 * and all components are properly wired.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "server.port=0", // Use random port for testing
        "spring.application.name=telemetry-streaming-service-test"
})
class TelemetryStreamingServiceTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // and all beans are properly configured
    }
}


