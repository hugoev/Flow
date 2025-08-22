package com.vehicletelemetry.gateway.model;

import java.time.Instant;

/**
 * ProcessedTelemetryData - API Gateway Model
 *
 * Represents processed telemetry data as received from the Data Processing
 * Service.
 * This is a simplified DTO for service-to-service communication.
 *
 * Clean Architecture: This is part of the infrastructure layer that handles
 * external service communication data models.
 */
public class ProcessedTelemetryData {

    private String vehicleId;
    private Instant timestamp;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double fuelLevel;
    private Double engineTemp;
    private Double tirePressure;

    // Default constructor required for JSON deserialization
    public ProcessedTelemetryData() {
    }

    public ProcessedTelemetryData(String vehicleId, Instant timestamp,
            Double latitude, Double longitude,
            Double speed, Double fuelLevel,
            Double engineTemp, Double tirePressure) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.engineTemp = engineTemp;
        this.tirePressure = tirePressure;
    }

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

    @Override
    public String toString() {
        return "ProcessedTelemetryData{" +
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

