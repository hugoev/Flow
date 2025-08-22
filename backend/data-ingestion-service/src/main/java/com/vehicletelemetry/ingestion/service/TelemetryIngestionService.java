package com.vehicletelemetry.ingestion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.vehicletelemetry.ingestion.model.TelemetryData;

/**
 * TelemetryIngestionService - Service Layer
 *
 * This service handles the business logic for processing telemetry data.
 * It follows the Single Responsibility Principle by focusing only on
 * data ingestion and publishing to Kafka.
 *
 * Clean Architecture: This is part of the use case layer that orchestrates
 * the flow of data between the presentation layer and the infrastructure layer.
 */
@Service
public class TelemetryIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryIngestionService.class);
    private static final String TELEMETRY_TOPIC = "vehicle-telemetry";

    private final KafkaTemplate<String, TelemetryData> kafkaTemplate;

    public TelemetryIngestionService(KafkaTemplate<String, TelemetryData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Processes incoming telemetry data by validating it and publishing to Kafka.
     *
     * @param telemetryData The telemetry data to process
     * @throws IllegalArgumentException if data is invalid
     */
    public void processTelemetryData(TelemetryData telemetryData) {
        logger.info("Processing telemetry data for vehicle: {}", telemetryData.getVehicleId());

        // Validate data (additional business rules can be added here)
        validateTelemetryData(telemetryData);

        // Publish to Kafka for further processing
        publishToKafka(telemetryData);

        logger.debug("Successfully processed telemetry data for vehicle: {}", telemetryData.getVehicleId());
    }

    /**
     * Validates telemetry data according to business rules.
     * This method can be extended with more sophisticated validation logic.
     */
    private void validateTelemetryData(TelemetryData data) {
        if (data.getVehicleId() == null || data.getVehicleId().trim().isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID cannot be null or empty");
        }

        if (data.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        // Add more business validation rules as needed
        logger.debug("Telemetry data validation passed for vehicle: {}", data.getVehicleId());
    }

    /**
     * Publishes telemetry data to Kafka topic.
     * Uses vehicle ID as the message key for proper partitioning.
     */
    private void publishToKafka(TelemetryData data) {
        try {
            kafkaTemplate.send(TELEMETRY_TOPIC, data.getVehicleId(), data);
            logger.debug("Published telemetry data to Kafka for vehicle: {}", data.getVehicleId());
        } catch (Exception e) {
            logger.error("Failed to publish telemetry data to Kafka for vehicle: {}", data.getVehicleId(), e);
            throw new RuntimeException("Failed to publish telemetry data", e);
        }
    }
}
