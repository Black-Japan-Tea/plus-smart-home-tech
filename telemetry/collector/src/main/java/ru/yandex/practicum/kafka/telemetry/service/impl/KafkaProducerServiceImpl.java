package ru.yandex.practicum.kafka.telemetry.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.service.KafkaProducerService;

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
        log.debug("Sending sensor event to topic {}: {}", sensorsTopic, event);
        sensorEventKafkaTemplate.send(sensorsTopic, event.getId(), event);
    }

    @Override
    public void sendHubEvent(HubEventAvro event) {
        log.debug("Sending hub event to topic {}: {}", hubsTopic, event);
        hubEventKafkaTemplate.send(hubsTopic, event.getHubId(), event);
    }
}

