package com.vehicletelemetry.ingestion;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicletelemetry.ingestion.model.TelemetryData;
import com.vehicletelemetry.ingestion.service.TelemetryIngestionService;

/**
 * Unit tests for TelemetryIngestionService
 *
 * These tests verify the business logic of the service layer
 * without external dependencies (Kafka, databases, etc.)
 */
class TelemetryIngestionServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private TelemetryIngestionService telemetryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        telemetryService = new TelemetryIngestionService();
        // Use reflection to inject mocks since the service uses @Autowired
        try {
            java.lang.reflect.Field kafkaField = TelemetryIngestionService.class.getDeclaredField("kafkaTemplate");
            kafkaField.setAccessible(true);
            kafkaField.set(telemetryService, kafkaTemplate);

            java.lang.reflect.Field objectMapperField = TelemetryIngestionService.class
                    .getDeclaredField("objectMapper");
            objectMapperField.setAccessible(true);
            objectMapperField.set(telemetryService, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocks", e);
        }
    }

    @Test
    void shouldProcessValidTelemetryData() throws Exception {
        // Given
        TelemetryData validData = createValidTelemetryData();
        when(objectMapper.writeValueAsString(any(TelemetryData.class)))
                .thenReturn("{\"vehicleId\":\"VH001\"}");
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(null); // Mock successful send

        // When
        assertDoesNotThrow(() -> telemetryService.processTelemetryData(validData));

        // Then
        verify(kafkaTemplate).send(eq("vehicles.telemetry"), eq("VH001"), anyString());
    }

    @Test
    void shouldThrowExceptionForInvalidVehicleId() {
        // Given
        TelemetryData invalidData = createValidTelemetryData();
        invalidData.setVehicleId(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> telemetryService.processTelemetryData(invalidData));

        assertEquals("Vehicle ID is required", exception.getMessage());
        verifyNoInteractions(kafkaTemplate); // Should not attempt to publish
    }

    @Test
    void shouldThrowExceptionForNullTimestamp() {
        // Given
        TelemetryData invalidData = createValidTelemetryData();
        invalidData.setTimestamp(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> telemetryService.processTelemetryData(invalidData));

        assertEquals("Timestamp is required", exception.getMessage());
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldHandleKafkaPublishingFailure() throws Exception {
        // Given
        TelemetryData validData = createValidTelemetryData();
        when(objectMapper.writeValueAsString(any(TelemetryData.class)))
                .thenReturn("{\"vehicleId\":\"VH001\"}");
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> telemetryService.processTelemetryData(validData));

        assertEquals("Failed to process telemetry data", exception.getMessage());
    }

    private TelemetryData createValidTelemetryData() {
        return new TelemetryData(
                "VH001",
                Instant.now(),
                40.7128,
                -74.0060,
                65.5,
                85.2,
                90.5,
                32.0,
                1500.0);
    }
}
