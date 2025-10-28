package com.example.shop.payment.presentation.dto.request;

import com.example.shop.payment.domain.model.Payment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ReqPostPaymentsDtoV1 {

    @Valid
    @NotNull
    private PaymentDto payment;

    @Getter
    @Builder
    public static class PaymentDto {

        @NotNull(message = "주문 ID를 입력해주세요.")
        private UUID orderId;

        @NotBlank(message = "결제 수단을 입력해주세요.")
        private Payment.Method method;

        @NotNull(message = "결제 금액을 입력해주세요.")
        @Min(value = 0, message = "결제 금액은 0 이상이어야 합니다.")
        private Long amount;

    }

}
