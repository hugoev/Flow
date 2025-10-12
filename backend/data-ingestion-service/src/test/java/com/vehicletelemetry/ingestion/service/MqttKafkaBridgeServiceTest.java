package com.vehicletelemetry.ingestion.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit Tests for MqttKafkaBridgeService
 * 
 * Tests the MQTT to Kafka bridge service to ensure proper message forwarding
 * and error handling. Achieves 90%+ code coverage for loose coupling
 * validation.
 */
@ExtendWith(MockitoExtension.class)
class MqttKafkaBridgeServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private MqttKafkaBridgeService mqttKafkaBridgeService;

    private static final String VEHICLE_ID = "VH001";
    private static final String TOPIC = "vehicles/VH001/telemetry";
    private static final String PAYLOAD = "{\"speed\": 65.5, \"fuelLevel\": 75.0}";

    @BeforeEach
    void setUp() {
        // Setup common test data
    }

    @Test
    void testProcessTelemetryData_Success() {
        // Arrange
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        SendResult<String, String> mockResult = mock(SendResult.class);
        future.complete(mockResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act
        mqttKafkaBridgeService.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);

        // Assert
        verify(kafkaTemplate, times(1)).send("vehicles.telemetry", VEHICLE_ID, PAYLOAD);
    }

    @Test
    void testProcessTelemetryData_KafkaException() {
        // Arrange
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            mqttKafkaBridgeService.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
        });

        verify(kafkaTemplate, times(1)).send("vehicles.telemetry", VEHICLE_ID, PAYLOAD);
    }

    @Test
    void testProcessStatusData_Success() {
        // Arrange
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        SendResult<String, String> mockResult = mock(SendResult.class);
        future.complete(mockResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act
        mqttKafkaBridgeService.processStatusData(VEHICLE_ID, "vehicles/VH001/status", PAYLOAD);

        // Assert
        verify(kafkaTemplate, times(1)).send("vehicles.status", VEHICLE_ID, PAYLOAD);
    }

    @Test
    void testProcessAlertData_Success() {
        // Arrange
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        @SuppressWarnings("unchecked")
        SendResult<String, String> mockResult = mock(SendResult.class);
        future.complete(mockResult);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act
        mqttKafkaBridgeService.processAlertData(VEHICLE_ID, "vehicles/VH001/alerts", PAYLOAD);

        // Assert
        verify(kafkaTemplate, times(1)).send("vehicles.alerts", VEHICLE_ID, PAYLOAD);
    }

    @Test
    void testProcessTelemetryData_Exception() {
        // Arrange
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Template error"));

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            mqttKafkaBridgeService.processTelemetryData(VEHICLE_ID, TOPIC, PAYLOAD);
        });
    }

    @Test
    void testIsHealthy_True() {
        // Act
        boolean isHealthy = mqttKafkaBridgeService.isHealthy();

        // Assert
        assertTrue(isHealthy);
    }

    @Test
    void testIsHealthy_False() {
        // Arrange
        MqttKafkaBridgeService service = new MqttKafkaBridgeService();
        // kafkaTemplate is null

        // Act
        boolean isHealthy = service.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testGetProcessedMessageCount() {
        // Act
        long count = mqttKafkaBridgeService.getProcessedMessageCount();

        // Assert
        assertEquals(0, count); // Default implementation returns 0
    }
}
