package ru.yandex.practicum.commerce.interaction.api.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductReturnRequest;

@Validated
public interface OrderApi {

    String API_PATH = "/api/v1/order";

    @GetMapping
    List<OrderDto> getClientOrders(@RequestParam("username") @NotBlank String username);

    @PutMapping
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/return")
    OrderDto productReturn(@Valid @RequestBody ProductReturnRequest productReturnRequest);

    @PostMapping("/payment")
    OrderDto payment(@RequestBody @NotNull UUID orderId);

    @PostMapping("/payment/failed")
    OrderDto paymentFailed(@RequestBody @NotNull UUID orderId);

    @PostMapping("/delivery")
    OrderDto delivery(@RequestBody @NotNull UUID orderId);

    @PostMapping("/delivery/failed")
    OrderDto deliveryFailed(@RequestBody @NotNull UUID orderId);

    @PostMapping("/completed")
    OrderDto complete(@RequestBody @NotNull UUID orderId);

    @PostMapping("/calculate/total")
    OrderDto calculateTotalCost(@RequestBody @NotNull UUID orderId);

    @PostMapping("/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody @NotNull UUID orderId);

    @PostMapping("/assembly")
    OrderDto assembly(@RequestBody @NotNull UUID orderId);

    @PostMapping("/assembly/failed")
    OrderDto assemblyFailed(@RequestBody @NotNull UUID orderId);
}

