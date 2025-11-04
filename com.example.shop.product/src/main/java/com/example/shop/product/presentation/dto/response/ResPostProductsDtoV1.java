package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResPostProductsDtoV1 {

    private final ProductDto product;

    public static ResPostProductsDtoV1 of(Product product) {
        return ResPostProductsDtoV1.builder()
                .product(ProductDto.from(product))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductDto {
        private final String id;

        public static ProductDto from(Product product) {
            return ProductDto.builder()
                    .id(String.valueOf(product.getId()))
                    .build();
        }
    }
}
