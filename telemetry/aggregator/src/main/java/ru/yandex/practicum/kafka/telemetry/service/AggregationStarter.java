package ru.yandex.practicum.kafka.telemetry.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.config.AvroSerializer;
import ru.yandex.practicum.kafka.telemetry.deserializer.SensorEventDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class AggregationStarter {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topics.sensors:telemetry.sensors.v1}")
    private String sensorsTopic;

    @Value("${kafka.topics.snapshots:telemetry.snapshots.v1}")
    private String snapshotsTopic;

    @Value("${kafka.consumer.group-id:aggregator-group}")
    private String groupId;

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public void start() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SensorEventDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        producerProps.put(ProducerConfig.RETRIES_CONFIG, 3);

        try (KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerProps);
             KafkaProducer<String, SensorsSnapshotAvro> producer = new KafkaProducer<>(producerProps)) {

            consumer.subscribe(Collections.singletonList(sensorsTopic));
            log.info("Starting aggregation service. Subscribed to topic: {}", sensorsTopic);

            while (true) {
                try {
                    ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));
                    
                    for (ConsumerRecord<String, SensorEventAvro> record : records) {
                        log.debug("Received sensor event: {}", record.value());
                        
                        Optional<SensorsSnapshotAvro> updatedSnapshot = updateState(record.value());
                        
                        if (updatedSnapshot.isPresent()) {
                            SensorsSnapshotAvro snapshot = updatedSnapshot.get();
                            log.debug("Sending updated snapshot for hub: {}", snapshot.getHubId());
                            
                            ProducerRecord<String, SensorsSnapshotAvro> producerRecord = 
                                new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);
                            producer.send(producerRecord, (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Error sending snapshot to topic {}", snapshotsTopic, exception);
                                } else {
                                    log.debug("Successfully sent snapshot to topic {} at offset {}", 
                                        snapshotsTopic, metadata.offset());
                                }
                            });
                        }
                    }
                    
                    consumer.commitSync();
                } catch (WakeupException ignored) {
                    break;
                } catch (Exception e) {
                    log.error("Ошибка во время обработки событий от датчиков", e);
                }
            }

            producer.flush();
            consumer.commitSync();
            log.info("Закрываем консьюмер и продюсер");

        } catch (Exception e) {
            log.error("Ошибка во время работы агрегатора", e);
        }
    }

    private Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        
        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            Map<String, SensorStateAvro> sensorsState = new HashMap<>();
            SensorStateAvro sensorState = createSensorState(event);
            sensorsState.put(sensorId, sensorState);
            
            snapshot = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(event.getTimestamp())
                .setSensorsState(sensorsState)
                .build();
            
            snapshots.put(hubId, snapshot);
            return Optional.of(snapshot);
        }
        
        Map<String, SensorStateAvro> sensorsState = new HashMap<>(snapshot.getSensorsState());
        SensorStateAvro oldState = sensorsState.get(sensorId);
        
        if (oldState != null) {
            if (oldState.getTimestamp() > event.getTimestamp() || 
                Objects.equals(oldState.getData(), event.getPayload())) {
                return Optional.empty();
            }
        }
        
        SensorStateAvro sensorState = createSensorState(event);
        sensorsState.put(sensorId, sensorState);
        
        long newTimestamp = Math.max(snapshot.getTimestamp(), event.getTimestamp());
        
        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder()
            .setHubId(hubId)
            .setTimestamp(newTimestamp)
            .setSensorsState(sensorsState)
            .build();
        
        snapshots.put(hubId, updatedSnapshot);
        return Optional.of(updatedSnapshot);
    }

    private SensorStateAvro createSensorState(SensorEventAvro event) {
        return SensorStateAvro.newBuilder()
            .setTimestamp(event.getTimestamp())
            .setData(event.getPayload())
            .build();
    }
}

