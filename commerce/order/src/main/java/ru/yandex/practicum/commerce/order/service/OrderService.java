package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.commerce.interaction.api.dto.AddressDto;
import ru.yandex.practicum.commerce.interaction.api.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.BookedProductsDto;
import ru.yandex.practicum.commerce.interaction.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.interaction.api.dto.DeliveryState;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderState;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.interaction.api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.order.client.DeliveryClient;
import ru.yandex.practicum.commerce.order.client.PaymentClient;
import ru.yandex.practicum.commerce.order.client.WarehouseClient;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.model.OrderEntity;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;
import ru.yandex.practicum.commerce.order.service.mapper.OrderMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final WarehouseClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public List<OrderDto> getOrders(String username) {
        if (!StringUtils.hasText(username)) {
            throw new NotAuthorizedUserException();
        }
        return orderRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto createOrder(CreateNewOrderRequest request) {
        if (request == null || request.getShoppingCart() == null) {
            throw new IllegalArgumentException("Не указана корзина для создания заказа");
        }
        String username = resolveUsername(request);
        if (!StringUtils.hasText(username)) {
            throw new NotAuthorizedUserException();
        }
        Map<UUID, Long> products = orderMapper.copyProducts(request.getShoppingCart().getProducts());
        if (CollectionUtils.isEmpty(products)) {
            throw new IllegalArgumentException("Нельзя создать заказ без товаров");
        }

        OrderEntity entity = new OrderEntity();
        entity.setUsername(username);
        entity.setShoppingCartId(request.getShoppingCart().getShoppingCartId());
        entity.setProducts(products);
        entity.setDeliveryAddress(orderMapper.toEmbeddable(request.getDeliveryAddress()));
        entity.setState(OrderState.NEW);

        OrderEntity saved = orderRepository.save(entity);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        if (request == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("Не указан заказ для возврата");
        }
        OrderEntity order = getOrder(request.getOrderId());
        if (!CollectionUtils.isEmpty(request.getProducts())) {
            warehouseClient.acceptReturn(request.getProducts());
        }
        order.setState(OrderState.PRODUCT_RETURNED);
        OrderEntity saved = orderRepository.save(order);
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        if (order.getPaymentId() == null) {
            OrderDto dto = orderMapper.toDto(order);
            PaymentDto payment = paymentClient.payment(dto);
            order.setPaymentId(payment.getPaymentId());
            order.setProductPrice(resolveProductPrice(dto, payment));
            order.setDeliveryPrice(payment.getDeliveryTotal());
            order.setTotalPrice(payment.getTotalPayment());
            order.setState(OrderState.ON_PAYMENT);
        } else {
            order.setState(OrderState.PAID);
        }
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        if (order.getDeliveryId() == null) {
            ensureDeliveryPlanned(order);
            order.setState(OrderState.ON_DELIVERY);
        } else {
            order.setState(OrderState.DELIVERED);
        }
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        order.setState(OrderState.COMPLETED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        BigDecimal total = paymentClient.getTotalCost(orderMapper.toDto(order));
        order.setTotalPrice(total);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderMapper.toDto(order));
        order.setDeliveryPrice(deliveryCost);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        if (CollectionUtils.isEmpty(order.getProducts())) {
            throw new IllegalArgumentException("Нельзя собрать заказ без товаров");
        }
        AssemblyProductsForOrderRequest request = AssemblyProductsForOrderRequest.builder()
                .orderId(order.getOrderId())
                .products(orderMapper.copyProducts(order.getProducts()))
                .build();
        BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(request);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());
        order.setState(OrderState.ASSEMBLED);
        ensureDeliveryPlanned(order);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        OrderEntity order = getOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    private OrderEntity getOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Не указан идентификатор заказа");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private String resolveUsername(CreateNewOrderRequest request) {
        if (request == null) {
            return null;
        }
        if (StringUtils.hasText(request.getUsername())) {
            return request.getUsername();
        }
        if (request.getShoppingCart() != null) {
            return request.getShoppingCart().getUsername();
        }
        return null;
    }

    private BigDecimal resolveProductPrice(OrderDto orderDto, PaymentDto paymentDto) {
        if (paymentDto.getProductTotal() != null) {
            return paymentDto.getProductTotal();
        }
        BigDecimal total = orZero(paymentDto.getTotalPayment());
        BigDecimal delivery = orZero(paymentDto.getDeliveryTotal());
        BigDecimal fee = orZero(paymentDto.getFeeTotal());
        BigDecimal result = total.subtract(delivery).subtract(fee);
        if (result.signum() < 0) {
            result = BigDecimal.ZERO;
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private void ensureDeliveryPlanned(OrderEntity order) {
        if (order.getDeliveryId() != null) {
            return;
        }
        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();
        AddressDto deliveryAddress = orderMapper.toDto(order.getDeliveryAddress());
        if (deliveryAddress == null || warehouseAddress == null) {
            return;
        }
        DeliveryDto request = DeliveryDto.builder()
                .orderId(order.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(deliveryAddress)
                .deliveryState(DeliveryState.CREATED)
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.getFragile())
                .deliveryCost(order.getDeliveryPrice())
                .build();
        DeliveryDto response = deliveryClient.planDelivery(request);
        order.setDeliveryId(response.getDeliveryId());
        if (response.getDeliveryCost() != null) {
            order.setDeliveryPrice(response.getDeliveryCost());
        }
        warehouseClient.shippedToDelivery(
                ShippedToDeliveryRequest.builder()
                        .orderId(order.getOrderId())
                        .deliveryId(order.getDeliveryId())
                        .build()
        );
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

