package com.example.shop.payment.infrastructure.persistence.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "PAYMENT")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false)
    private Method method;

    @Builder.Default
    @Column(name = "amount", nullable = false)
    private Long amount = 0L;

    @Column(name = "transaction_key")
    private String transactionKey;

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateDetails(Method method, Long amount, String transactionKey) {
        if (method != null) {
            this.method = method;
        }
        if (amount != null) {
            this.amount = amount;
        }
        if (transactionKey != null) {
            this.transactionKey = transactionKey;
        }
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
