package com.example.shop.order.infrastructure.resttemplate.product.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ReqPostInternalProductsReleaseStockDtoV1 {

    private final OrderDto order;
    private final List<ProductStockDto> productStocks;

    @Getter
    @Builder
    public static class OrderDto {
        private final UUID orderId;
    }

    @Getter
    @Builder
    public static class ProductStockDto {
        private final UUID productId;
        private final Long quantity;
    }
}
