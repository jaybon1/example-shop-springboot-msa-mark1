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
public class ReqPostInternalProductsReturnStockDtoV1 {

    @Valid
    @NotNull
    private OrderDto order;

    @Getter
    @Builder
    public static class OrderDto {

        @NotNull(message = "주문 ID를 입력해주세요.")
        private UUID orderId;

    }

}
