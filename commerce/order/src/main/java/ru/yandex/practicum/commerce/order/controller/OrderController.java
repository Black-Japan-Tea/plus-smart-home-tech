package ru.yandex.practicum.commerce.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.api.OrderApi;
import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(OrderApi.API_PATH)
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return orderService.getOrders(username);
    }

    @Override
    public OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request) {
        return orderService.createOrder(request);
    }

    @Override
    public OrderDto productReturn(@Valid @RequestBody ProductReturnRequest productReturnRequest) {
        return orderService.productReturn(productReturnRequest);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto complete(UUID orderId) {
        return orderService.complete(orderId);
    }

    @Override
    public OrderDto calculateTotalCost(UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    public OrderDto calculateDeliveryCost(UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        return orderService.assemblyFailed(orderId);
    }
}

