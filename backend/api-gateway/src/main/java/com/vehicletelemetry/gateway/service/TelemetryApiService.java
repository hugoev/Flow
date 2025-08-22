package com.vehicletelemetry.gateway.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vehicletelemetry.gateway.client.ProcessingServiceClient;
import com.vehicletelemetry.gateway.model.ProcessedTelemetryData;
import com.vehicletelemetry.gateway.model.TelemetryDataResponse;
import com.vehicletelemetry.gateway.model.VehicleSummary;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * TelemetryApiService - Service Layer
 *
 * Orchestrates communication with backend services and handles data
 * transformation.
 * Implements resilience patterns like circuit breakers and retries for fault
 * tolerance.
 *
 * Clean Architecture: This is part of the use case layer that orchestrates
 * data flow between external services and the presentation layer.
 */
@Service
public class TelemetryApiService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryApiService.class);
    private static final String PROCESSING_SERVICE = "processingService";

    private final ProcessingServiceClient processingClient;

    public TelemetryApiService(ProcessingServiceClient processingClient) {
        this.processingClient = processingClient;
    }

    /**
     * Retrieves telemetry data for a specific vehicle within a time range.
     * Implements circuit breaker and retry patterns for fault tolerance.
     */
    @CircuitBreaker(name = PROCESSING_SERVICE, fallbackMethod = "getVehicleTelemetryFallback")
    @Retry(name = PROCESSING_SERVICE)
    public List<TelemetryDataResponse> getVehicleTelemetry(String vehicleId, Instant startTime, Instant endTime) {
        logger.debug("Fetching telemetry data for vehicle: {} from {} to {}",
                vehicleId, startTime, endTime);

        try {
            List<ProcessedTelemetryData> rawData = processingClient.getVehicleTelemetry(
                    vehicleId, startTime, endTime);

            return rawData.stream()
                    .map(this::transformToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch telemetry data for vehicle: {}", vehicleId, e);
            throw e;
        }
    }

    /**
     * Retrieves the latest telemetry data for a specific vehicle.
     */
    @CircuitBreaker(name = PROCESSING_SERVICE, fallbackMethod = "getLatestVehicleTelemetryFallback")
    @Retry(name = PROCESSING_SERVICE)
    @Cacheable(value = "latestTelemetry", key = "#vehicleId")
    public TelemetryDataResponse getLatestVehicleTelemetry(String vehicleId) {
        logger.debug("Fetching latest telemetry data for vehicle: {}", vehicleId);

        try {
            List<ProcessedTelemetryData> rawData = processingClient.getLatestVehicleTelemetry(vehicleId);

            if (rawData.isEmpty()) {
                return null;
            }

            // Return the most recent record (first due to DESC ordering)
            return transformToResponse(rawData.get(0));

        } catch (Exception e) {
            logger.error("Failed to fetch latest telemetry data for vehicle: {}", vehicleId, e);
            throw e;
        }
    }

    /**
     * Retrieves summary information for all vehicles.
     * This aggregates data from multiple service calls.
     */
    @CircuitBreaker(name = PROCESSING_SERVICE, fallbackMethod = "getAllVehiclesSummaryFallback")
    @Retry(name = PROCESSING_SERVICE)
    @Cacheable(value = "vehicleSummaries")
    public List<VehicleSummary> getAllVehiclesSummary() {
        logger.debug("Fetching summary for all vehicles");

        try {
            List<String> vehicleIds = processingClient.getAllVehicleIds();

            return vehicleIds.stream()
                    .map(this::getVehicleSummary)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch vehicle summaries", e);
            throw e;
        }
    }

    /**
     * Creates a summary for a specific vehicle.
     * This is a helper method used by getAllVehiclesSummary.
     */
    private VehicleSummary getVehicleSummary(String vehicleId) {
        try {
            TelemetryDataResponse latestData = getLatestVehicleTelemetry(vehicleId);

            if (latestData == null) {
                return new VehicleSummary(vehicleId, null, 0L, null, null, null, null);
            }

            // In a real implementation, you would fetch the total count from the processing
            // service
            Long totalRecords = 1L; // Placeholder

            return new VehicleSummary(
                    vehicleId,
                    latestData.getTimestamp(),
                    totalRecords,
                    latestData.getLocation().getLatitude(),
                    latestData.getLocation().getLongitude(),
                    latestData.getSpeed(),
                    latestData.getFuel().getLevel());

        } catch (Exception e) {
            logger.warn("Failed to create summary for vehicle: {}", vehicleId, e);
            return new VehicleSummary(vehicleId, null, 0L, null, null, null, null);
        }
    }

    /**
     * Transforms ProcessedTelemetryData to TelemetryDataResponse.
     * This handles the mapping between internal and external data models.
     */
    private TelemetryDataResponse transformToResponse(ProcessedTelemetryData rawData) {
        return new TelemetryDataResponse(
                rawData.getVehicleId(),
                rawData.getTimestamp(),
                rawData.getLatitude(),
                rawData.getLongitude(),
                rawData.getSpeed(),
                rawData.getFuelLevel(),
                rawData.getEngineTemp(),
                rawData.getTirePressure());
    }

    // Fallback methods for circuit breaker pattern

    public List<TelemetryDataResponse> getVehicleTelemetryFallback(String vehicleId, Instant startTime,
            Instant endTime, Exception e) {
        logger.warn("Circuit breaker fallback for getVehicleTelemetry: {}", e.getMessage());
        return List.of(); // Return empty list as fallback
    }

    public TelemetryDataResponse getLatestVehicleTelemetryFallback(String vehicleId, Exception e) {
        logger.warn("Circuit breaker fallback for getLatestVehicleTelemetry: {}", e.getMessage());
        return null; // Return null as fallback
    }

    public List<VehicleSummary> getAllVehiclesSummaryFallback(Exception e) {
        logger.warn("Circuit breaker fallback for getAllVehiclesSummary: {}", e.getMessage());
        return List.of(); // Return empty list as fallback
    }
}
