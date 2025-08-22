package com.vehicletelemetry.ingestion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vehicletelemetry.ingestion.model.TelemetryData;
import com.vehicletelemetry.ingestion.service.TelemetryIngestionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * TelemetryController - REST API Controller
 *
 * This controller provides REST endpoints for ingesting telemetry data.
 * It follows REST principles and provides clear, documented APIs.
 *
 * Clean Architecture: This is the presentation layer that handles
 * HTTP requests and delegates business logic to the service layer.
 */
@RestController
@RequestMapping("/telemetry")
@Tag(name = "Telemetry API", description = "Endpoints for vehicle telemetry data ingestion")
public class TelemetryController {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryController.class);

    private final TelemetryIngestionService telemetryService;

    public TelemetryController(TelemetryIngestionService telemetryService) {
        this.telemetryService = telemetryService;
    }

    /**
     * Endpoint for ingesting single telemetry data point.
     *
     * @param telemetryData The telemetry data to ingest
     * @return HTTP response indicating success or failure
     */
    @PostMapping("/ingest")
    @Operation(summary = "Ingest telemetry data", description = "Accepts and processes a single telemetry data point from a vehicle")
    @ApiResponse(responseCode = "202", description = "Telemetry data accepted for processing")
    @ApiResponse(responseCode = "400", description = "Invalid telemetry data")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<String> ingestTelemetryData(@Valid @RequestBody TelemetryData telemetryData) {
        try {
            logger.info("Received telemetry data ingestion request for vehicle: {}", telemetryData.getVehicleId());

            telemetryService.processTelemetryData(telemetryData);

            return ResponseEntity.accepted()
                    .body("Telemetry data accepted for processing");

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid telemetry data received: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body("Invalid telemetry data: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Error processing telemetry data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error while processing telemetry data");
        }
    }

    /**
     * Endpoint for ingesting multiple telemetry data points in batch.
     * This is optimized for high-throughput scenarios.
     *
     * @param telemetryBatch Array of telemetry data points
     * @return HTTP response with processing results
     */
    @PostMapping("/ingest/batch")
    @Operation(summary = "Ingest telemetry data batch", description = "Accepts and processes multiple telemetry data points from vehicles")
    @ApiResponse(responseCode = "202", description = "Batch telemetry data accepted for processing")
    @ApiResponse(responseCode = "400", description = "Invalid telemetry data in batch")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    public ResponseEntity<String> ingestTelemetryBatch(@Valid @RequestBody TelemetryData[] telemetryBatch) {
        try {
            logger.info("Received batch telemetry ingestion request with {} items", telemetryBatch.length);

            int processedCount = 0;
            int failedCount = 0;

            for (TelemetryData data : telemetryBatch) {
                try {
                    telemetryService.processTelemetryData(data);
                    processedCount++;
                } catch (Exception e) {
                    logger.warn("Failed to process telemetry data for vehicle {}: {}",
                            data.getVehicleId(), e.getMessage());
                    failedCount++;
                }
            }

            String message = String.format("Batch processing completed. Processed: %d, Failed: %d",
                    processedCount, failedCount);

            logger.info(message);
            return ResponseEntity.accepted().body(message);

        } catch (Exception e) {
            logger.error("Error processing telemetry batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error while processing telemetry batch");
        }
    }

    /**
     * Health check endpoint for monitoring service availability.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service health status")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Data Ingestion Service is healthy");
    }
}
