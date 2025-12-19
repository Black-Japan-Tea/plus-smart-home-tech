package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.DeliveryApi;

@FeignClient(
        name = "delivery",
        contextId = "orderDeliveryClient",
        path = DeliveryApi.API_PATH
)
public interface DeliveryClient extends DeliveryApi {
}

