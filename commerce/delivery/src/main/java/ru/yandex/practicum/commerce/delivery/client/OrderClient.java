package ru.yandex.practicum.commerce.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.OrderApi;

@FeignClient(
        name = "order",
        contextId = "deliveryOrderClient",
        path = OrderApi.API_PATH
)
public interface OrderClient extends OrderApi {
}

