package ru.yandex.practicum.kafka.telemetry.hubrouter.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.grpc.telemetry.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.hubrouter.model.ActionLogEntry;

@Slf4j
@Service
public class ActionLogService {

    private final CopyOnWriteArrayList<ActionLogEntry> entries = new CopyOnWriteArrayList<>();

    public void record(DeviceActionRequest request) {
        ActionLogEntry entry = ActionLogEntry.from(request);
        entries.add(entry);
        log.info(
            "Action recorded: hubId={}, scenario={}, sensorId={}, type={}, value={}, timestamp={}",
            entry.hubId(), entry.scenarioName(), entry.sensorId(),
            entry.actionType(), entry.value(), entry.timestamp()
        );
    }

    public List<ActionLogEntry> getEntries() {
        return List.copyOf(entries);
    }

    public void clear() {
        entries.clear();
        log.info("Action log cleared");
    }
}

