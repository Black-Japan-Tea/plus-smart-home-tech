package ru.yandex.practicum.kafka.telemetry.hubrouter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class HubRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HubRouterApplication.class, args);
        log.info("Hub Router mock service started");
    }
}

