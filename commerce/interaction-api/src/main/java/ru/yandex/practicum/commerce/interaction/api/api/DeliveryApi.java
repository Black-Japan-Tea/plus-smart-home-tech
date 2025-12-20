package ru.yandex.practicum.commerce.interaction.api.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;

@Validated
public interface DeliveryApi {

    String API_PATH = "/api/v1/delivery";

    @PutMapping
    DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto deliveryDto);

    @PostMapping("/successful")
    void deliverySuccessful(@RequestBody @NotNull UUID orderId);

    @PostMapping("/picked")
    void deliveryPicked(@RequestBody @NotNull UUID orderId);

    @PostMapping("/failed")
    void deliveryFailed(@RequestBody @NotNull UUID orderId);

    @PostMapping("/cost")
    BigDecimal deliveryCost(@Valid @RequestBody OrderDto orderDto);
}

