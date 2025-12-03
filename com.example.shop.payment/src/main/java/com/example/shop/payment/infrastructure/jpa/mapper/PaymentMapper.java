package com.example.shop.payment.infrastructure.jpa.mapper;

import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.infrastructure.jpa.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentEntity entity) {
        if (entity == null) {
            return null;
        }
        return Payment.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .userId(entity.getUserId())
                .status(toDomain(entity.getStatus()))
                .method(toDomain(entity.getMethod()))
                .amount(entity.getAmount())
                .transactionKey(entity.getTransactionKey())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .build();
    }

    public PaymentEntity toEntity(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentEntity.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .status(toEntity(payment.getStatus()))
                .method(toEntity(payment.getMethod()))
                .amount(payment.getAmount())
                .transactionKey(payment.getTransactionKey())
                .build();
    }

    public void applyDomain(Payment payment, PaymentEntity entity) {
        if (payment == null || entity == null) {
            return;
        }
        entity.updateDetails(
                toEntity(payment.getMethod()),
                payment.getAmount(),
                payment.getTransactionKey()
        );
        if (payment.getStatus() != null) {
            entity.updateStatus(toEntity(payment.getStatus()));
        }
    }

    private Payment.Status toDomain(PaymentEntity.Status status) {
        return status == null ? null : Payment.Status.valueOf(status.name());
    }

    private PaymentEntity.Status toEntity(Payment.Status status) {
        return status == null ? null : PaymentEntity.Status.valueOf(status.name());
    }

    private Payment.Method toDomain(PaymentEntity.Method method) {
        return method == null ? null : Payment.Method.valueOf(method.name());
    }

    private PaymentEntity.Method toEntity(Payment.Method method) {
        return method == null ? null : PaymentEntity.Method.valueOf(method.name());
    }
}
