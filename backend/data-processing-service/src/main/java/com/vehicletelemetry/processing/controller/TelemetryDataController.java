package com.vehicletelemetry.processing.controller;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vehicletelemetry.processing.model.ProcessedTelemetryData;
import com.vehicletelemetry.processing.repository.TelemetryDataRepository;

/**
 * TelemetryDataController - Internal API Controller
 * 
 * Provides internal REST endpoints for other microservices to access
 * processed telemetry data. This follows the Single Responsibility Principle
 * by handling only data retrieval operations.
 * 
 * Clean Architecture: This is the presentation layer for internal
 * service-to-service communication.
 */
@RestController
@RequestMapping("/api/processing")
public class TelemetryDataController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryDataController.class);

    private final TelemetryDataRepository repository;

    public TelemetryDataController(TelemetryDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Get telemetry data for a specific vehicle within a time range.
     * Used by the API Gateway to fetch data for external API consumers.
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<List<ProcessedTelemetryData>> getVehicleTelemetry(
            @PathVariable String vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        logger.debug("Fetching telemetry data for vehicle: {} from {} to {}",
                vehicleId, startTime, endTime);

        try {
            List<ProcessedTelemetryData> data = repository.findByVehicleAndTimeRange(
                    vehicleId, startTime, endTime);

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error fetching telemetry data for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the latest telemetry data for a specific vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}/latest")
    public ResponseEntity<List<ProcessedTelemetryData>> getLatestVehicleTelemetry(
            @PathVariable String vehicleId) {

        logger.debug("Fetching latest telemetry data for vehicle: {}", vehicleId);

        try {
            List<ProcessedTelemetryData> data = repository.findLatestByVehicleId(vehicleId);
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error fetching latest telemetry data for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all vehicle IDs that have telemetry data.
     * This is a simplified implementation. In production, you might want
     * to implement this with a separate table or caching mechanism.
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<String>> getAllVehicleIds() {
        logger.debug("Fetching all vehicle IDs");

        try {
            // For now, return a placeholder. In production, implement proper query
            List<String> vehicleIds = List.of("VH001", "VH002", "VH003");
            return ResponseEntity.ok(vehicleIds);

        } catch (Exception e) {
            logger.error("Error fetching vehicle IDs", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for monitoring service availability.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Processing Service is healthy");
    }
}

