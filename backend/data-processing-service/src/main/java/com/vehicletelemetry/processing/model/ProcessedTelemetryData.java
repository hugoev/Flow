package com.vehicletelemetry.processing.model;

import java.time.Instant;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * ProcessedTelemetryData - Cassandra Entity
 *
 * Represents processed and stored telemetry data optimized for querying.
 * This entity is designed with Cassandra best practices in mind:
 * - Partitioned by vehicle_id for efficient vehicle-specific queries
 * - Clustered by timestamp for time-series ordering
 *
 * This design allows for efficient queries like:
 * - Get all data for a specific vehicle
 * - Get data for a vehicle within a time range
 * - Get latest data for all vehicles
 */
@Table("vehicle_telemetry")
public class ProcessedTelemetryData {

    @PrimaryKey
    private TelemetryDataKey key;

    @Column("latitude")
    private Double latitude;

    @Column("longitude")
    private Double longitude;

    @Column("speed")
    private Double speed;

    @Column("fuel_level")
    private Double fuelLevel;

    @Column("engine_temp")
    private Double engineTemp;

    @Column("tire_pressure")
    private Double tirePressure;

    // Default constructor required by Cassandra
    public ProcessedTelemetryData() {
    }

    public ProcessedTelemetryData(String vehicleId, Instant timestamp,
            Double latitude, Double longitude,
            Double speed, Double fuelLevel,
            Double engineTemp, Double tirePressure) {
        this.key = new TelemetryDataKey(vehicleId, timestamp);
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.engineTemp = engineTemp;
        this.tirePressure = tirePressure;
    }

    public TelemetryDataKey getKey() {
        return key;
    }

    public void setKey(TelemetryDataKey key) {
        this.key = key;
    }

    public String getVehicleId() {
        return key.getVehicleId();
    }

    public Instant getTimestamp() {
        return key.getTimestamp();
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
                "vehicleId='" + key.getVehicleId() + '\'' +
                ", timestamp=" + key.getTimestamp() +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", fuelLevel=" + fuelLevel +
                ", engineTemp=" + engineTemp +
                ", tirePressure=" + tirePressure +
                '}';
    }
}
