package com.example.shop.product.presentation.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetProductsDtoV1 {

    private final List<ProductDto> productList;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductDto {
        private final String id;
        private final String name;
        private final Long price;
        private final Long stock;
    }
}
