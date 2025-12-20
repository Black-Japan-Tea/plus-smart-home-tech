package ru.yandex.practicum.commerce.payment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.api.api.PaymentApi;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(PaymentApi.API_PATH)
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public PaymentDto payment(@Valid OrderDto orderDto) {
        return paymentService.createPayment(orderDto);
    }

    @Override
    public BigDecimal getTotalCost(@Valid OrderDto orderDto) {
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public PaymentDto paymentSuccess(UUID paymentId) {
        return paymentService.paymentSuccess(paymentId);
    }

    @Override
    public BigDecimal productCost(@Valid OrderDto orderDto) {
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public PaymentDto paymentFailed(UUID paymentId) {
        return paymentService.paymentFailed(paymentId);
    }
}

