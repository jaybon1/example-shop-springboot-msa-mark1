package com.example.shop.order.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class OrderItem {

    private final UUID id;
    private final UUID orderId;
    private final UUID productId;
    private final String productName;
    private final Long unitPrice;
    private final Long quantity;
    private final Long lineTotal;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    OrderItemBuilder toBuilder() {
        return OrderItem.builder()
                .id(id)
                .orderId(orderId)
                .productId(productId)
                .productName(productName)
                .unitPrice(unitPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }
}
