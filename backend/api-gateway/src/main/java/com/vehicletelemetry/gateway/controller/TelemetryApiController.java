package com.vehicletelemetry.gateway.controller;

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

import com.vehicletelemetry.gateway.model.TelemetryDataResponse;
import com.vehicletelemetry.gateway.model.VehicleSummary;
import com.vehicletelemetry.gateway.service.TelemetryApiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * TelemetryApiController - REST API Controller
 *
 * Provides REST endpoints for accessing vehicle telemetry data.
 * This controller aggregates data from various backend services
 * and provides a unified API for frontend applications.
 *
 * Clean Architecture: This is the presentation layer that handles
 * HTTP requests and delegates business logic to the service layer.
 */
@RestController
@RequestMapping("/api/telemetry")
@Tag(name = "Telemetry API", description = "Endpoints for accessing vehicle telemetry data")
public class TelemetryApiController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryApiController.class);

    private final TelemetryApiService telemetryService;

    public TelemetryApiController(TelemetryApiService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Get telemetry data for a specific vehicle within a time range.
     */
    @GetMapping("/vehicles/{vehicleId}")
    @Operation(summary = "Get vehicle telemetry data", description = "Retrieves telemetry data for a specific vehicle within the specified time range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved telemetry data")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    @ApiResponse(responseCode = "400", description = "Invalid parameters")
    public ResponseEntity<List<TelemetryDataResponse>> getVehicleTelemetry(
            @Parameter(description = "Vehicle ID") @PathVariable String vehicleId,
            @Parameter(description = "Start time (ISO 8601 format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @Parameter(description = "End time (ISO 8601 format)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        logger.info("Request for telemetry data: vehicleId={}, startTime={}, endTime={}",
                vehicleId, startTime, endTime);

        try {
            List<TelemetryDataResponse> data = telemetryService.getVehicleTelemetry(
                    vehicleId, startTime, endTime);

            if (data.isEmpty()) {
                logger.info("No telemetry data found for vehicle: {}", vehicleId);
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error retrieving telemetry data for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get the latest telemetry data for a specific vehicle.
     */
    @GetMapping("/vehicles/{vehicleId}/latest")
    @Operation(summary = "Get latest vehicle telemetry", description = "Retrieves the most recent telemetry data for a specific vehicle")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved latest telemetry data")
    @ApiResponse(responseCode = "404", description = "Vehicle not found")
    public ResponseEntity<TelemetryDataResponse> getLatestVehicleTelemetry(
            @Parameter(description = "Vehicle ID") @PathVariable String vehicleId) {

        logger.info("Request for latest telemetry data: vehicleId={}", vehicleId);

        try {
            TelemetryDataResponse data = telemetryService.getLatestVehicleTelemetry(vehicleId);

            if (data == null) {
                logger.info("No telemetry data found for vehicle: {}", vehicleId);
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error retrieving latest telemetry data for vehicle: {}", vehicleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get summary information for all vehicles.
     */
    @GetMapping("/vehicles")
    @Operation(summary = "Get all vehicles summary", description = "Retrieves summary information for all vehicles with telemetry data")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved vehicle summaries")
    public ResponseEntity<List<VehicleSummary>> getAllVehiclesSummary() {

        logger.info("Request for all vehicles summary");

        try {
            List<VehicleSummary> summaries = telemetryService.getAllVehiclesSummary();
            return ResponseEntity.ok(summaries);

        } catch (Exception e) {
            logger.error("Error retrieving vehicles summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get real-time telemetry data stream for a vehicle.
     * This endpoint provides server-sent events for real-time updates.
     */
    @GetMapping("/vehicles/{vehicleId}/stream")
    @Operation(summary = "Stream vehicle telemetry data", description = "Provides real-time telemetry data stream for a specific vehicle using Server-Sent Events")
    @ApiResponse(responseCode = "200", description = "Real-time telemetry stream established")
    public ResponseEntity<String> streamVehicleTelemetry(
            @Parameter(description = "Vehicle ID") @PathVariable String vehicleId) {

        logger.info("Request for real-time telemetry stream: vehicleId={}", vehicleId);

        // TODO: Implement Server-Sent Events for real-time streaming
        // This would require WebFlux and integration with Kafka consumer

        return ResponseEntity.ok("Real-time streaming endpoint - Coming soon");
    }

    /**
     * Health check endpoint for monitoring service availability.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway Service is healthy");
    }
}
