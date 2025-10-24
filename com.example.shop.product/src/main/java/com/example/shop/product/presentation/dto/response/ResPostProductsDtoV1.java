package com.example.shop.product.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResPostProductsDtoV1 {

    private final ProductDto product;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductDto {
        private final String id;
    }
}
