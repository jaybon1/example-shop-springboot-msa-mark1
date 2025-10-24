package com.example.shop.payment.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentsWithIdDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
public class PaymentControllerV1 {

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetPaymentsWithIdDtoV1>> getPayment(@PathVariable("id") UUID paymentId) {
        ResGetPaymentsWithIdDtoV1 responseBody = ResGetPaymentsWithIdDtoV1.builder()
                .payment(
                        ResGetPaymentsWithIdDtoV1.PaymentDto.builder()
                                .id(paymentId.toString())
                                .status("COMPLETED")
                                .method("CARD")
                                .amount(120_000L)
                                .approvedAt(Instant.parse("2024-03-15T12:34:56Z"))
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetPaymentsWithIdDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostPaymentsDtoV1>> createPayment(
            @RequestBody @Valid ReqPostPaymentsDtoV1 reqDto
    ) {
        ResPostPaymentsDtoV1 responseBody = ResPostPaymentsDtoV1.builder()
                .payment(
                        ResPostPaymentsDtoV1.PaymentDto.builder()
                                .id(UUID.randomUUID().toString())
                                .status("COMPLETED")
                                .method(reqDto.getMethod())
                                .amount(reqDto.getAmount())
                                .approvedAt(Instant.now())
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostPaymentsDtoV1>builder()
                        .message("결제가 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
