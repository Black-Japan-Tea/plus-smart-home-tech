package ru.yandex.practicum.commerce.delivery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;
import ru.yandex.practicum.commerce.interaction.api.api.DeliveryApi;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(DeliveryApi.API_PATH)
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {

    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto planDelivery(@Valid @RequestBody DeliveryDto deliveryDto) {
        return deliveryService.planDelivery(deliveryDto);
    }

    @Override
    public void deliverySuccessful(@RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    public void deliveryPicked(@RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    public void deliveryFailed(@RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }

    @Override
    public BigDecimal deliveryCost(@Valid @RequestBody OrderDto orderDto) {
        return deliveryService.deliveryCost(orderDto);
    }
}

