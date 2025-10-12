package com.vehicletelemetry.streaming.service;

import java.time.Instant;
import java.util.Random;

import org.springframework.stereotype.Component;

import com.vehicletelemetry.streaming.model.TelemetryData;

/**
 * TelemetryDataGenerator - Generates Realistic Vehicle Telemetry Data
 * 
 * This component is responsible for generating realistic telemetry data
 * for vehicles. It uses proper object-oriented design with clear
 * separation of concerns.
 * 
 * Clean Architecture: This is part of the use case layer.
 */
@Component
public class TelemetryDataGenerator {

    private static final double NEW_YORK_LATITUDE = 40.7128;
    private static final double NEW_YORK_LONGITUDE = -74.0060;
    private static final double GPS_VARIATION_RADIUS = 0.01;
    private static final double NORMAL_SPEED_MEAN = 50.0;
    private static final double NORMAL_SPEED_STANDARD_DEVIATION = 15.0;
    private static final double MIN_FUEL_LEVEL = 5.0;
    private static final double MAX_FUEL_LEVEL = 100.0;
    private static final double FUEL_DEPLETION_RATE = 20.0;
    private static final double NORMAL_ENGINE_TEMP = 80.0;
    private static final double ENGINE_TEMP_VARIATION = 5.0;
    private static final double NORMAL_TIRE_PRESSURE = 30.0;
    private static final double TIRE_PRESSURE_VARIATION = 2.0;
    private static final double MAX_TOTAL_DISTANCE = 50000.0;

    private final Random randomNumberGenerator;
    private final VehicleIdGenerator vehicleIdGenerator;

    public TelemetryDataGenerator(VehicleIdGenerator vehicleIdGenerator) {
        this.randomNumberGenerator = new Random();
        this.vehicleIdGenerator = vehicleIdGenerator;
    }

    /**
     * Generates telemetry data for a random vehicle
     * 
     * @return TelemetryData for a randomly selected vehicle
     */
    public TelemetryData generateRandomVehicleTelemetry() {
        String randomVehicleId = vehicleIdGenerator.generateRandomVehicleId();
        return generateTelemetryForVehicle(randomVehicleId);
    }

    /**
     * Generates telemetry data for a specific vehicle
     * 
     * @param vehicleId The specific vehicle to generate data for
     * @return TelemetryData for the specified vehicle
     */
    public TelemetryData generateTelemetryForVehicle(String vehicleId) {
        Instant currentTimestamp = Instant.now();
        double latitude = generateRealisticLatitude();
        double longitude = generateRealisticLongitude();
        double speedKmh = generateRealisticSpeed();
        double fuelLevelPercentage = generateRealisticFuelLevel();
        double engineTemperatureCelsius = generateRealisticEngineTemperature();
        double tirePressurePsi = generateRealisticTirePressure();
        double totalDistanceKm = generateRealisticTotalDistance();

        return new TelemetryData(
                vehicleId,
                currentTimestamp,
                latitude,
                longitude,
                speedKmh,
                fuelLevelPercentage,
                engineTemperatureCelsius,
                tirePressurePsi,
                totalDistanceKm);
    }

    /**
     * Generates realistic GPS latitude around New York area
     */
    private double generateRealisticLatitude() {
        return NEW_YORK_LATITUDE + (randomNumberGenerator.nextGaussian() * GPS_VARIATION_RADIUS);
    }

    /**
     * Generates realistic GPS longitude around New York area
     */
    private double generateRealisticLongitude() {
        return NEW_YORK_LONGITUDE + (randomNumberGenerator.nextGaussian() * GPS_VARIATION_RADIUS);
    }

    /**
     * Generates realistic vehicle speed using normal distribution
     */
    private double generateRealisticSpeed() {
        double speed = NORMAL_SPEED_MEAN + (randomNumberGenerator.nextGaussian() * NORMAL_SPEED_STANDARD_DEVIATION);
        return Math.max(0.0, speed); // Speed cannot be negative
    }

    /**
     * Generates realistic fuel level (decreasing over time)
     */
    private double generateRealisticFuelLevel() {
        double fuelLevel = MAX_FUEL_LEVEL - randomNumberGenerator.nextInt((int) FUEL_DEPLETION_RATE);
        return Math.max(MIN_FUEL_LEVEL, fuelLevel);
    }

    /**
     * Generates realistic engine temperature
     */
    private double generateRealisticEngineTemperature() {
        return NORMAL_ENGINE_TEMP + (randomNumberGenerator.nextGaussian() * ENGINE_TEMP_VARIATION);
    }

    /**
     * Generates realistic tire pressure
     */
    private double generateRealisticTirePressure() {
        return NORMAL_TIRE_PRESSURE + (randomNumberGenerator.nextGaussian() * TIRE_PRESSURE_VARIATION);
    }

    /**
     * Generates realistic total distance traveled
     */
    private double generateRealisticTotalDistance() {
        return randomNumberGenerator.nextDouble() * MAX_TOTAL_DISTANCE;
    }
}