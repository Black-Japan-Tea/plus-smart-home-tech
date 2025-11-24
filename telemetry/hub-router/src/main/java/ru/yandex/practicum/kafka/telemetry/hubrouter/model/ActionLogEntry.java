package ru.yandex.practicum.kafka.telemetry.hubrouter.model;

import java.time.Instant;

import ru.yandex.practicum.grpc.telemetry.DeviceActionRequest;

public record ActionLogEntry(
    String hubId,
    String scenarioName,
    String sensorId,
    String actionType,
    Integer value,
    Instant timestamp
) {

    public static ActionLogEntry from(DeviceActionRequest request) {
        Integer value = request.getAction().getValue();
        Instant timestamp = Instant.ofEpochSecond(
            request.getTimestamp().getSeconds(),
            request.getTimestamp().getNanos()
        );
        return new ActionLogEntry(
            request.getHubId(),
            request.getScenarioName(),
            request.getAction().getSensorId(),
            request.getAction().getType().name(),
            value,
            timestamp
        );
    }
}

