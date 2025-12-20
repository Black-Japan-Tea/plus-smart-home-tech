package ru.yandex.practicum.commerce.interaction.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNewOrderRequest {

    @Valid
    @NotNull
    private ShoppingCartDto shoppingCart;

    @Valid
    @NotNull
    private AddressDto deliveryAddress;

    /**
     * Имя пользователя, оформившего заказ.
     * Поле опционально и может дублировать username из ShoppingCartDto.
     */
    private String username;
}

