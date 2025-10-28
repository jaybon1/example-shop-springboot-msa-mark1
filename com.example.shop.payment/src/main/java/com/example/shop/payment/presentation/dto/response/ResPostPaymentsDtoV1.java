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
        private final String status;
        private final String method;
        private final Long amount;
        private final Instant approvedAt;
        private final String transactionKey;
        private final String orderId;

        public static PaymentDto from(Payment payment) {
            return PaymentDto.builder()
                    .id(payment.getId().toString())
                    .status(payment.getStatus().toString())
                    .method(payment.getMethod().toString())
                    .amount(payment.getAmount())
                    .approvedAt(payment.getCreatedAt())
                    .transactionKey(payment.getTransactionKey())
                    .orderId(payment.getOrderId().toString())
                    .build();
        }
    }
}
