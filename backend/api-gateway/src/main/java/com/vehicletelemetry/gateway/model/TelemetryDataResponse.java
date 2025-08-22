package com.vehicletelemetry.gateway.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TelemetryDataResponse - API Response Model
 *
 * Represents the telemetry data returned by the API.
 * This is the external representation optimized for API consumers.
 *
 * Clean Architecture: This is part of the presentation layer,
 * providing a stable API contract that shields consumers from
 * internal domain model changes.
 */
public class TelemetryDataResponse {

    @JsonProperty("vehicleId")
    private String vehicleId;

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("location")
    private Location location;

    @JsonProperty("speed")
    private Double speed;

    @JsonProperty("fuel")
    private FuelInfo fuel;

    @JsonProperty("engine")
    private EngineInfo engine;

    @JsonProperty("tires")
    private TireInfo tires;

    // Default constructor required for JSON deserialization
    public TelemetryDataResponse() {
    }

    public TelemetryDataResponse(String vehicleId, Instant timestamp, Double latitude,
            Double longitude, Double speed, Double fuelLevel,
            Double engineTemp, Double tirePressure) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.location = new Location(latitude, longitude);
        this.speed = speed;
        this.fuel = new FuelInfo(fuelLevel);
        this.engine = new EngineInfo(engineTemp);
        this.tires = new TireInfo(tirePressure);
    }

    // Getters and setters
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public FuelInfo getFuel() {
        return fuel;
    }

    public void setFuel(FuelInfo fuel) {
        this.fuel = fuel;
    }

    public EngineInfo getEngine() {
        return engine;
    }

    public void setEngine(EngineInfo engine) {
        this.engine = engine;
    }

    public TireInfo getTires() {
        return tires;
    }

    public void setTires(TireInfo tires) {
        this.tires = tires;
    }

    /**
     * Location - Nested class for GPS coordinates
     */
    public static class Location {
        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        public Location() {
        }

        public Location(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
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
    }

    /**
     * FuelInfo - Nested class for fuel-related data
     */
    public static class FuelInfo {
        @JsonProperty("level")
        private Double level;

        @JsonProperty("percentage")
        private Double percentage;

        public FuelInfo() {
        }

        public FuelInfo(Double level) {
            this.level = level;
            this.percentage = level; // Assuming level is already a percentage
        }

        public Double getLevel() {
            return level;
        }

        public void setLevel(Double level) {
            this.level = level;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    /**
     * EngineInfo - Nested class for engine-related data
     */
    public static class EngineInfo {
        @JsonProperty("temperature")
        private Double temperature;

        @JsonProperty("temperatureUnit")
        private String temperatureUnit = "Celsius";

        public EngineInfo() {
        }

        public EngineInfo(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public String getTemperatureUnit() {
            return temperatureUnit;
        }

        public void setTemperatureUnit(String temperatureUnit) {
            this.temperatureUnit = temperatureUnit;
        }
    }

    /**
     * TireInfo - Nested class for tire-related data
     */
    public static class TireInfo {
        @JsonProperty("pressure")
        private Double pressure;

        @JsonProperty("pressureUnit")
        private String pressureUnit = "PSI";

        public TireInfo() {
        }

        public TireInfo(Double pressure) {
            this.pressure = pressure;
        }

        public Double getPressure() {
            return pressure;
        }

        public void setPressure(Double pressure) {
            this.pressure = pressure;
        }

        public String getPressureUnit() {
            return pressureUnit;
        }

        public void setPressureUnit(String pressureUnit) {
            this.pressureUnit = pressureUnit;
        }
    }
}
