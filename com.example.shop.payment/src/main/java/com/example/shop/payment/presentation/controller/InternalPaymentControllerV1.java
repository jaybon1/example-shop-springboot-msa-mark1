package com.example.shop.payment.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.payment.application.service.PaymentServiceV1;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/payments")
public class InternalPaymentControllerV1 {

    private final PaymentServiceV1 paymentServiceV1;

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiDto<Object>> postInternalPaymentsCancel(
            @PathVariable("id") UUID paymentId
    ) {
        paymentServiceV1.postInternalPaymentsCancel(paymentId);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(paymentId + " 결제가 취소되었습니다.")
                        .build()
        );
    }
}
