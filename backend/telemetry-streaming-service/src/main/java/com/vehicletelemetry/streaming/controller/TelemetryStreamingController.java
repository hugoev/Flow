package com.vehicletelemetry.streaming.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicletelemetry.streaming.model.TelemetryData;
import com.vehicletelemetry.streaming.service.TelemetryStreamingService;

import reactor.core.publisher.Flux;

/**
 * TelemetryStreamingController - REST Controller for Real-time Telemetry
 * Streaming
 * 
 * This controller provides Server-Sent Events (SSE) endpoints for real-time
 * vehicle telemetry data streaming. It follows object-oriented principles
 * with proper dependency injection and clear separation of concerns.
 * 
 * Clean Architecture: This is part of the presentation layer.
 */
@RestController
@RequestMapping("/api/telemetry")
public class TelemetryStreamingController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryStreamingController.class);

    private final TelemetryStreamingService telemetryStreamingService;

    public TelemetryStreamingController(TelemetryStreamingService telemetryStreamingService) {
        this.telemetryStreamingService = telemetryStreamingService;
    }

    /**
     * Provides real-time telemetry stream for all vehicles in the fleet
     * 
     * @return Server-Sent Events stream of telemetry data from all vehicles
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TelemetryData> streamAllVehiclesTelemetry() {
        logger.info("Received request for real-time telemetry stream from all vehicles");
        return telemetryStreamingService.createAllVehiclesStream();
    }

    /**
     * Provides real-time telemetry stream for a specific vehicle
     * 
     * @param vehicleId The specific vehicle to stream data for
     * @return Server-Sent Events stream of telemetry data for the specified vehicle
     */
    @GetMapping(value = "/vehicles/{vehicleId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TelemetryData> streamSingleVehicleTelemetry(@PathVariable String vehicleId) {
        logger.info("Received request for real-time telemetry stream from vehicle: {}", vehicleId);
        return telemetryStreamingService.createSingleVehicleStream(vehicleId);
    }

    /**
     * Health check endpoint for the telemetry streaming service
     * 
     * @return Health status message
     */
    @GetMapping("/health")
    public String getServiceHealth() {
        logger.debug("Health check requested for telemetry streaming service");
        return "Telemetry Streaming Service is healthy and operational";
    }

    /**
     * Gets information about the vehicle fleet
     * 
     * @return Fleet information including total vehicle count
     */
    @GetMapping("/fleet/info")
    public FleetInfo getFleetInfo() {
        logger.debug("Fleet information requested");
        int totalVehicleCount = telemetryStreamingService.getTotalVehicleCount();
        return new FleetInfo(totalVehicleCount);
    }

    /**
     * FleetInfo - Value object for fleet information
     */
    public static class FleetInfo {
        private final int totalVehicleCount;
        private final String serviceStatus;

        public FleetInfo(int totalVehicleCount) {
            this.totalVehicleCount = totalVehicleCount;
            this.serviceStatus = "operational";
        }

        public int getTotalVehicleCount() {
            return totalVehicleCount;
        }

        public String getServiceStatus() {
            return serviceStatus;
        }
    }
}
