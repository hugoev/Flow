package com.vehicletelemetry.ingestion.service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Vehicle Simulator Service
 * 
 * Simulates real vehicle behavior by sending MQTT messages
 * that mimic actual vehicle telemetry data patterns.
 * 
 * Real-world simulation features:
 * - Realistic driving patterns (acceleration, deceleration, stops)
 * - Environmental factors (temperature, weather)
 * - Vehicle maintenance cycles
 * - Emergency scenarios
 * - Fleet management patterns
 */
@Service
public class VehicleSimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleSimulatorService.class);

    @Value("${mqtt.broker.url:tcp://mosquitto:1883}")
    private String mqttBrokerUrl;

    @Value("${mqtt.topic.prefix:vehicles}")
    private String topicPrefix;

    @Value("${mqtt.qos:1}")
    private int qos;

    private MqttClient mqttClient;
    private ScheduledExecutorService scheduler;
    private final ObjectMapper objectMapper;
    private Random random;

    // Vehicle fleet data - 100+ vehicles for realistic simulation
    private final String[] vehicleIds = generateVehicleIds(100);

    private final String[] vehicleTypes = {
            "sedan", "suv", "truck", "van", "motorcycle"
    };

    private final String[] drivers = generateDriverNames(100);

    public VehicleSimulatorService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(100); // Increased thread pool for 100+ vehicles
    }

    /**
     * Generate vehicle IDs for realistic fleet simulation
     * Creates 100+ vehicles with realistic ID patterns
     */
    private String[] generateVehicleIds(int count) {
        String[] ids = new String[count];
        for (int i = 0; i < count; i++) {
            // Generate realistic vehicle IDs: VH001, VH002, ..., VH100
            ids[i] = String.format("VH%03d", i + 1);
        }
        return ids;
    }

    /**
     * Generate driver names for realistic fleet simulation
     * Creates 100+ unique driver names
     */
    private String[] generateDriverNames(int count) {
        String[] firstNames = {
                "John", "Sarah", "Mike", "Lisa", "David", "Emma", "Chris", "Anna", "Tom", "Kate",
                "Alex", "Maria", "James", "Jennifer", "Robert", "Linda", "Michael", "Elizabeth", "William", "Patricia",
                "Richard", "Jennifer", "Charles", "Maria", "Joseph", "Susan", "Thomas", "Karen", "Christopher", "Nancy",
                "Daniel", "Betty", "Paul", "Helen", "Mark", "Sandra", "Donald", "Donna", "Steven", "Carol",
                "Andrew", "Ruth", "Joshua", "Sharon", "Kenneth", "Michelle", "Kevin", "Laura", "Brian", "Sarah",
                "George", "Kimberly", "Edward", "Deborah", "Ronald", "Dorothy", "Timothy", "Lisa", "Jason", "Nancy",
                "Jeffrey", "Karen", "Ryan", "Betty", "Jacob", "Helen", "Gary", "Sandra", "Nicholas", "Donna",
                "Eric", "Carol", "Jonathan", "Ruth", "Stephen", "Sharon", "Larry", "Michelle", "Justin", "Laura",
                "Scott", "Sarah", "Brandon", "Kimberly", "Benjamin", "Deborah", "Samuel", "Dorothy", "Gregory", "Lisa",
                "Alexander", "Nancy", "Patrick", "Karen", "Jack", "Betty", "Dennis", "Helen", "Jerry", "Sandra"
        };

        String[] lastNames = {
                "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson",
                "Martin",
                "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
                "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
                "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts",
                "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker", "Cruz", "Edwards", "Collins", "Reyes",
                "Stewart", "Morris", "Morales", "Murphy", "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper",
                "Peterson", "Bailey", "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson",
                "Watson", "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza", "Ruiz", "Hughes",
                "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers", "Long", "Ross", "Foster", "Jimenez"
        };

        String[] drivers = new String[count];
        for (int i = 0; i < count; i++) {
            String firstName = firstNames[i % firstNames.length];
            String lastName = lastNames[i % lastNames.length];
            drivers[i] = firstName + " " + lastName;
        }
        return drivers;
    }

    /**
     * Start vehicle simulation
     * Begins sending realistic telemetry data for all vehicles
     */
    public void startSimulation() {
        try {
            // Recreate scheduler if it's been terminated
            if (scheduler == null || scheduler.isShutdown()) {
                logger.info("Recreating scheduler for new simulation cycle");
                scheduler = Executors.newScheduledThreadPool(100); // Increased thread pool for 100+ vehicles
            }

            initializeMqttClient();

            logger.info("Starting vehicle simulation for {} vehicles", vehicleIds.length);

            // Start simulation for each vehicle
            for (String vehicleId : vehicleIds) {
                startVehicleSimulation(vehicleId);
            }

            logger.info("Vehicle simulation started successfully");

        } catch (Exception e) {
            logger.error("Failed to start vehicle simulation: {}", e.getMessage(), e);
        }
    }

    /**
     * Stop vehicle simulation
     */
    public void stopSimulation() {
        try {
            if (scheduler != null) {
                scheduler.shutdown();
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            }

            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }

            logger.info("Vehicle simulation stopped");

        } catch (Exception e) {
            logger.error("Error stopping simulation: {}", e.getMessage(), e);
        }
    }

    /**
     * Initialize MQTT client
     */
    private void initializeMqttClient() throws MqttException {
        String clientId = "vehicle-simulator-" + System.currentTimeMillis();
        mqttClient = new MqttClient(mqttBrokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);

        mqttClient.connect(options);
        logger.info("MQTT client connected to broker: {}", mqttBrokerUrl);
    }

    /**
     * Start simulation for a specific vehicle
     */
    private void startVehicleSimulation(String vehicleId) {
        // Telemetry data every 5 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendTelemetryData(vehicleId);
            } catch (Exception e) {
                logger.error("Error sending telemetry for vehicle {}: {}", vehicleId, e.getMessage());
            }
        }, 0, 5, TimeUnit.SECONDS);

        // Status updates every 30 seconds
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sendStatusUpdate(vehicleId);
            } catch (Exception e) {
                logger.error("Error sending status for vehicle {}: {}", vehicleId, e.getMessage());
            }
        }, 15, 30, TimeUnit.SECONDS);

        // Occasional alerts
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (random.nextDouble() < 0.1) { // 10% chance of alert
                    sendAlert(vehicleId);
                }
            } catch (Exception e) {
                logger.error("Error sending alert for vehicle {}: {}", vehicleId, e.getMessage());
            }
        }, 60, 120, TimeUnit.SECONDS);
    }

    /**
     * Send telemetry data for a vehicle
     */
    private void sendTelemetryData(String vehicleId) throws Exception {
        VehicleTelemetryData data = generateTelemetryData(vehicleId);
        String topic = topicPrefix + "/" + vehicleId + "/telemetry";
        String payload = objectMapper.writeValueAsString(data);

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        message.setRetained(false);

        mqttClient.publish(topic, message);
        logger.debug("Sent telemetry for vehicle {}: speed={}, fuel={}%",
                vehicleId, data.getSpeed(), data.getFuelLevel());
    }

    /**
     * Send status update for a vehicle
     */
    private void sendStatusUpdate(String vehicleId) throws Exception {
        VehicleStatusData status = generateStatusData(vehicleId);
        String topic = topicPrefix + "/" + vehicleId + "/status";
        String payload = objectMapper.writeValueAsString(status);

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        message.setRetained(true); // Retain status messages

        mqttClient.publish(topic, message);
        logger.debug("Sent status for vehicle {}: {}", vehicleId, status.getStatus());
    }

    /**
     * Send alert for a vehicle
     */
    private void sendAlert(String vehicleId) throws Exception {
        VehicleAlertData alert = generateAlertData(vehicleId);
        String topic = topicPrefix + "/" + vehicleId + "/alerts";
        String payload = objectMapper.writeValueAsString(alert);

        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(2); // Highest QoS for alerts
        message.setRetained(false);

        mqttClient.publish(topic, message);
        logger.warn("Sent alert for vehicle {}: {}", vehicleId, alert.getAlertType());
    }

    /**
     * Generate realistic telemetry data
     */
    public VehicleTelemetryData generateTelemetryData(String vehicleId) {
        VehicleTelemetryData data = new VehicleTelemetryData();
        data.setVehicleId(vehicleId);
        data.setTimestamp(Instant.now());

        // Realistic speed patterns (0-120 km/h)
        data.setSpeed(Math.max(0, random.nextGaussian() * 15 + 50));

        // Fuel level (decreasing over time)
        data.setFuelLevel(Math.max(5, 100 - random.nextInt(20)));

        // Engine temperature (80-100°C normal range)
        data.setEngineTemp(80 + random.nextGaussian() * 5);

        // Tire pressure (30-35 PSI)
        data.setTirePressure(30 + random.nextGaussian() * 2);

        // GPS coordinates (simulate movement)
        data.setLatitude(40.7128 + random.nextGaussian() * 0.01);
        data.setLongitude(-74.0060 + random.nextGaussian() * 0.01);

        // Distance traveled
        data.setTotalDistance(data.getTotalDistance() + random.nextDouble() * 0.5);

        return data;
    }

    /**
     * Generate status data
     */
    public VehicleStatusData generateStatusData(String vehicleId) {
        VehicleStatusData status = new VehicleStatusData();
        status.setVehicleId(vehicleId);
        status.setTimestamp(Instant.now());
        status.setStatus(random.nextDouble() < 0.9 ? "online" : "offline");
        status.setDriver(drivers[random.nextInt(drivers.length)]);
        status.setVehicleType(vehicleTypes[random.nextInt(vehicleTypes.length)]);
        status.setLocation("New York, NY");
        return status;
    }

    /**
     * Generate alert data
     */
    public VehicleAlertData generateAlertData(String vehicleId) {
        String[] alertTypes = {
                "low_fuel", "high_temperature", "low_tire_pressure",
                "engine_warning", "brake_system", "maintenance_due"
        };

        VehicleAlertData alert = new VehicleAlertData();
        alert.setVehicleId(vehicleId);
        alert.setTimestamp(Instant.now());
        alert.setAlertType(alertTypes[random.nextInt(alertTypes.length)]);
        alert.setSeverity(random.nextDouble() < 0.3 ? "critical" : "warning");
        alert.setMessage("Vehicle " + vehicleId + " requires attention: " + alert.getAlertType());
        return alert;
    }

    // Data classes for MQTT messages
    public static class VehicleTelemetryData {
        private String vehicleId;
        private Instant timestamp;
        private double speed;
        private double fuelLevel;
        private double engineTemp;
        private double tirePressure;
        private double latitude;
        private double longitude;
        private double totalDistance;

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

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public double getFuelLevel() {
            return fuelLevel;
        }

        public void setFuelLevel(double fuelLevel) {
            this.fuelLevel = fuelLevel;
        }

        public double getEngineTemp() {
            return engineTemp;
        }

        public void setEngineTemp(double engineTemp) {
            this.engineTemp = engineTemp;
        }

        public double getTirePressure() {
            return tirePressure;
        }

        public void setTirePressure(double tirePressure) {
            this.tirePressure = tirePressure;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public void setTotalDistance(double totalDistance) {
            this.totalDistance = totalDistance;
        }
    }

    public static class VehicleStatusData {
        private String vehicleId;
        private Instant timestamp;
        private String status;
        private String driver;
        private String vehicleType;
        private String location;

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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getVehicleType() {
            return vehicleType;
        }

        public void setVehicleType(String vehicleType) {
            this.vehicleType = vehicleType;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }
    }

    public static class VehicleAlertData {
        private String vehicleId;
        private Instant timestamp;
        private String alertType;
        private String severity;
        private String message;

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

        public String getAlertType() {
            return alertType;
        }

        public void setAlertType(String alertType) {
            this.alertType = alertType;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
