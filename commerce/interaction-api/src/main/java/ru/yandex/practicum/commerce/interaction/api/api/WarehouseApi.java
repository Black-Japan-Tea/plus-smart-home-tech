package ru.yandex.practicum.commerce.interaction.api.api;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.UUID;

import ru.yandex.practicum.commerce.interaction.api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;

@Validated
public interface WarehouseApi {

    String API_PATH = "/api/v1/warehouse";
    
    @PutMapping
    void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto);

    @PostMapping("/add")
    void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PostMapping("/assembly")
    BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/shipped")
    void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/return")
    void acceptReturn(@RequestBody Map<UUID, Long> products);
}

