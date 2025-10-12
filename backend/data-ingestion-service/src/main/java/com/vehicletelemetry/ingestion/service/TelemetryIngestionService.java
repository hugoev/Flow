package com.vehicletelemetry.ingestion.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicletelemetry.ingestion.model.TelemetryData;

/**
 * TelemetryIngestionService - REST API Telemetry Processing Service
 * 
 * This service handles telemetry data received via REST API endpoints
 * and forwards it to Kafka for processing. It provides a clean interface
 * for the TelemetryController.
 * 
 * Clean Architecture: This is part of the use case layer.
 */
@Service
public class TelemetryIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryIngestionService.class);

    private static final String TELEMETRY_TOPIC = "vehicles.telemetry";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Process a single telemetry data point
     * 
     * @param telemetryData The telemetry data to process
     * @throws IllegalArgumentException if the data is invalid
     */
    public void processTelemetryData(TelemetryData telemetryData) {
        try {
            logger.info("Processing telemetry data for vehicle: {}", telemetryData.getVehicleId());

            // Validate the telemetry data
            validateTelemetryData(telemetryData);

            // Convert to JSON string
            String jsonPayload = objectMapper.writeValueAsString(telemetryData);

            // Send to Kafka
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
                    TELEMETRY_TOPIC,
                    telemetryData.getVehicleId(),
                    jsonPayload);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.debug("Successfully sent telemetry data to Kafka for vehicle: {}",
                            telemetryData.getVehicleId());
                } else {
                    logger.error("Failed to send telemetry data to Kafka for vehicle {}: {}",
                            telemetryData.getVehicleId(), exception.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error processing telemetry data for vehicle {}: {}",
                    telemetryData.getVehicleId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process telemetry data", e);
        }
    }

    /**
     * Process multiple telemetry data points in batch
     * 
     * @param telemetryDataArray Array of telemetry data to process
     * @return Number of successfully processed items
     */
    public int processTelemetryBatch(TelemetryData[] telemetryDataArray) {
        int processedCount = 0;

        for (TelemetryData telemetryData : telemetryDataArray) {
            try {
                processTelemetryData(telemetryData);
                processedCount++;
            } catch (Exception e) {
                logger.warn("Failed to process telemetry data for vehicle {}: {}",
                        telemetryData.getVehicleId(), e.getMessage());
            }
        }

        logger.info("Batch processing completed. Processed: {}/{}", processedCount, telemetryDataArray.length);
        return processedCount;
    }

    /**
     * Validate telemetry data
     * 
     * @param telemetryData The data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTelemetryData(TelemetryData telemetryData) {
        if (telemetryData == null) {
            throw new IllegalArgumentException("Telemetry data cannot be null");
        }

        if (telemetryData.getVehicleId() == null || telemetryData.getVehicleId().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID is required");
        }

        if (telemetryData.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp is required");
        }

        if (telemetryData.getLatitude() == null || telemetryData.getLongitude() == null) {
            throw new IllegalArgumentException("GPS coordinates are required");
        }

        // Additional business logic validation can be added here
        logger.debug("Telemetry data validation passed for vehicle: {}", telemetryData.getVehicleId());
    }

    /**
     * Health check for the service
     * 
     * @return true if the service is healthy
     */
    public boolean isHealthy() {
        try {
            return kafkaTemplate != null;
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}

