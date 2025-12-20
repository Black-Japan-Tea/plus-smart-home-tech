package ru.yandex.practicum.commerce.delivery.exception;

import java.util.UUID;

public class DeliveryNotFoundException extends RuntimeException {

    public DeliveryNotFoundException(UUID orderId) {
        super("Не найдена доставка для заказа %s".formatted(orderId));
    }
}

