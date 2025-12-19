package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.commerce.delivery.client.OrderClient;
import ru.yandex.practicum.commerce.delivery.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.model.DeliveryEntity;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.delivery.service.mapper.DeliveryMapper;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryState;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ShippedToDeliveryRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeliveryService {

    private static final BigDecimal BASE_COST = new BigDecimal("5.0");

    private final DeliveryRepository deliveryRepository;
    private final DeliveryMapper deliveryMapper;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        if (deliveryDto == null || deliveryDto.getOrderId() == null) {
            throw new IllegalArgumentException("Необходимо указать идентификатор заказа для доставки");
        }
        DeliveryEntity entity = deliveryRepository.findByOrderId(deliveryDto.getOrderId())
                .orElseGet(DeliveryEntity::new);
        entity.setOrderId(deliveryDto.getOrderId());
        entity.setFromAddress(deliveryMapper.toEmbeddable(deliveryDto.getFromAddress()));
        entity.setToAddress(deliveryMapper.toEmbeddable(deliveryDto.getToAddress()));
        entity.setDeliveryCost(deliveryDto.getDeliveryCost());
        entity.setDeliveryWeight(deliveryDto.getDeliveryWeight());
        entity.setDeliveryVolume(deliveryDto.getDeliveryVolume());
        entity.setFragile(deliveryDto.getFragile());
        entity.setDeliveryState(deliveryDto.getDeliveryState() == null
                ? DeliveryState.CREATED
                : deliveryDto.getDeliveryState());
        DeliveryEntity saved = deliveryRepository.save(entity);
        return deliveryMapper.toDto(saved);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        DeliveryEntity entity = getByOrderId(orderId);
        entity.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(entity);
        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        DeliveryEntity entity = getByOrderId(orderId);
        entity.setDeliveryState(DeliveryState.IN_PROGRESS);
        DeliveryEntity saved = deliveryRepository.save(entity);
        if (saved.getDeliveryId() != null) {
            warehouseClient.shippedToDelivery(
                    ShippedToDeliveryRequest.builder()
                            .orderId(orderId)
                            .deliveryId(saved.getDeliveryId())
                            .build()
            );
        }
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        DeliveryEntity entity = getByOrderId(orderId);
        entity.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(entity);
        orderClient.deliveryFailed(orderId);
    }

    public BigDecimal deliveryCost(OrderDto orderDto) {
        if (orderDto == null || orderDto.getOrderId() == null) {
            throw new IllegalArgumentException("Не указан заказ для расчёта доставки");
        }
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        AddressDto toAddress = orderDto.getDeliveryAddress();
        BigDecimal cost = BASE_COST;

        BigDecimal warehouseMultiplier = resolveWarehouseMultiplier(warehouseAddress);
        cost = cost.add(BASE_COST.multiply(warehouseMultiplier));

        if (Boolean.TRUE.equals(orderDto.getFragile())) {
            cost = cost.add(cost.multiply(new BigDecimal("0.2")));
        }

        BigDecimal weight = orZero(orderDto.getDeliveryWeight());
        cost = cost.add(weight.multiply(new BigDecimal("0.3")));

        BigDecimal volume = orZero(orderDto.getDeliveryVolume());
        cost = cost.add(volume.multiply(new BigDecimal("0.2")));

        if (!isSameStreet(warehouseAddress, toAddress)) {
            cost = cost.add(cost.multiply(new BigDecimal("0.2")));
        }

        return cost.setScale(2, RoundingMode.HALF_UP);
    }

    private DeliveryEntity getByOrderId(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Не указан идентификатор заказа");
        }
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new DeliveryNotFoundException(orderId));
    }

    private BigDecimal resolveWarehouseMultiplier(AddressDto warehouseAddress) {
        if (warehouseAddress == null || !StringUtils.hasText(warehouseAddress.getStreet())) {
            return BigDecimal.ZERO;
        }
        String street = warehouseAddress.getStreet().toUpperCase();
        if (street.contains("ADDRESS_2")) {
            return BigDecimal.valueOf(2);
        }
        if (street.contains("ADDRESS_1")) {
            return BigDecimal.ONE;
        }
        return BigDecimal.ZERO;
    }

    private boolean isSameStreet(AddressDto from, AddressDto to) {
        if (from == null || to == null) {
            return false;
        }
        return StringUtils.hasText(from.getStreet())
                && from.getStreet().equalsIgnoreCase(to.getStreet());
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

