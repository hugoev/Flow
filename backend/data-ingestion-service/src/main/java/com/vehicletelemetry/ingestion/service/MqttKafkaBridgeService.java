package com.vehicletelemetry.ingestion.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

/**
 * MQTT to Kafka Bridge Service
 * 
 * Implements the TelemetryDataProcessor interface to bridge MQTT messages
 * to Kafka topics. This service maintains loose coupling between MQTT and
 * Kafka.
 * 
 * Clean Architecture: This is part of the infrastructure layer that handles
 * the technical integration between MQTT and Kafka.
 */
@Service
public class MqttKafkaBridgeService implements TelemetryDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(MqttKafkaBridgeService.class);

    @Autowired
    private KafkaTemplate<String, String> stringKafkaTemplate;

    // Kafka topics for different types of data
    private static final String TELEMETRY_TOPIC = "vehicles.telemetry";
    private static final String STATUS_TOPIC = "vehicles.status";
    private static final String ALERTS_TOPIC = "vehicles.alerts";

    /**
     * Process telemetry data from MQTT and forward to Kafka
     * Maintains loose coupling by using interface-based design
     */
    @Override
    public void processTelemetryData(String vehicleId, String topic, String payload) {
        try {
            logger.info("Bridging MQTT telemetry data to Kafka for vehicle: {}", vehicleId);

            // Forward to Kafka with vehicle ID as key for partitioning
            CompletableFuture<SendResult<String, String>> future = stringKafkaTemplate.send(TELEMETRY_TOPIC, vehicleId,
                    payload);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.debug("Successfully sent telemetry data to Kafka for vehicle: {}", vehicleId);
                } else {
                    logger.error("Failed to send telemetry data to Kafka for vehicle {}: {}",
                            vehicleId, exception.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error processing telemetry data for vehicle {}: {}", vehicleId, e.getMessage(), e);
        }
    }

    /**
     * Process status data from MQTT and forward to Kafka
     */
    @Override
    public void processStatusData(String vehicleId, String topic, String payload) {
        try {
            logger.info("Bridging MQTT status data to Kafka for vehicle: {}", vehicleId);

            CompletableFuture<SendResult<String, String>> future = stringKafkaTemplate.send(STATUS_TOPIC, vehicleId, payload);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.debug("Successfully sent status data to Kafka for vehicle: {}", vehicleId);
                } else {
                    logger.error("Failed to send status data to Kafka for vehicle {}: {}",
                            vehicleId, exception.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error processing status data for vehicle {}: {}", vehicleId, e.getMessage(), e);
        }
    }

    /**
     * Process alert data from MQTT and forward to Kafka
     */
    @Override
    public void processAlertData(String vehicleId, String topic, String payload) {
        try {
            logger.warn("Bridging MQTT alert data to Kafka for vehicle: {}", vehicleId);

            // Alerts use highest priority and are sent immediately
            CompletableFuture<SendResult<String, String>> future = stringKafkaTemplate.send(ALERTS_TOPIC, vehicleId, payload);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.warn("Successfully sent alert data to Kafka for vehicle: {}", vehicleId);
                } else {
                    logger.error("Failed to send alert data to Kafka for vehicle {}: {}",
                            vehicleId, exception.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("Error processing alert data for vehicle {}: {}", vehicleId, e.getMessage(), e);
        }
    }

    /**
     * Get the number of messages processed
     * This method can be used for monitoring and metrics
     */
    public long getProcessedMessageCount() {
        // This would be implemented with actual metrics collection
        return 0;
    }

    /**
     * Check if the service is healthy
     * Used for health checks and monitoring
     */
    public boolean isHealthy() {
        try {
            // Check if Kafka template is available and healthy
            return stringKafkaTemplate != null;
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }
}
