package com.example.shop.payment.application.service;

import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceV1 {

    private final PaymentRepository paymentRepository;

    public ResGetPaymentDtoV1 getPayment(UUID authUserId, UUID paymentId) {
        Payment payment = findPayment(paymentId);
        validateOwnership(payment, authUserId);
        return ResGetPaymentDtoV1.of(payment);
    }

    @Transactional
    public ResPostPaymentsDtoV1 postPayments(UUID authUserId, ReqPostPaymentsDtoV1 reqDto) {
        // TODO 결제 처리 로직 추가
        Payment payment = Payment.builder()
                .orderId(reqDto.getPayment().getOrderId())
                .userId(authUserId)
                .status(Payment.Status.COMPLETED)
                .method(reqDto.getPayment().getMethod())
                .amount(reqDto.getPayment().getAmount())
                .transactionKey(null)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        // TODO 주문 완료 처리 로직 추가
        return ResPostPaymentsDtoV1.of(savedPayment);
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PaymentError.PAYMENT_NOT_FOUND));
    }

    private void validateOwnership(Payment payment, UUID authUserId) {
        if (!payment.isOwnedBy(authUserId)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }
    }

}
