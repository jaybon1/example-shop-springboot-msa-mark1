package com.example.shop.product.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqPostProductsDtoV1 {

    @NotNull(message = "상품 정보를 입력해주세요.")
    @Valid
    private ProductDto product;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDto {

        @NotBlank(message = "상품명을 입력해주세요.")
        private String name;

        @NotNull(message = "가격을 입력해주세요.")
        @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
        private Long price;

        @NotNull(message = "재고를 입력해주세요.")
        @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
        private Long stock;
    }
}
