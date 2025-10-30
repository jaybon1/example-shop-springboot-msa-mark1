package com.example.shop.order.presentation.dto.request;

import com.example.shop.order.domain.vo.OrderPayment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqPostInternalOrderCompleteDtoV1 {

    @Valid
    @NotNull(message = "결제 정보를 입력해주세요.")
    private PaymentDto payment;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentDto {

        @NotNull(message = "결제 ID를 입력해주세요.")
        private UUID paymentId;

        @NotNull(message = "결제 금액을 입력해주세요.")
        @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다.")
        private Long amount;

        @NotNull(message = "결제 수단을 입력해주세요.")
        private OrderPayment.Method method;
    }
}
