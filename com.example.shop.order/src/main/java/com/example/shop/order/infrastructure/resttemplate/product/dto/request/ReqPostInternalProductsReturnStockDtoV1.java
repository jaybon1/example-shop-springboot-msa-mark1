package com.example.shop.order.infrastructure.resttemplate.product.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReqPostInternalProductsReturnStockDtoV1 {

    private final OrderDto order;

    @Getter
    @Builder
    public static class OrderDto {
        private final UUID orderId;
    }
}
