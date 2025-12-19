package ru.yandex.practicum.commerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.OrderApi;

@FeignClient(
        name = "order",
        contextId = "paymentOrderClient",
        path = OrderApi.API_PATH
)
public interface OrderClient extends OrderApi {
}

