package com.vehicletelemetry.processing.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/processing")
@CrossOrigin(origins = "http://localhost:4200")
public class ProcessingController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingController.class);

    @Autowired
    private TelemetryDataRepository telemetryRepository;

    /**
     * Get paginated vehicles (ESSENTIAL ENDPOINT)
     */
    @GetMapping("/vehicles/all/latest")
    public ResponseEntity<List<ProcessedTelemetryData>> getAllVehiclesLatest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        try {
            // Get all vehicle IDs
            List<String> vehicleIds = telemetryRepository.findAll()
                    .stream()
                    .map(ProcessedTelemetryData::getVehicleId)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            // Apply pagination
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, vehicleIds.size());

            if (startIndex >= vehicleIds.size()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<String> paginatedVehicleIds = vehicleIds.subList(startIndex, endIndex);

            // Get latest data for paginated vehicles
            List<ProcessedTelemetryData> result = new ArrayList<>();
            for (String vehicleId : paginatedVehicleIds) {
                List<ProcessedTelemetryData> latestData = telemetryRepository.findLatestByVehicleId(vehicleId);
                if (!latestData.isEmpty()) {
                    result.add(latestData.get(0));
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Error fetching paginated vehicles: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search vehicles by ID pattern (ESSENTIAL ENDPOINT)
     */
    @GetMapping("/vehicles/search")
    public ResponseEntity<List<ProcessedTelemetryData>> searchVehicles(
            @RequestParam String vehicleId) {
        try {
            // Find vehicles matching pattern
            List<String> matchingIds = telemetryRepository.findAll()
                    .stream()
                    .map(ProcessedTelemetryData::getVehicleId)
                    .distinct()
                    .filter(id -> id.toLowerCase().contains(vehicleId.toLowerCase()))
                    .collect(Collectors.toList());

            // Get latest data for matching vehicles
            List<ProcessedTelemetryData> result = new ArrayList<>();
            for (String matchingId : matchingIds) {
                List<ProcessedTelemetryData> latestData = telemetryRepository.findLatestByVehicleId(matchingId);
                if (!latestData.isEmpty()) {
                    result.add(latestData.get(0));
                }
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("Search error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get total vehicle count (ESSENTIAL ENDPOINT)
     */
    @GetMapping("/vehicles/count")
    public ResponseEntity<Integer> getTotalVehicleCount() {
        try {
            long count = telemetryRepository.findAll()
                    .stream()
                    .map(ProcessedTelemetryData::getVehicleId)
                    .distinct()
                    .count();
            return ResponseEntity.ok((int) count);
        } catch (Exception e) {
            logger.error("Count error: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check (ESSENTIAL)
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UP");
    }
}
