package ru.yandex.practicum.commerce.payment.service.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.commerce.interaction.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.model.PaymentEntity;

@Component
public class PaymentMapper {

    public PaymentDto toDto(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        return PaymentDto.builder()
                .paymentId(entity.getPaymentId())
                .orderId(entity.getOrderId())
                .totalPayment(entity.getTotalPayment())
                .deliveryTotal(entity.getDeliveryTotal())
                .feeTotal(entity.getFeeTotal())
                .productTotal(entity.getProductTotal())
                .status(entity.getStatus())
                .build();
    }
}

