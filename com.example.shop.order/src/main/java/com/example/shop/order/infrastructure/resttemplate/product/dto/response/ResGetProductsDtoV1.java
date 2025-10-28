package com.example.shop.order.infrastructure.resttemplate.product.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResGetProductsDtoV1 {

    private ProductPageDto productPage;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductPageDto {

        private List<ProductDto> content;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductDto {

        private String id;
        private String name;
        private Long price;
        private Long stock;
    }
}
