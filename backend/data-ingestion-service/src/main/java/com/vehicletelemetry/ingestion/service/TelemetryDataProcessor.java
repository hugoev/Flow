package com.vehicletelemetry.ingestion.service;

/**
 * TelemetryDataProcessor Interface
 * 
 * Defines the contract for processing vehicle telemetry data.
 * This interface ensures loose coupling between MQTT and Kafka components.
 * 
 * Clean Architecture: This is part of the use case layer that defines
 * the business logic for processing telemetry data.
 */
public interface TelemetryDataProcessor {

    /**
     * Process telemetry data from MQTT and forward to Kafka
     * 
     * @param vehicleId The vehicle identifier
     * @param topic     The MQTT topic
     * @param payload   The telemetry data payload
     */
    void processTelemetryData(String vehicleId, String topic, String payload);

    /**
     * Process status data from MQTT and forward to Kafka
     * 
     * @param vehicleId The vehicle identifier
     * @param topic     The MQTT topic
     * @param payload   The status data payload
     */
    void processStatusData(String vehicleId, String topic, String payload);

    /**
     * Process alert data from MQTT and forward to Kafka
     * 
     * @param vehicleId The vehicle identifier
     * @param topic     The MQTT topic
     * @param payload   The alert data payload
     */
    void processAlertData(String vehicleId, String topic, String payload);
}


