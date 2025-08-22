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
    private KafkaTemplate<String, TelemetryData> kafkaTemplate;

    private TelemetryIngestionService telemetryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        telemetryService = new TelemetryIngestionService(kafkaTemplate);
    }

    @Test
    void shouldProcessValidTelemetryData() {
        // Given
        TelemetryData validData = createValidTelemetryData();
        when(kafkaTemplate.send(anyString(), anyString(), any(TelemetryData.class)))
                .thenReturn(null); // Mock successful send

        // When
        assertDoesNotThrow(() -> telemetryService.processTelemetryData(validData));

        // Then
        verify(kafkaTemplate).send(eq("vehicle-telemetry"), eq("VH001"), eq(validData));
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

        assertEquals("Vehicle ID cannot be null or empty", exception.getMessage());
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

        assertEquals("Timestamp cannot be null", exception.getMessage());
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldHandleKafkaPublishingFailure() {
        // Given
        TelemetryData validData = createValidTelemetryData();
        when(kafkaTemplate.send(anyString(), anyString(), any(TelemetryData.class)))
                .thenThrow(new RuntimeException("Kafka connection failed"));

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> telemetryService.processTelemetryData(validData));

        assertEquals("Failed to publish telemetry data", exception.getMessage());
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
                32.0);
    }
}
