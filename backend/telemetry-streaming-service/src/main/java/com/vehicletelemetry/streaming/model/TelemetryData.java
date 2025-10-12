package com.vehicletelemetry.streaming.model;

import java.time.Instant;

/**
 * TelemetryData - Domain Model for Vehicle Telemetry
 * 
 * Represents real-time telemetry data from a vehicle.
 * This is a value object that encapsulates all telemetry information.
 * 
 * Clean Architecture: This is part of the domain layer.
 */
public class TelemetryData {

    private final String vehicleId;
    private final Instant timestamp;
    private final double latitude;
    private final double longitude;
    private final double speedKmh;
    private final double fuelLevelPercentage;
    private final double engineTemperatureCelsius;
    private final double tirePressurePsi;
    private final double totalDistanceKm;

    /**
     * Constructor for TelemetryData
     * 
     * @param vehicleId                Unique identifier for the vehicle
     * @param timestamp                When the telemetry data was captured
     * @param latitude                 GPS latitude coordinate
     * @param longitude                GPS longitude coordinate
     * @param speedKmh                 Current speed in kilometers per hour
     * @param fuelLevelPercentage      Fuel level as percentage (0-100)
     * @param engineTemperatureCelsius Engine temperature in Celsius
     * @param tirePressurePsi          Tire pressure in PSI
     * @param totalDistanceKm          Total distance traveled in kilometers
     */
    public TelemetryData(String vehicleId, Instant timestamp, double latitude, double longitude,
            double speedKmh, double fuelLevelPercentage, double engineTemperatureCelsius,
            double tirePressurePsi, double totalDistanceKm) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speedKmh = speedKmh;
        this.fuelLevelPercentage = fuelLevelPercentage;
        this.engineTemperatureCelsius = engineTemperatureCelsius;
        this.tirePressurePsi = tirePressurePsi;
        this.totalDistanceKm = totalDistanceKm;
    }

    // Getters - Immutable object, no setters
    public String getVehicleId() {
        return vehicleId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getSpeedKmh() {
        return speedKmh;
    }

    public double getFuelLevelPercentage() {
        return fuelLevelPercentage;
    }

    public double getEngineTemperatureCelsius() {
        return engineTemperatureCelsius;
    }

    public double getTirePressurePsi() {
        return tirePressurePsi;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    @Override
    public String toString() {
        return String.format("TelemetryData{vehicleId='%s', timestamp=%s, speed=%.1f km/h, fuel=%.1f%%, temp=%.1f°C}",
                vehicleId, timestamp, speedKmh, fuelLevelPercentage, engineTemperatureCelsius);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        TelemetryData that = (TelemetryData) obj;
        return vehicleId.equals(that.vehicleId) && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        return vehicleId.hashCode() + timestamp.hashCode();
    }
}

