package com.vehicletelemetry.processing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vehicletelemetry.processing.model.ProcessedTelemetryData;
import com.vehicletelemetry.processing.model.TelemetryData;
import com.vehicletelemetry.processing.repository.TelemetryDataRepository;

/**
 * TelemetryProcessingService - Service Layer
 *
 * Contains business logic for processing telemetry data received from Kafka.
 * This service handles data transformation, validation, and persistence.
 *
 * Clean Architecture: This is part of the use case layer that orchestrates
 * data processing while maintaining separation of concerns.
 */
@Service
public class TelemetryProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryProcessingService.class);

    private final TelemetryDataRepository repository;

    public TelemetryProcessingService(TelemetryDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Processes incoming telemetry data and stores it in Cassandra.
     * This method can be extended with additional business logic like:
     * - Data enrichment
     * - Anomaly detection
     * - Real-time analytics
     * - Alert generation
     *
     * @param rawData Raw telemetry data from Kafka
     * @return Processed telemetry data that was stored
     * @throws IllegalArgumentException if data validation fails
     */
    public ProcessedTelemetryData processTelemetryData(TelemetryData rawData) {
        logger.debug("Processing telemetry data for vehicle: {}", rawData.getVehicleId());

        // Validate incoming data
        validateTelemetryData(rawData);

        // Apply business transformations
        ProcessedTelemetryData processedData = transformTelemetryData(rawData);

        // Store in Cassandra
        ProcessedTelemetryData savedData = repository.save(processedData);

        logger.debug("Successfully processed and stored telemetry data for vehicle: {}",
                savedData.getVehicleId());

        return savedData;
    }

    /**
     * Validates telemetry data according to business rules.
     * Extends basic validation with business-specific checks.
     */
    private void validateTelemetryData(TelemetryData data) {
        if (data.getVehicleId() == null || data.getVehicleId().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
        }

        if (data.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        // Business rule: Speed should be reasonable (0-200 km/h)
        if (data.getSpeed() != null && (data.getSpeed() < 0 || data.getSpeed() > 200)) {
            throw new IllegalArgumentException("Speed must be between 0 and 200 km/h");
        }

        // Business rule: Fuel level should be reasonable (0-100%)
        if (data.getFuelLevel() != null && (data.getFuelLevel() < 0 || data.getFuelLevel() > 100)) {
            throw new IllegalArgumentException("Fuel level must be between 0 and 100");
        }

        logger.debug("Business validation passed for vehicle: {}", data.getVehicleId());
    }

    /**
     * Transforms raw telemetry data into processed format.
     * This is where business logic for data transformation would go.
     * Currently it's a simple mapping, but could include:
     * - Unit conversions
     * - Data normalization
     * - Calculated fields
     * - Anomaly scoring
     */
    private ProcessedTelemetryData transformTelemetryData(TelemetryData rawData) {
        ProcessedTelemetryData processedData = new ProcessedTelemetryData(
                rawData.getVehicleId(),
                rawData.getTimestamp(),
                rawData.getLatitude(),
                rawData.getLongitude(),
                rawData.getSpeed(),
                rawData.getFuelLevel(),
                rawData.getEngineTemp(),
                rawData.getTirePressure());

        // Future enhancement: Add calculated fields
        // processedData.setSpeedKmh(convertMphToKmh(rawData.getSpeed()));
        // processedData.setAnomalyScore(calculateAnomalyScore(rawData));
        // processedData.setDistanceFromLastReading(calculateDistance(processedData));

        return processedData;
    }
}
