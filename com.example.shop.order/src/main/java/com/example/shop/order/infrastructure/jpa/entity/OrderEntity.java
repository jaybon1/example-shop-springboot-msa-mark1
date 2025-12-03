package com.example.shop.order.infrastructure.jpa.entity;

import com.example.shop.global.infrastructure.persistence.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "`ORDER`")
@DynamicInsert
@DynamicUpdate
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder.Default
    @Column(name = "total_amount", nullable = false)
    private Long totalAmount = 0L;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItemList = new ArrayList<>();

    @Column(name = "payment_id")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_amount")
    private Long paymentAmount;

    public void updateStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void updateTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void assignUser(UUID userId) {
        this.userId = userId;
    }

    public void assignPayment(UUID paymentId, PaymentStatus paymentStatus, PaymentMethod paymentMethod, Long paymentAmount) {
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
    }

    public void addOrderItem(OrderItemEntity orderItemEntity) {
        orderItemEntity.setOrder(this);
        this.orderItemList.add(orderItemEntity);
    }

    public enum Status {
        CREATED,
        PAID,
        CANCELLED
    }

    public enum PaymentStatus {
        REQUESTED,
        COMPLETED,
        CANCELLED
    }

    public enum PaymentMethod {
        CARD,
        BANK_TRANSFER,
        MOBILE,
        POINT
    }
}
