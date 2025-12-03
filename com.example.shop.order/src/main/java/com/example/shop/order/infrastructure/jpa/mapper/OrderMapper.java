package com.example.shop.order.infrastructure.jpa.mapper;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.vo.OrderPayment;
import com.example.shop.order.infrastructure.jpa.entity.OrderEntity;
import com.example.shop.order.infrastructure.jpa.entity.OrderItemEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public Order toDomain(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .status(toDomain(entity.getStatus()))
                .totalAmount(entity.getTotalAmount())
                .orderItemList(
                        entity.getOrderItemList()
                                .stream()
                                .map(this::toDomain)
                                .toList()
                )
                .payment(toDomainPayment(entity))
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .build();
    }

    private OrderItem toDomain(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrderItem.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .lineTotal(entity.getLineTotal())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .deletedAt(entity.getDeletedAt())
                .deletedBy(entity.getDeletedBy())
                .build();
    }

    private OrderPayment toDomainPayment(OrderEntity entity) {
        if (entity.getPaymentId() == null) {
            return null;
        }
        return OrderPayment.builder()
                .id(entity.getPaymentId())
                .status(toDomain(entity.getPaymentStatus()))
                .method(toDomain(entity.getPaymentMethod()))
                .amount(entity.getPaymentAmount())
                .build();
    }

    public OrderEntity toEntity(Order order) {
        if (order == null) {
            return null;
        }
        OrderEntity entity = OrderEntity.builder()
                .id(order.getId())
                .status(toEntity(order.getStatus()))
                .totalAmount(order.getTotalAmount())
                .userId(order.getUserId())
                .paymentId(order.getPayment() != null ? order.getPayment().getId() : null)
                .paymentStatus(toEntity(order.getPayment() != null ? order.getPayment().getStatus() : null))
                .paymentMethod(toEntity(order.getPayment() != null ? order.getPayment().getMethod() : null))
                .paymentAmount(order.getPayment() != null ? order.getPayment().getAmount() : null)
                .build();

        order.getOrderItemList()
                .stream()
                .map(this::toEntity)
                .forEach(entity::addOrderItem);

        return entity;
    }

    public void applyDomain(Order order, OrderEntity entity) {
        if (order == null || entity == null) {
            return;
        }
        entity.updateStatus(toEntity(order.getStatus()));
        entity.updateTotalAmount(order.getTotalAmount());
        entity.assignUser(order.getUserId());
        if (order.getPayment() != null) {
            entity.assignPayment(
                    order.getPayment().getId(),
                    toEntity(order.getPayment().getStatus()),
                    toEntity(order.getPayment().getMethod()),
                    order.getPayment().getAmount()
            );
        } else {
            entity.assignPayment(null, null, null, null);
        }
        syncOrderItems(order, entity);
    }

    private void syncOrderItems(Order order, OrderEntity entity) {
        Map<UUID, OrderItemEntity> existingItems = entity.getOrderItemList()
                .stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(OrderItemEntity::getId, Function.identity()));

        Set<UUID> desiredIds = new HashSet<>();
        for (OrderItem orderItem : order.getOrderItemList()) {
            UUID orderItemId = orderItem.getId();
            if (orderItemId != null && existingItems.containsKey(orderItemId)) {
                OrderItemEntity existing = existingItems.get(orderItemId);
                existing.update(
                        orderItem.getProductId(),
                        orderItem.getProductName(),
                        orderItem.getUnitPrice(),
                        orderItem.getQuantity(),
                        orderItem.getLineTotal()
                );
                desiredIds.add(orderItemId);
            } else {
                OrderItemEntity created = toEntity(orderItem);
                entity.addOrderItem(created);
                if (created.getId() != null) {
                    desiredIds.add(created.getId());
                }
            }
        }

        List<OrderItemEntity> toRemove = entity.getOrderItemList()
                .stream()
                .filter(item -> item.getId() != null && !desiredIds.contains(item.getId()))
                .toList();
        toRemove.forEach(item -> {
            item.detachOrder();
            entity.getOrderItemList().remove(item);
        });
    }

    private OrderItemEntity toEntity(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        return OrderItemEntity.builder()
                .id(orderItem.getId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .unitPrice(orderItem.getUnitPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .build();
    }

    private Order.Status toDomain(OrderEntity.Status status) {
        return status == null ? null : Order.Status.valueOf(status.name());
    }

    private OrderEntity.Status toEntity(Order.Status status) {
        return status == null ? null : OrderEntity.Status.valueOf(status.name());
    }

    private OrderPayment.Status toDomain(OrderEntity.PaymentStatus status) {
        return status == null ? null : OrderPayment.Status.valueOf(status.name());
    }

    private OrderEntity.PaymentStatus toEntity(OrderPayment.Status status) {
        return status == null ? null : OrderEntity.PaymentStatus.valueOf(status.name());
    }

    private OrderPayment.Method toDomain(OrderEntity.PaymentMethod method) {
        return method == null ? null : OrderPayment.Method.valueOf(method.name());
    }

    private OrderEntity.PaymentMethod toEntity(OrderPayment.Method method) {
        return method == null ? null : OrderEntity.PaymentMethod.valueOf(method.name());
    }
}
