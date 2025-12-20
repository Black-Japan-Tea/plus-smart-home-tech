package ru.yandex.practicum.commerce.interaction.api.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;

@Validated
public interface PaymentApi {

    String API_PATH = "/api/v1/payment";

    @PostMapping
    PaymentDto payment(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/refund")
    PaymentDto paymentSuccess(@RequestBody @NotNull UUID paymentId);

    @PostMapping("/productCost")
    BigDecimal productCost(@Valid @RequestBody OrderDto orderDto);

    @PostMapping("/failed")
    PaymentDto paymentFailed(@RequestBody @NotNull UUID paymentId);
}

