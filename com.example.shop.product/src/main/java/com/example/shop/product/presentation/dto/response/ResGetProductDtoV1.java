package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResGetProductDtoV1 {

    private final ProductDto product;

    public static ResGetProductDtoV1 of(Product product) {
        return ResGetProductDtoV1.builder()
                .product(ProductDto.from(product))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProductDto {
        private final String id;
        private final String name;
        private final Long price;
        private final Long stock;

        public static ProductDto from(Product product) {
            return ProductDto.builder()
                    .id(product.getId().toString())
                    .name(product.getName())
                    .price(product.getPrice())
                    .stock(product.getStock())
                    .build();
        }
    }
}
