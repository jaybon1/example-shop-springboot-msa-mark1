package com.example.shop.product.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResPostInternalProductReleaseStockDtoV1 {

    private final String productId;
    private final String orderId;
    private final Long releasedQuantity;
    private final Long currentStock;
}
