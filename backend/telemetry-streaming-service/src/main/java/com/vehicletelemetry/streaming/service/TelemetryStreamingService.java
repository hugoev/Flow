package com.vehicletelemetry.streaming.service;

import java.time.Duration;
import java.util.List;

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
    private static final Duration STREAM_INTERVAL = Duration.ofSeconds(1);

    private final TelemetryDataGenerator telemetryDataGenerator;

    public TelemetryStreamingService(TelemetryDataGenerator telemetryDataGenerator) {
        this.telemetryDataGenerator = telemetryDataGenerator;
    }

    /**
     * SSE stream for specific vehicles with concurrent updates (ESSENTIAL METHOD)
     */
    public Flux<TelemetryData> createSpecificVehiclesStream(List<String> vehicleIds) {
        if (vehicleIds == null || vehicleIds.isEmpty()) {
            return Flux.empty();
        }

        // Create concurrent streams for all vehicles
        return Flux.interval(STREAM_INTERVAL)
                .flatMap(intervalTick -> {
                    // Generate telemetry for ALL vehicles concurrently
                    return Flux.fromIterable(vehicleIds)
                            .map(vehicleId -> telemetryDataGenerator.generateTelemetryForVehicle(vehicleId));
                })
                .onErrorResume(throwable -> {
                    logger.error("Stream error: {}", throwable.getMessage());
                    return Flux.empty();
                });
    }

}