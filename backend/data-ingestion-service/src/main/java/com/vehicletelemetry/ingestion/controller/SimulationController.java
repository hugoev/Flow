package com.vehicletelemetry.ingestion.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicletelemetry.ingestion.service.AutoSimulationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * SimulationController - Data Simulation Controller
 *
 * Provides endpoints for controlling data simulation for testing and
 * demonstration.
 * Allows starting/stopping simulation and generating historical data.
 */
@RestController
@RequestMapping("/simulation")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Data Simulation", description = "Endpoints for controlling data simulation")
public class SimulationController {

    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);

    @Autowired
    private AutoSimulationService autoSimulationService;

    /**
     * Start data simulation
     */
    @PostMapping("/start")
    @Operation(summary = "Start data simulation", description = "Starts continuous vehicle telemetry data simulation")
    @ApiResponse(responseCode = "200", description = "Simulation started successfully")
    public ResponseEntity<String> startSimulation() {
        try {
            autoSimulationService.startAutoSimulation();
            logger.info("Vehicle simulation started via controller");
            return ResponseEntity.ok("Vehicle simulation started successfully");
        } catch (Exception e) {
            logger.error("Error starting simulation: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to start simulation: " + e.getMessage());
        }
    }

    /**
     * Stop data simulation
     */
    @PostMapping("/stop")
    @Operation(summary = "Stop data simulation", description = "Stops the running data simulation")
    @ApiResponse(responseCode = "200", description = "Simulation stopped successfully")
    public ResponseEntity<String> stopSimulation() {
        try {
            autoSimulationService.stopAutoSimulation();
            logger.info("Vehicle simulation stopped via controller");
            return ResponseEntity.ok("Vehicle simulation stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping simulation: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to stop simulation: " + e.getMessage());
        }
    }

    /**
     * Get simulation status
     */
    @GetMapping("/status")
    @Operation(summary = "Get simulation status", description = "Returns current status of vehicle simulation")
    @ApiResponse(responseCode = "200", description = "Simulation status retrieved successfully")
    public ResponseEntity<Map<String, String>> getSimulationStatus() {
        try {
            logger.info("Simulation status requested");
            String status = autoSimulationService.getSimulationStatus();
            Map<String, String> response = new HashMap<>();
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting simulation status: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get simulation status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Get detailed simulation info
     */
    @GetMapping("/info")
    @Operation(summary = "Get simulation info", description = "Returns detailed information about the simulation")
    @ApiResponse(responseCode = "200", description = "Simulation info retrieved successfully")
    public ResponseEntity<Map<String, Object>> getSimulationInfo() {
        try {
            String status = autoSimulationService.getSimulationStatus();
            int restartCount = autoSimulationService.getRestartCount();
            boolean isRunning = autoSimulationService.isSimulationRunning();

            Map<String, Object> response = new HashMap<>();
            response.put("status", status);
            response.put("restartCount", restartCount);
            response.put("isRunning", isRunning);
            response.put("autoRestart", "Every 2 minutes");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting simulation info: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get simulation info: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Health check for simulation service
     */
    @GetMapping("/health")
    @Operation(summary = "Simulation health check", description = "Returns simulation service health status")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Simulation Service is healthy");
    }
}
