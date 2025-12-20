package ru.yandex.practicum.commerce.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.api.WarehouseApi;
import ru.yandex.practicum.commerce.interaction.api.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(WarehouseApi.API_PATH)
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void newProductInWarehouse(@Valid @RequestBody NewProductInWarehouseRequest request) {
        warehouseService.registerNewProduct(request);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@Valid @RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkProductQuantityEnoughForShoppingCart(shoppingCartDto);
    }

    @Override
    @PostMapping("/add")
    public void addProductToWarehouse(@Valid @RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProduct(request);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    @PostMapping("/assembly")
    public BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request) {
        return warehouseService.assemblyProductsForOrder(request);
    }

    @Override
    @PostMapping("/shipped")
    public void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request) {
        warehouseService.shippedToDelivery(request);
    }

    @Override
    @PostMapping("/return")
    public void acceptReturn(@RequestBody Map<UUID, Long> products) {
        warehouseService.acceptReturn(products);
    }
}

