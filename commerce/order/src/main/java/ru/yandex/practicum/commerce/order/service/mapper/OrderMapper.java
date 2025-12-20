package ru.yandex.practicum.commerce.order.service.mapper;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.order.model.AddressEmbeddable;
import ru.yandex.practicum.commerce.order.model.OrderEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrderMapper {

    public OrderDto toDto(OrderEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderDto.builder()
                .orderId(entity.getOrderId())
                .shoppingCartId(entity.getShoppingCartId())
                .username(entity.getUsername())
                .products(copyProducts(entity.getProducts()))
                .paymentId(entity.getPaymentId())
                .deliveryId(entity.getDeliveryId())
                .state(entity.getState())
                .deliveryWeight(entity.getDeliveryWeight())
                .deliveryVolume(entity.getDeliveryVolume())
                .fragile(entity.getFragile())
                .totalPrice(entity.getTotalPrice())
                .deliveryPrice(entity.getDeliveryPrice())
                .productPrice(entity.getProductPrice())
                .deliveryAddress(toDto(entity.getDeliveryAddress()))
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AddressEmbeddable toEmbeddable(AddressDto dto) {
        if (dto == null) {
            return null;
        }
        AddressEmbeddable embeddable = new AddressEmbeddable();
        embeddable.setCountry(dto.getCountry());
        embeddable.setCity(dto.getCity());
        embeddable.setStreet(dto.getStreet());
        embeddable.setHouse(dto.getHouse());
        embeddable.setFlat(dto.getFlat());
        return embeddable;
    }

    public AddressDto toDto(AddressEmbeddable embeddable) {
        if (embeddable == null) {
            return null;
        }
        return AddressDto.builder()
                .country(embeddable.getCountry())
                .city(embeddable.getCity())
                .street(embeddable.getStreet())
                .house(embeddable.getHouse())
                .flat(embeddable.getFlat())
                .build();
    }

    public Map<UUID, Long> copyProducts(Map<UUID, Long> products) {
        if (CollectionUtils.isEmpty(products)) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(products);
    }
}

