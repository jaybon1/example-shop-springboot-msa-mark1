package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.Order.Status;
import com.example.shop.order.domain.model.OrderItem;
import com.example.shop.order.domain.vo.OrderPayment;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResGetOrderDtoV1 {

    private final OrderDto order;

    public static ResGetOrderDtoV1 of(Order order) {
        return ResGetOrderDtoV1.builder()
                .order(OrderDto.from(order))
                .build();
    }

    @Getter
    @Builder
    public static class OrderDto {

        private final String id;
        private final Status status;
        private final Long totalAmount;
        private final Instant createdAt;
        private final Instant updatedAt;
        private final List<OrderItemDto> orderItemList;
        private final PaymentDto payment;

        public static OrderDto from(Order order) {
            return OrderDto.builder()
                    .id(order.getId() != null ? order.getId().toString() : null)
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .orderItemList(OrderItemDto.from(order.getOrderItemList()))
                    .payment(PaymentDto.from(order.getPayment()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class OrderItemDto {

        private final String id;
        private final String productId;
        private final String productName;
        private final Long unitPrice;
        private final Long quantity;
        private final Long lineTotal;

        private static List<OrderItemDto> from(List<OrderItem> orderItemList) {
            return orderItemList.stream()
                    .map(OrderItemDto::from)
                    .toList();
        }

        public static OrderItemDto from(OrderItem orderItem) {
            if (orderItem == null) {
                return null;
            }
            return OrderItemDto.builder()
                    .id(orderItem.getId() != null ? orderItem.getId().toString() : null)
                    .productId(orderItem.getProductId() != null ? orderItem.getProductId().toString() : null)
                    .productName(orderItem.getProductName())
                    .unitPrice(orderItem.getUnitPrice())
                    .quantity(orderItem.getQuantity())
                    .lineTotal(orderItem.getLineTotal())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PaymentDto {

        private final String id;
        private final OrderPayment.Status status;
        private final OrderPayment.Method method;
        private final Long amount;

        public static PaymentDto from(OrderPayment payment) {
            if (payment == null) {
                return null;
            }
            return PaymentDto.builder()
                    .id(payment.getId() != null ? payment.getId().toString() : null)
                    .status(payment.getStatus())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .build();
        }
    }
}
