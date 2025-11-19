package ru.yandex.practicum.kafka.telemetry.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.service.KafkaProducerService;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Value("${kafka.topics.sensors:telemetry.sensors.v1}")
    private String sensorsTopic;

    @Value("${kafka.topics.hubs:telemetry.hubs.v1}")
    private String hubsTopic;

    private final KafkaTemplate<String, SensorEventAvro> sensorEventKafkaTemplate;
    private final KafkaTemplate<String, HubEventAvro> hubEventKafkaTemplate;

    @Override
    public void sendSensorEvent(SensorEventAvro event) {
        try {
            log.debug("Sending sensor event to topic {}: {}", sensorsTopic, event);
            CompletableFuture<SendResult<String, SensorEventAvro>> future = 
                sensorEventKafkaTemplate.send(sensorsTopic, event.getId(), event);
            future.get();
            log.debug("Successfully sent sensor event to topic {}", sensorsTopic);
        } catch (Exception e) {
            log.error("Error sending sensor event to topic {}: {}", sensorsTopic, event, e);
            throw new RuntimeException("Failed to send sensor event to Kafka", e);
        }
    }

    @Override
    public void sendHubEvent(HubEventAvro event) {
        try {
            log.debug("Sending hub event to topic {}: {}", hubsTopic, event);
            CompletableFuture<SendResult<String, HubEventAvro>> future = 
                hubEventKafkaTemplate.send(hubsTopic, event.getHubId(), event);
            future.get();
            log.debug("Successfully sent hub event to topic {}", hubsTopic);
        } catch (Exception e) {
            log.error("Error sending hub event to topic {}: {}", hubsTopic, event, e);
            throw new RuntimeException("Failed to send hub event to Kafka", e);
        }
    }
}

