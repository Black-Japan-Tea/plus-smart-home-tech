package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.PaymentApi;

@FeignClient(
        name = "payment",
        contextId = "orderPaymentClient",
        path = PaymentApi.API_PATH
)
public interface PaymentClient extends PaymentApi {
}

