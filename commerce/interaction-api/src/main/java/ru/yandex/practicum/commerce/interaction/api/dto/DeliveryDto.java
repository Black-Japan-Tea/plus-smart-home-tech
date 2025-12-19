package ru.yandex.practicum.commerce.interaction.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDto {

    private UUID deliveryId;

    @Valid
    @NotNull
    private AddressDto fromAddress;

    @Valid
    @NotNull
    private AddressDto toAddress;

    @NotNull
    private UUID orderId;

    private DeliveryState deliveryState;
    private BigDecimal deliveryCost;
    private BigDecimal deliveryWeight;
    private BigDecimal deliveryVolume;
    private Boolean fragile;
}

