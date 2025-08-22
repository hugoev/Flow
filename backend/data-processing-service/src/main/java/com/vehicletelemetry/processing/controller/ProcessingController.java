package com.vehicletelemetry.processing.controller;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * ProcessingController - REST API for processed telemetry data
 * 
 * Provides endpoints for retrieving processed vehicle telemetry data
 * from Cassandra database. This controller follows clean architecture
 * principles by separating API concerns from business logic.
 */
@RestController
@RequestMapping("/api/processing")
public class ProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingController.class);

    @Autowired
    private TelemetryDataRepository telemetryRepository;

    /**
     * Get all vehicle IDs that have telemetry data
     */
    @GetMapping("/vehicles")
    public ResponseEntity<List<String>> getAllVehicleIds() {
        try {
            logger.info("Fetching all vehicle IDs");

            // Get all records and extract unique vehicle IDs
            // Note: In production, this should be optimized with a separate table
            List<String> vehicleIds = telemetryRepository.findAll()
                    .stream()
                    .map(ProcessedTelemetryData::getVehicleId)
                    .distinct()
                    .collect(Collectors.toList());

            logger.info("Found {} unique vehicles", vehicleIds.size());
            return ResponseEntity.ok(vehicleIds);

        } catch (Exception e) {
            logger.error("Error fetching vehicle IDs: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get telemetry data for a specific vehicle within a time range
     */
    @GetMapping("/vehicles/{vehicleId}")
    public ResponseEntity<List<ProcessedTelemetryData>> getVehicleTelemetry(
            @PathVariable String vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        try {
            logger.info("Fetching telemetry for vehicle {} from {} to {}", vehicleId, startTime, endTime);

            List<ProcessedTelemetryData> data = telemetryRepository.findByVehicleIdAndTimestampBetween(
                    vehicleId, startTime, endTime);

            logger.info("Found {} telemetry records for vehicle {}", data.size(), vehicleId);
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error fetching telemetry for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the latest telemetry data for a specific vehicle
     */
    @GetMapping("/vehicles/{vehicleId}/latest")
    public ResponseEntity<List<ProcessedTelemetryData>> getLatestVehicleTelemetry(
            @PathVariable String vehicleId) {

        try {
            logger.info("Fetching latest telemetry for vehicle {}", vehicleId);

            // Get the most recent 10 records for the vehicle
            List<ProcessedTelemetryData> data = telemetryRepository.findTop10ByVehicleIdOrderByTimestampDesc(vehicleId);

            logger.info("Found {} latest records for vehicle {}", data.size(), vehicleId);
            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error fetching latest telemetry for vehicle {}: {}", vehicleId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint for the processing service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\",\"service\":\"data-processing\"}");
    }
}
