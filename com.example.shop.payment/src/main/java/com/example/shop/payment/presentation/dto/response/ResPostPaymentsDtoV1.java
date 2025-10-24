package com.example.shop.payment.presentation.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResPostPaymentsDtoV1 {

    private final PaymentDto payment;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class PaymentDto {
        private final String id;
        private final String status;
        private final String method;
        private final Long amount;
        private final Instant approvedAt;
    }
}
