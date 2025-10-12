package com.vehicletelemetry.streaming.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vehicletelemetry.streaming.model.TelemetryData;

import reactor.core.publisher.Flux;

/**
 * TelemetryStreamingService - Real-time Telemetry Data Streaming
 * 
 * This service provides real-time telemetry data streaming using Server-Sent
 * Events (SSE). It follows object-oriented principles with proper dependency
 * injection and loose coupling.
 * 
 * Clean Architecture: This is part of the use case layer.
 */
@Service
public class TelemetryStreamingService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryStreamingService.class);
    private static final Duration ALL_VEHICLES_STREAM_INTERVAL = Duration.ofSeconds(3);
    private static final Duration SINGLE_VEHICLE_STREAM_INTERVAL = Duration.ofSeconds(5);

    private final TelemetryDataGenerator telemetryDataGenerator;
    private final VehicleIdGenerator vehicleIdGenerator;

    public TelemetryStreamingService(TelemetryDataGenerator telemetryDataGenerator,
            VehicleIdGenerator vehicleIdGenerator) {
        this.telemetryDataGenerator = telemetryDataGenerator;
        this.vehicleIdGenerator = vehicleIdGenerator;
    }

    /**
     * Creates a real-time stream for all vehicles in the fleet
     * 
     * @return Flux stream of telemetry data from all vehicles
     */
    public Flux<TelemetryData> createAllVehiclesStream() {
        logger.info("Creating real-time telemetry stream for all vehicles in fleet");

        return Flux.interval(ALL_VEHICLES_STREAM_INTERVAL)
                .map(intervalTick -> telemetryDataGenerator.generateRandomVehicleTelemetry())
                .doOnNext(telemetryData -> logTelemetryData(telemetryData))
                .doOnSubscribe(subscription -> logger.info("Client connected to all vehicles telemetry stream"))
                .doOnCancel(() -> logger.info("Client disconnected from all vehicles telemetry stream"))
                .onErrorResume(throwable -> handleStreamError("all vehicles", throwable));
    }

    /**
     * Creates a real-time stream for a specific vehicle
     * 
     * @param vehicleId The specific vehicle to stream data for
     * @return Flux stream of telemetry data for the specified vehicle
     */
    public Flux<TelemetryData> createSingleVehicleStream(String vehicleId) {
        logger.info("Creating real-time telemetry stream for vehicle: {}", vehicleId);

        return Flux.interval(SINGLE_VEHICLE_STREAM_INTERVAL)
                .map(intervalTick -> telemetryDataGenerator.generateTelemetryForVehicle(vehicleId))
                .doOnNext(telemetryData -> logTelemetryData(telemetryData))
                .doOnSubscribe(subscription -> logger.info("Client connected to vehicle stream: {}", vehicleId))
                .doOnCancel(() -> logger.info("Client disconnected from vehicle stream: {}", vehicleId))
                .onErrorResume(throwable -> handleStreamError("vehicle " + vehicleId, throwable));
    }

    /**
     * Gets the total number of vehicles in the fleet
     * 
     * @return Total vehicle count
     */
    public int getTotalVehicleCount() {
        return vehicleIdGenerator.getTotalVehicleCount();
    }

    /**
     * Logs telemetry data at debug level
     * 
     * @param telemetryData The telemetry data to log
     */
    private void logTelemetryData(TelemetryData telemetryData) {
        logger.debug("Streaming telemetry data: {}", telemetryData);
    }

    /**
     * Handles stream errors with proper logging and graceful recovery
     * 
     * @param streamDescription Description of the stream that encountered an error
     * @param throwable         The error that occurred
     * @return Empty flux to gracefully handle the error
     */
    private Flux<TelemetryData> handleStreamError(String streamDescription, Throwable throwable) {
        logger.error("Error in {} telemetry stream: {}", streamDescription, throwable.getMessage(), throwable);
        return Flux.empty();
    }
}