package com.example.shop.product.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

@Getter
@Builder
public class ResGetProductsDtoV1 {

    private final ProductPageDto productPage;

    @Getter
    @ToString
    public static class ProductPageDto extends PagedModel<ProductPageDto.ProductDto> {

        public ProductPageDto(Page<ProductDto> page) {
            super(page);
        }

        @Getter
        @Builder
        public static class ProductDto {
            private final String id;
            private final String name;
            private final Long price;
            private final Long stock;

        }

    }

}
