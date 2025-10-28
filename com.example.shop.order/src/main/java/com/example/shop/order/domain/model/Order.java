package com.example.shop.order.domain.model;

import com.example.shop.order.domain.vo.OrderPayment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Order {

    private final UUID id;
    private final UUID userId;
    private final Status status;
    private final Long totalAmount;
    private final List<OrderItem> orderItemList;
    private final OrderPayment payment;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    public List<OrderItem> getOrderItemList() {
        return orderItemList == null ? List.of() : Collections.unmodifiableList(orderItemList);
    }

    public Order addOrderItem(OrderItem orderItem) {
        if (orderItem == null) {
            return this;
        }
        List<OrderItem> updated = new ArrayList<>(getOrderItemList());
        updated.add(orderItem);
        return toBuilder()
                .orderItemList(List.copyOf(updated))
                .build();
    }

    public Order updateTotalAmount(Long totalAmount) {
        return toBuilder()
                .totalAmount(totalAmount)
                .build();
    }

    public Order markCancelled() {
        return toBuilder()
                .status(Status.CANCELLED)
                .build();
    }

    public Order assignPayment(OrderPayment orderPayment) {
        return toBuilder()
                .payment(orderPayment)
                .build();
    }

    OrderBuilder toBuilder() {
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(status)
                .totalAmount(totalAmount)
                .orderItemList(orderItemList)
                .payment(payment)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }

    public boolean isOwnedBy(UUID targetUserId) {
        return targetUserId != null && targetUserId.equals(this.userId);
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELLED
    }
}
