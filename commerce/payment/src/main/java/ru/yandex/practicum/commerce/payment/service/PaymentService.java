package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.yandex.practicum.commerce.interaction.api.dto.OrderDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentStatus;
import ru.yandex.practicum.commerce.interaction.api.dto.ProductDto;
import ru.yandex.practicum.commerce.payment.client.OrderClient;
import ru.yandex.practicum.commerce.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;
import ru.yandex.practicum.commerce.payment.service.mapper.PaymentMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    @Transactional
    public PaymentDto createPayment(OrderDto orderDto) {
        validateOrder(orderDto);
        BigDecimal productCost = calculateProductCost(orderDto);
        BigDecimal deliveryCost = orZero(orderDto.getDeliveryPrice());
        BigDecimal fee = productCost.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = productCost.add(deliveryCost).add(fee).setScale(2, RoundingMode.HALF_UP);

        PaymentEntity entity = new PaymentEntity();
        entity.setOrderId(orderDto.getOrderId());
        entity.setProductTotal(productCost);
        entity.setDeliveryTotal(deliveryCost);
        entity.setFeeTotal(fee);
        entity.setTotalPayment(total);
        entity.setStatus(PaymentStatus.PENDING);

        return paymentMapper.toDto(paymentRepository.save(entity));
    }

    public BigDecimal calculateProductCost(OrderDto orderDto) {
        validateOrder(orderDto);
        Map<UUID, Long> products = orderDto.getProducts();
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Количество товара должно быть положительным");
            }
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            BigDecimal line = product.getPrice()
                    .multiply(BigDecimal.valueOf(entry.getValue()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(line);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalCost(OrderDto orderDto) {
        BigDecimal productCost = calculateProductCost(orderDto);
        BigDecimal deliveryCost = orZero(orderDto.getDeliveryPrice());
        BigDecimal fee = productCost.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        return productCost.add(deliveryCost).add(fee).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public PaymentDto paymentSuccess(UUID paymentId) {
        PaymentEntity payment = getPayment(paymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        PaymentEntity saved = paymentRepository.save(payment);
        orderClient.payment(saved.getOrderId());
        return paymentMapper.toDto(saved);
    }

    @Transactional
    public PaymentDto paymentFailed(UUID paymentId) {
        PaymentEntity payment = getPayment(paymentId);
        payment.setStatus(PaymentStatus.FAILED);
        PaymentEntity saved = paymentRepository.save(payment);
        orderClient.paymentFailed(saved.getOrderId());
        return paymentMapper.toDto(saved);
    }

    private void validateOrder(OrderDto orderDto) {
        if (orderDto == null || orderDto.getOrderId() == null) {
            throw new IllegalArgumentException("Недостаточно информации о заказе");
        }
        if (CollectionUtils.isEmpty(orderDto.getProducts())) {
            throw new IllegalArgumentException("Заказ не содержит товаров");
        }
    }

    private PaymentEntity getPayment(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Не указан идентификатор оплаты");
        }
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

