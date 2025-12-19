package ru.yandex.practicum.commerce.interaction.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    @NotNull
    private UUID orderId;

    private UUID shoppingCartId;
    private String username;

    @Builder.Default
    @NotNull
    private Map<UUID, Long> products = new LinkedHashMap<>();

    private UUID paymentId;
    private UUID deliveryId;
    private OrderState state;
    private BigDecimal deliveryWeight;
    private BigDecimal deliveryVolume;
    private Boolean fragile;
    private BigDecimal totalPrice;
    private BigDecimal deliveryPrice;
    private BigDecimal productPrice;

    @Valid
    private AddressDto deliveryAddress;

    private OffsetDateTime createdAt;
}

