package com.vehicletelemetry.ingestion.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

/**
 * TelemetryData - Domain Model
 *
 * Represents the telemetry data collected from vehicles.
 * This model follows clean architecture principles by being a pure
 * domain object with no dependencies on external frameworks.
 *
 * Single Responsibility: This class only represents the structure
 * and validation rules for telemetry data.
 */
public class TelemetryData {

    @JsonProperty("vehicleId")
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;

    @JsonProperty("timestamp")
    @NotNull(message = "Timestamp is required")
    @PastOrPresent(message = "Timestamp must not be in the future")
    private Instant timestamp;

    @JsonProperty("latitude")
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @JsonProperty("longitude")
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    @JsonProperty("speed")
    @Min(value = 0, message = "Speed must be non-negative")
    @Max(value = 200, message = "Speed must be reasonable (max 200 km/h)")
    private Double speed;

    @JsonProperty("fuelLevel")
    @Min(value = 0, message = "Fuel level must be non-negative")
    @Max(value = 100, message = "Fuel level must be between 0 and 100")
    private Double fuelLevel;

    @JsonProperty("engineTemp")
    @Min(value = -50, message = "Engine temperature must be reasonable")
    @Max(value = 200, message = "Engine temperature must be reasonable")
    private Double engineTemp;

    @JsonProperty("tirePressure")
    @Min(value = 0, message = "Tire pressure must be positive")
    @Max(value = 50, message = "Tire pressure must be reasonable")
    private Double tirePressure;

    @JsonProperty("totalDistance")
    @Min(value = 0, message = "Total distance must be non-negative")
    private Double totalDistance;

    // Default constructor required for JSON deserialization
    public TelemetryData() {
    }

    // Constructor for creating instances programmatically
    public TelemetryData(String vehicleId, Instant timestamp, Double latitude,
            Double longitude, Double speed, Double fuelLevel,
            Double engineTemp, Double tirePressure, Double totalDistance) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.engineTemp = engineTemp;
        this.tirePressure = tirePressure;
        this.totalDistance = totalDistance;
    }

    // Getters and setters with clear documentation
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Double fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Double getEngineTemp() {
        return engineTemp;
    }

    public void setEngineTemp(Double engineTemp) {
        this.engineTemp = engineTemp;
    }

    public Double getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(Double tirePressure) {
        this.tirePressure = tirePressure;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    @Override
    public String toString() {
        return "TelemetryData{" +
                "vehicleId='" + vehicleId + '\'' +
                ", timestamp=" + timestamp +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", fuelLevel=" + fuelLevel +
                ", engineTemp=" + engineTemp +
                ", tirePressure=" + tirePressure +
                '}';
    }
}
