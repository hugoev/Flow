package com.vehicletelemetry.ingestion.service;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

/**
 * Auto Simulation Service
 * 
 * Automatically manages vehicle simulation lifecycle:
 * - Starts simulation when the service is ready
 * - Restarts simulation every 2 minutes to generate fresh data
 * - Provides continuous realistic telemetry data for testing
 * - No manual intervention required
 */
@Service
public class AutoSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(AutoSimulationService.class);

    @Autowired
    private VehicleSimulatorService vehicleSimulatorService;

    private ScheduledExecutorService autoRestartScheduler;
    private boolean isSimulationRunning = false;
    private int restartCount = 0;

    /**
     * Start auto-simulation when the application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startAutoSimulation() {
        logger.info("🚀 Auto-simulation service starting...");

        // Initialize the auto-restart scheduler
        autoRestartScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auto-simulation-restart");
            t.setDaemon(true);
            return t;
        });

        // Start the first simulation immediately
        startSimulationCycle();

        // Schedule automatic restarts every 2 minutes
        autoRestartScheduler.scheduleAtFixedRate(
                this::restartSimulationCycle,
                2, // Initial delay: 2 minutes
                2, // Repeat every: 2 minutes
                TimeUnit.MINUTES);

        logger.info("✅ Auto-simulation service started - will restart every 2 minutes");
    }

    /**
     * Start a new simulation cycle
     */
    private void startSimulationCycle() {
        try {
            if (isSimulationRunning) {
                logger.info("🔄 Stopping previous simulation...");
                vehicleSimulatorService.stopSimulation();
                Thread.sleep(2000); // Wait 2 seconds for cleanup
            }

            logger.info("🎯 Starting simulation cycle #{} at {}",
                    ++restartCount, Instant.now());

            vehicleSimulatorService.startSimulation();
            isSimulationRunning = true;

            logger.info("✅ Simulation cycle #{} started successfully", restartCount);

        } catch (Exception e) {
            logger.error("❌ Failed to start simulation cycle #{}: {}", restartCount, e.getMessage());
        }
    }

    /**
     * Restart the simulation cycle
     */
    private void restartSimulationCycle() {
        logger.info("🔄 Auto-restarting simulation cycle...");
        startSimulationCycle();
    }

    /**
     * Get simulation status
     */
    public String getSimulationStatus() {
        if (isSimulationRunning) {
            return String.format("Running (Cycle #%d, Auto-restart every 2 minutes)", restartCount);
        } else {
            return "Stopped";
        }
    }

    /**
     * Get restart count
     */
    public int getRestartCount() {
        return restartCount;
    }

    /**
     * Check if simulation is running
     */
    public boolean isSimulationRunning() {
        return isSimulationRunning;
    }

    /**
     * Manually stop auto-simulation
     */
    public void stopAutoSimulation() {
        logger.info("🛑 Stopping auto-simulation service...");

        if (autoRestartScheduler != null) {
            autoRestartScheduler.shutdown();
        }

        if (isSimulationRunning) {
            vehicleSimulatorService.stopSimulation();
            isSimulationRunning = false;
        }

        logger.info("✅ Auto-simulation service stopped");
    }

    /**
     * Cleanup on application shutdown
     */
    @PreDestroy
    public void cleanup() {
        stopAutoSimulation();
    }
}
