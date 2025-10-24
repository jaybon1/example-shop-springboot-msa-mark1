package com.example.shop.order.presentation.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetOrdersDtoV1 {

    private final List<OrderDto> orderList;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OrderDto {
        private final String id;
        private final String status;
        private final Long totalAmount;
        private final Instant createdAt;
    }
}
