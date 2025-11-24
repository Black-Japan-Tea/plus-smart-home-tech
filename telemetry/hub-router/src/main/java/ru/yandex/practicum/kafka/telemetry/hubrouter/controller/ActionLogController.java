package ru.yandex.practicum.kafka.telemetry.hubrouter.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.kafka.telemetry.hubrouter.model.ActionLogEntry;
import ru.yandex.practicum.kafka.telemetry.hubrouter.service.ActionLogService;

@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionLogController {

    private final ActionLogService actionLogService;

    @GetMapping
    public List<ActionLogEntry> getActions() {
        return actionLogService.getEntries();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearActions() {
        actionLogService.clear();
        return ResponseEntity.noContent().build();
    }
}

