package com.example.shop.order.presentation.dto.response;

import com.example.shop.order.domain.model.Order;
import com.example.shop.order.domain.model.OrderItem;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostOrdersDtoV1 {

    private final OrderDto order;

    public static ResPostOrdersDtoV1 of(Order order) {
        return ResPostOrdersDtoV1.builder()
                .order(OrderDto.from(order))
                .build();
    }

    @Getter
    @Builder
    public static class OrderDto {

        private final String id;
        private final Order.Status status;
        private final Long totalAmount;
        private final Instant createdAt;
        private final List<OrderItemDto> orderItemList;

        public static OrderDto from(Order order) {
            return OrderDto.builder()
                    .id(order.getId() != null ? order.getId().toString() : null)
                    .status(order.getStatus())
                    .totalAmount(order.getTotalAmount())
                    .createdAt(order.getCreatedAt())
                    .orderItemList(OrderItemDto.from(order.getOrderItemList()))
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
}
