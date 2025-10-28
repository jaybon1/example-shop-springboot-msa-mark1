package com.example.shop.payment.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class Payment {

    private final UUID id;
    private final UUID orderId;
    private final UUID userId;
    private final Status status;
    private final Method method;
    private final Long amount;
    private final String transactionKey;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    public Payment markCompleted() {
        return toBuilder()
                .status(Status.COMPLETED)
                .build();
    }

    public Payment markCancelled() {
        return toBuilder()
                .status(Status.CANCELLED)
                .build();
    }

    public boolean isOwnedBy(UUID targetUserId) {
        return targetUserId != null && targetUserId.equals(this.userId);
    }

    PaymentBuilder toBuilder() {
        return Payment.builder()
                .id(id)
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .method(method)
                .amount(amount)
                .transactionKey(transactionKey)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }

    public enum Status {
        COMPLETED,
        CANCELLED
    }

    public enum Method {
        CARD,
        BANK_TRANSFER,
        MOBILE,
        POINT
    }
}
