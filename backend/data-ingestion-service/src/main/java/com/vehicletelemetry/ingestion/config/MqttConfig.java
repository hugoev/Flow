package com.vehicletelemetry.ingestion.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;

import com.vehicletelemetry.ingestion.service.TelemetryDataProcessor;

/**
 * MQTT Configuration for Vehicle Telemetry Ingestion
 * 
 * This configuration sets up MQTT client to receive vehicle telemetry data
 * from vehicles using the industry-standard MQTT protocol.
 * 
 * Real-world usage:
 * - Tesla uses MQTT for vehicle communication
 * - BMW uses MQTT for connected car services
 * - Uber uses MQTT for fleet management
 */
@Configuration
public class MqttConfig {

    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Value("${mqtt.broker.url:tcp://mosquitto:1883}")
    private String mqttBrokerUrl;

    @Value("${mqtt.topic.prefix:vehicles}")
    private String topicPrefix;

    @Value("${mqtt.qos:1}")
    private int qos;

    @Value("${mqtt.client.id:vehicle-telemetry-ingestion}")
    private String clientId;

    @Autowired
    private TelemetryDataProcessor telemetryDataProcessor;

    /**
     * MQTT Client Factory Configuration
     * Sets up connection options for reliable vehicle communication
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { mqttBrokerUrl });
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        options.setMaxInflight(100);

        factory.setConnectionOptions(options);

        logger.info("MQTT Client Factory configured for broker: {}", mqttBrokerUrl);
        return factory;
    }

    /**
     * MQTT Input Channel
     * Channel for receiving MQTT messages from vehicles
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT Message Producer
     * Subscribes to vehicle telemetry topics and forwards messages to processing
     */
    @Bean
    public MessageProducer mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId + "-inbound",
                mqttClientFactory(),
                topicPrefix + "/+/telemetry", // vehicles/{vehicleId}/telemetry
                topicPrefix + "/+/status", // vehicles/{vehicleId}/status
                topicPrefix + "/+/alerts" // vehicles/{vehicleId}/alerts
        );

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(qos);
        adapter.setOutputChannel(mqttInputChannel());

        logger.info("MQTT Inbound adapter configured for topics: {}/*", topicPrefix);
        return adapter;
    }

    /**
     * MQTT Message Handler
     * Processes incoming MQTT messages from vehicles
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttMessageHandler() {
        return message -> {
            try {
                String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
                String payload = (String) message.getPayload();

                logger.info("Received MQTT message from topic: {}", topic);
                logger.debug("MQTT payload: {}", payload);

                // Extract vehicle ID from topic
                String vehicleId = extractVehicleIdFromTopic(topic);

                // Process the telemetry data
                processVehicleTelemetry(vehicleId, topic, payload);

            } catch (Exception e) {
                logger.error("Error processing MQTT message: {}", e.getMessage(), e);
            }
        };
    }

    /**
     * Extract vehicle ID from MQTT topic
     * Topics format: vehicles/{vehicleId}/telemetry
     */
    private String extractVehicleIdFromTopic(String topic) {
        if (topic != null && topic.contains("/")) {
            String[] parts = topic.split("/");
            if (parts.length >= 2) {
                return parts[1]; // vehicles/{vehicleId}/telemetry -> vehicleId
            }
        }
        return "unknown";
    }

    /**
     * Process vehicle telemetry data
     * Forwards MQTT data to Kafka for processing using loose coupling
     */
    private void processVehicleTelemetry(String vehicleId, String topic, String payload) {
        logger.info("Processing telemetry for vehicle: {} from topic: {}", vehicleId, topic);

        try {
            // Use dependency injection for loose coupling
            if (topic.contains("telemetry")) {
                telemetryDataProcessor.processTelemetryData(vehicleId, topic, payload);
            } else if (topic.contains("status")) {
                telemetryDataProcessor.processStatusData(vehicleId, topic, payload);
            } else if (topic.contains("alerts")) {
                telemetryDataProcessor.processAlertData(vehicleId, topic, payload);
            }

            logger.debug("Successfully processed MQTT data for vehicle: {}", vehicleId);
        } catch (Exception e) {
            logger.error("Failed to process MQTT data for vehicle {}: {}", vehicleId, e.getMessage());
        }
    }
}
