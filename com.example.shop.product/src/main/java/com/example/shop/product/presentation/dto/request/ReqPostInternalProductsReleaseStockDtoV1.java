package com.example.shop.product.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ReqPostInternalProductsReleaseStockDtoV1 {

    @Valid
    @NotNull
    private OrderDto order;

    @Valid
    @NotEmpty(message = "상품 재고 정보는 최소 1개 이상이어야 합니다.")
    private List<ProductStockDto> productStocks;

    @Getter
    @Builder
    public static class OrderDto {

        @NotNull(message = "주문 ID를 입력해주세요.")
        private UUID orderId;

    }

    @Getter
    @Builder
    public static class ProductStockDto {

        @NotNull(message = "상품 ID를 입력해주세요.")
        private UUID productId;

        @NotNull(message = "차감 수량을 입력해주세요.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Long quantity;

    }

}
