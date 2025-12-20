package ru.yandex.practicum.commerce.delivery.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.WarehouseApi;

@FeignClient(
        name = "warehouse",
        contextId = "deliveryWarehouseClient",
        path = WarehouseApi.API_PATH
)
public interface WarehouseClient extends WarehouseApi {
}

