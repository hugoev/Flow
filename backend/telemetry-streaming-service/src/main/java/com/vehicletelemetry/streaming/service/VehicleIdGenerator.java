package com.vehicletelemetry.streaming.service;

import java.util.Random;

import org.springframework.stereotype.Component;

/**
 * VehicleIdGenerator - Generates Vehicle Identifiers
 * 
 * This component is responsible for generating vehicle IDs
 * following a consistent pattern. It supports both random
 * and sequential ID generation.
 * 
 * Clean Architecture: This is part of the use case layer.
 */
@Component
public class VehicleIdGenerator {

    private static final String VEHICLE_ID_PREFIX = "VH";
    private static final int VEHICLE_ID_PADDING = 3;
    private static final int MIN_VEHICLE_NUMBER = 1;
    private static final int MAX_VEHICLE_NUMBER = 100;

    private final Random randomNumberGenerator;

    public VehicleIdGenerator() {
        this.randomNumberGenerator = new Random();
    }

    /**
     * Generates a random vehicle ID from the fleet
     * 
     * @return A randomly selected vehicle ID (VH001 to VH100)
     */
    public String generateRandomVehicleId() {
        int vehicleNumber = MIN_VEHICLE_NUMBER + randomNumberGenerator.nextInt(MAX_VEHICLE_NUMBER);
        return formatVehicleId(vehicleNumber);
    }

    /**
     * Generates a vehicle ID for a specific vehicle number
     * 
     * @param vehicleNumber The vehicle number (1-100)
     * @return Formatted vehicle ID (e.g., VH001, VH042, VH100)
     */
    public String generateVehicleId(int vehicleNumber) {
        if (vehicleNumber < MIN_VEHICLE_NUMBER || vehicleNumber > MAX_VEHICLE_NUMBER) {
            throw new IllegalArgumentException(
                    String.format("Vehicle number must be between %d and %d", MIN_VEHICLE_NUMBER, MAX_VEHICLE_NUMBER));
        }
        return formatVehicleId(vehicleNumber);
    }

    /**
     * Formats a vehicle number into a proper vehicle ID
     * 
     * @param vehicleNumber The vehicle number to format
     * @return Formatted vehicle ID with zero padding
     */
    private String formatVehicleId(int vehicleNumber) {
        return String.format("%s%0" + VEHICLE_ID_PADDING + "d", VEHICLE_ID_PREFIX, vehicleNumber);
    }

    /**
     * Gets the total number of vehicles in the fleet
     * 
     * @return Total vehicle count
     */
    public int getTotalVehicleCount() {
        return MAX_VEHICLE_NUMBER;
    }

    /**
     * Gets the minimum vehicle number
     * 
     * @return Minimum vehicle number
     */
    public int getMinVehicleNumber() {
        return MIN_VEHICLE_NUMBER;
    }

    /**
     * Gets the maximum vehicle number
     * 
     * @return Maximum vehicle number
     */
    public int getMaxVehicleNumber() {
        return MAX_VEHICLE_NUMBER;
    }
}