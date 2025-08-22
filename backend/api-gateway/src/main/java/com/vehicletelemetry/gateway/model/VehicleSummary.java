package com.vehicletelemetry.gateway.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * VehicleSummary - API Response Model
 *
 * Represents summary information for a vehicle with telemetry data.
 * Provides a concise overview suitable for dashboard displays.
 *
 * Clean Architecture: This is part of the presentation layer,
 * optimized for frontend consumption and dashboard displays.
 */
public class VehicleSummary {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("lastSeen")
    private Instant lastSeen;

    @JsonProperty("totalRecords")
    private Long totalRecords;

    @JsonProperty("currentLocation")
    private TelemetryDataResponse.Location currentLocation;

    @JsonProperty("currentSpeed")
    private Double currentSpeed;

    @JsonProperty("currentFuelLevel")
    private Double currentFuelLevel;

    @JsonProperty("alerts")
    private Integer alerts = 0;

    // Default constructor required for JSON deserialization
    public VehicleSummary() {
    }

    public VehicleSummary(String vehicleId, Instant lastSeen, Long totalRecords,
            Double latitude, Double longitude, Double speed, Double fuelLevel) {
        this.vehicleId = vehicleId;
        this.lastSeen = lastSeen;
        this.totalRecords = totalRecords;
        this.currentLocation = new TelemetryDataResponse.Location(latitude, longitude);
        this.currentSpeed = speed;
        this.currentFuelLevel = fuelLevel;
    }

    // Getters and setters
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public TelemetryDataResponse.Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(TelemetryDataResponse.Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(Double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public Double getCurrentFuelLevel() {
        return currentFuelLevel;
    }

    public void setCurrentFuelLevel(Double currentFuelLevel) {
        this.currentFuelLevel = currentFuelLevel;
    }

    public Integer getAlerts() {
        return alerts;
    }

    public void setAlerts(Integer alerts) {
        this.alerts = alerts;
    }

    /**
     * Checks if the vehicle has any active alerts based on current data.
     * This is a simple implementation that could be extended with more
     * sophisticated logic.
     */
    public boolean hasAlerts() {
        return alerts > 0;
    }

    /**
     * Adds an alert to the vehicle summary.
     */
    public void addAlert() {
        this.alerts++;
    }
}
