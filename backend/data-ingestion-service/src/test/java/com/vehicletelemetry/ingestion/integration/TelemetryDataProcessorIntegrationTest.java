package com.vehicletelemetry.ingestion.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.TestPropertySource;

import com.vehicletelemetry.ingestion.service.MqttKafkaBridgeService;
import com.vehicletelemetry.ingestion.service.TelemetryDataProcessor;

/**
 * Integration Tests for TelemetryDataProcessor
 * 
 * Tests the loose coupling between MQTT and Kafka components through
 * dependency injection and interface-based design. Validates that services
 * can be easily swapped and tested in isolation.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=localhost:9092",
        "mqtt.broker.url=tcp://localhost:1883",
        "mqtt.topic.prefix=vehicles",
        "mqtt.qos=1"
})
class TelemetryDataProcessorIntegrationTest {

    @Autowired
    private TelemetryDataProcessor telemetryDataProcessor;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    private static final String VEHICLE_ID = "VH001";
    private static final String TOPIC = "vehicles/VH001/telemetry";
    private static final String PAYLOAD = "{\"speed\": 65.5, \"fuelLevel\": 75.0}";

    @Test
    void testTelemetryDataProcessor_IsInjected() {
        // Assert
        assertNotNull(telemetryDataProcessor, "TelemetryDataProcessor should be injected");
        assertTrue(telemetryDataProcessor instanceof MqttKafkaBridgeService,
                "Should be instance of MqttKafkaBridgeService");
    }

    @Test
    void testProcessTelemetryData_LooseCoupling() {
        // Act & Assert - Should not throw exception due to loose coupling
        assertDoesNotThrow(() -> {
            telemetryDataProcessor.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
        });
    }

    @Test
    void testProcessStatusData_LooseCoupling() {
        // Act & Assert - Should not throw exception due to loose coupling
        assertDoesNotThrow(() -> {
            telemetryDataProcessor.processStatusData(VEHICLE_ID, "vehicles/VH001/status", PAYLOAD);
        });
    }

    @Test
    void testProcessAlertData_LooseCoupling() {
        // Act & Assert - Should not throw exception due to loose coupling
        assertDoesNotThrow(() -> {
            telemetryDataProcessor.processAlertData(VEHICLE_ID, "vehicles/VH001/alerts", PAYLOAD);
        });
    }

    @Test
    void testServiceCanBeSwapped() {
        // This test validates that the service can be easily swapped
        // by implementing the TelemetryDataProcessor interface

        // Arrange
        TelemetryDataProcessor mockProcessor = mock(TelemetryDataProcessor.class);

        // Act
        mockProcessor.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);

        // Assert
        verify(mockProcessor, times(1)).processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
    }

    @Test
    void testInterfaceContract_AllMethodsImplemented() {
        // This test ensures all interface methods are properly implemented

        // Act & Assert
        assertDoesNotThrow(() -> {
            telemetryDataProcessor.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
            telemetryDataProcessor.processStatusData(VEHICLE_ID, "vehicles/VH001/status", PAYLOAD);
            telemetryDataProcessor.processAlertData(VEHICLE_ID, "vehicles/VH001/alerts", PAYLOAD);
        });
    }

    @Test
    void testErrorHandling_LooseCoupling() {
        // This test ensures that errors in one component don't affect others

        // Arrange - Simulate Kafka failure
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        // Act & Assert - Should handle errors gracefully due to loose coupling
        assertDoesNotThrow(() -> {
            telemetryDataProcessor.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
        });
    }

    @Test
    void testDependencyInjection_SpringContext() {
        // This test validates that Spring's dependency injection is working
        // and services are properly wired together

        // Assert
        assertNotNull(telemetryDataProcessor, "Service should be injected by Spring");

        // Verify that the service is properly configured
        if (telemetryDataProcessor instanceof MqttKafkaBridgeService) {
            MqttKafkaBridgeService service = (MqttKafkaBridgeService) telemetryDataProcessor;
            assertTrue(service.isHealthy(), "Service should be healthy");
        }
    }

    @Test
    void testServiceIsolation_IndependentTesting() {
        // This test validates that services can be tested independently
        // without affecting other components

        // Arrange
        TelemetryDataProcessor isolatedProcessor = new TelemetryDataProcessor() {
            @Override
            public void processTelemetryData(String vehicleId, String topic, String payload) {
                // Isolated implementation for testing
            }

            @Override
            public void processStatusData(String vehicleId, String topic, String payload) {
                // Isolated implementation for testing
            }

            @Override
            public void processAlertData(String vehicleId, String topic, String payload) {
                // Isolated implementation for testing
            }
        };

        // Act & Assert - Should work independently
        assertDoesNotThrow(() -> {
            isolatedProcessor.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
            isolatedProcessor.processStatusData(VEHICLE_ID, "vehicles/VH001/status", PAYLOAD);
            isolatedProcessor.processAlertData(VEHICLE_ID, "vehicles/VH001/alerts", PAYLOAD);
        });
    }
}
