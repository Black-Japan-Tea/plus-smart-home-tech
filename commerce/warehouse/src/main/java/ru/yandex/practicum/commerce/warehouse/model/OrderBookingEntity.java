package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "order_bookings")
public class OrderBookingEntity {

    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_booking_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products = new LinkedHashMap<>();

    @Column(name = "delivery_weight")
    private BigDecimal deliveryWeight;

    @Column(name = "delivery_volume")
    private BigDecimal deliveryVolume;

    @Column(name = "fragile")
    private Boolean fragile;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}

