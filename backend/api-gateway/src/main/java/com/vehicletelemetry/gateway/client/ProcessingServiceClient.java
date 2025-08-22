package com.vehicletelemetry.gateway.client;

import java.time.Instant;
import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.vehicletelemetry.gateway.model.ProcessedTelemetryData;

/**
 * ProcessingServiceClient - Feign Client
 *
 * Feign client for communicating with the Data Processing Service.
 * Provides a declarative HTTP client interface for service-to-service
 * communication.
 *
 * Clean Architecture: This is part of the infrastructure layer that handles
 * external service communication, while the service layer remains unaware of
 * the implementation details.
 */
@FeignClient(name = "processing-service", url = "${services.processing-service.url:http://localhost:8082}")
public interface ProcessingServiceClient {

    /**
     * Fetches telemetry data for a specific vehicle within a time range.
     * This endpoint calls the processing service's internal API.
     */
    @GetMapping("/api/processing/vehicles/{vehicleId}")
    List<ProcessedTelemetryData> getVehicleTelemetry(
            @PathVariable String vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime);

    /**
     * Fetches the latest telemetry data for a specific vehicle.
     */
    @GetMapping("/api/processing/vehicles/{vehicleId}/latest")
    List<ProcessedTelemetryData> getLatestVehicleTelemetry(@PathVariable String vehicleId);

    /**
     * Fetches all vehicles with their latest data.
     * This is used to build vehicle summaries.
     */
    @GetMapping("/api/processing/vehicles")
    List<String> getAllVehicleIds();

    /**
     * Health check for the processing service.
     */
    @GetMapping("/actuator/health")
    String getHealth();
}
