package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.interaction.api.api.WarehouseApi;

@FeignClient(
        name = "warehouse",
        contextId = "orderWarehouseClient",
        path = WarehouseApi.API_PATH
)
public interface WarehouseClient extends WarehouseApi {
}

