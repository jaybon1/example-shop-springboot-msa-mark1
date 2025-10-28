package com.example.shop.product.presentation.dto.response;

import com.example.shop.product.domain.model.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter
@Builder
public class ResGetProductsDtoV1 {

    private final ProductPageDto productPage;

    public static ResGetProductsDtoV1 of(Page<Product> productPage) {
        return ResGetProductsDtoV1.builder()
                .productPage(new ProductPageDto(productPage))
                .build();
    }

    @Getter
    @ToString
    public static class ProductPageDto extends PagedModel<ProductPageDto.ProductDto> {

        public ProductPageDto(Page<Product> productPage) {
            super(
                    new PageImpl<>(
                            ProductDto.from(productPage.getContent()),
                            productPage.getPageable(),
                            productPage.getTotalElements()
                    )
            );
        }

        public ProductPageDto(ProductDto... productDtoArray) {
            super(
                    new PageImpl<>(
                            List.of(productDtoArray)
                    )
            );
        }

        @Getter
        @Builder
        public static class ProductDto {
            private final String id;
            private final String name;
            private final Long price;
            private final Long stock;

            private static List<ProductDto> from(List<Product> productList) {
                return productList.stream()
                        .map(ProductDto::from)
                        .toList();
            }

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
}
