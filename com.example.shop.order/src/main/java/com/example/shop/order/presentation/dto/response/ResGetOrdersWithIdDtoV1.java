package com.example.shop.order.presentation.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetOrdersWithIdDtoV1 {

    private final OrderDto order;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderDto {
        private final String id;
        private final String status;
        private final Long totalAmount;
        private final Instant createdAt;
        private final List<OrderItemDto> orderItemList;
        private final PaymentDto payment;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderItemDto {
        private final String id;
        private final String productId;
        private final String productName;
        private final Long unitPrice;
        private final Long quantity;
        private final Long lineTotal;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PaymentDto {
        private final String id;
        private final String status;
        private final String method;
        private final Long amount;
    }
}
