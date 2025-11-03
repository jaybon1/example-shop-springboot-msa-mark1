package com.example.shop.payment.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.payment.application.service.PaymentServiceV1;
import com.example.shop.payment.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/payments")
public class PaymentControllerV1 {

    private final PaymentServiceV1 paymentServiceV1;

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetPaymentDtoV1>> getPayment(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID paymentId
    ) {
        ResGetPaymentDtoV1 responseBody = paymentServiceV1.getPayment(customUserDetails.getId(), paymentId);
        return ResponseEntity.ok(
                ApiDto.<ResGetPaymentDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostPaymentsDtoV1>> postPayments(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPostPaymentsDtoV1 reqDto
    ) {
        ResPostPaymentsDtoV1 responseBody = paymentServiceV1.postPayments(
                customUserDetails.getId(),
                customUserDetails.getAccessJwt(),
                reqDto
        );
        return ResponseEntity.ok(
                ApiDto.<ResPostPaymentsDtoV1>builder()
                        .message("결제가 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
