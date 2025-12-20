package ru.yandex.practicum.commerce.delivery.service.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.delivery.model.AddressEmbeddable;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;

@Component
public class DeliveryMapper {

    public DeliveryDto toDto(DeliveryEntity entity) {
        if (entity == null) {
            return null;
        }
        return DeliveryDto.builder()
                .deliveryId(entity.getDeliveryId())
                .orderId(entity.getOrderId())
                .deliveryState(entity.getDeliveryState())
                .fromAddress(toDto(entity.getFromAddress()))
                .toAddress(toDto(entity.getToAddress()))
                .deliveryCost(entity.getDeliveryCost())
                .deliveryWeight(entity.getDeliveryWeight())
                .deliveryVolume(entity.getDeliveryVolume())
                .fragile(entity.getFragile())
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
}

