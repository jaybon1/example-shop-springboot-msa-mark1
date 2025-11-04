package com.example.shop.payment.presentation.dto.response;

import com.example.shop.payment.domain.model.Payment;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostPaymentsDtoV1 {

    private final PaymentDto payment;

    public static ResPostPaymentsDtoV1 of(Payment payment) {
        return ResPostPaymentsDtoV1.builder()
                .payment(PaymentDto.from(payment))
                .build();
    }

    @Getter
    @Builder
    public static class PaymentDto {
        private final String id;

        public static PaymentDto from(Payment payment) {
            return PaymentDto.builder()
                    .id(String.valueOf(payment.getId()))
                    .build();
        }
    }
}
