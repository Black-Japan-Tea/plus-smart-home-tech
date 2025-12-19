package ru.yandex.practicum.commerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.ShoppingStoreApi;

@FeignClient(
        name = "shopping-store",
        contextId = "paymentShoppingStoreClient",
        path = ShoppingStoreApi.API_PATH
)
public interface ShoppingStoreClient extends ShoppingStoreApi {
}

