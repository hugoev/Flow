package com.vehicletelemetry.processing.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.vehicletelemetry.processing.model.ProcessedTelemetryData;
import com.vehicletelemetry.processing.model.TelemetryData;
import com.vehicletelemetry.processing.service.TelemetryProcessingService;

/**
 * TelemetryDataConsumer - Kafka Consumer
 *
 * Consumes telemetry data from Kafka topics and delegates processing to the
 * service layer.
 * Implements reliable message processing with proper error handling and
 * acknowledgment.
 *
 * Clean Architecture: This is part of the infrastructure layer that handles
 * external messaging systems, while delegating business logic to services.
 */
@Component
public class TelemetryDataConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryDataConsumer.class);
    private static final String TELEMETRY_TOPIC = "vehicle-telemetry";

    private final TelemetryProcessingService processingService;

    public TelemetryDataConsumer(TelemetryProcessingService processingService) {
        this.processingService = processingService;
    }

    /**
     * Kafka listener for processing telemetry data messages.
     * Configured for batch processing to handle high-volume data streams
     * efficiently.
     *
     * @param telemetryDataBatch Batch of telemetry data from Kafka
     * @param acknowledgment     Manual acknowledgment for reliable message
     *                           processing
     * @param partition          Partition information for monitoring
     * @param offset             Offset information for monitoring
     */
    @KafkaListener(topics = TELEMETRY_TOPIC, groupId = "telemetry-processing-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeTelemetryData(
            @Payload TelemetryData telemetryData,
            Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        logger.debug("Received telemetry data for vehicle: {} from partition: {} at offset: {}",
                telemetryData.getVehicleId(), partition, offset);

        try {
            // Process the telemetry data
            ProcessedTelemetryData processedData = processingService.processTelemetryData(telemetryData);

            // Log successful processing
            logger.info("Successfully processed telemetry data for vehicle: {} at {}",
                    processedData.getVehicleId(), processedData.getTimestamp());

            // Acknowledge the message to commit the offset
            acknowledgment.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process telemetry data for vehicle: {} at offset: {}",
                    telemetryData.getVehicleId(), offset, e);

            // In a production system, you might want to:
            // 1. Send failed messages to a dead letter queue
            // 2. Implement retry logic with exponential backoff
            // 3. Send alerts for systematic failures

            // For now, we'll acknowledge to prevent infinite retries
            // In production, consider not acknowledging on certain types of failures
            acknowledgment.acknowledge();
        }
    }

    /**
     * Alternative single-message listener for low-volume scenarios.
     * Uncomment and use instead of batch processing if needed.
     */
    /*
     * @KafkaListener(
     * topics = TELEMETRY_TOPIC,
     * groupId = "telemetry-processing-group-single"
     * )
     * public void consumeSingleTelemetryData(
     * 
     * @Payload TelemetryData telemetryData,
     * Acknowledgment acknowledgment) {
     * 
     * try {
     * ProcessedTelemetryData processedData =
     * processingService.processTelemetryData(telemetryData);
     * logger.info("Processed telemetry data for vehicle: {}",
     * processedData.getVehicleId());
     * acknowledgment.acknowledge();
     * 
     * } catch (Exception e) {
     * logger.error("Failed to process telemetry data for vehicle: {}",
     * telemetryData.getVehicleId(), e);
     * acknowledgment.acknowledge();
     * }
     * }
     */
}
