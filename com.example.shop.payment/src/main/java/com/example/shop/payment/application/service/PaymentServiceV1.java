package com.example.shop.payment.application.service;

import com.example.shop.payment.domain.model.Payment;
import com.example.shop.payment.domain.repository.PaymentRepository;
import com.example.shop.payment.infrastructure.resttemplate.order.client.OrderRestTemplateClientV1;
import com.example.shop.payment.infrastructure.resttemplate.order.dto.request.ReqPostInternalOrderCompleteDtoV1;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.example.shop.payment.presentation.dto.request.ReqPostPaymentsDtoV1;
import com.example.shop.payment.presentation.dto.response.ResGetPaymentDtoV1;
import com.example.shop.payment.presentation.dto.response.ResPostPaymentsDtoV1;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceV1 {

    private final PaymentRepository paymentRepository;
    private final OrderRestTemplateClientV1 orderRestTemplateClientV1;

    public ResGetPaymentDtoV1 getPayment(UUID authUserId, UUID paymentId) {
        Payment payment = findPayment(paymentId);
        validateOwnership(payment, authUserId);
        return ResGetPaymentDtoV1.of(payment);
    }

    @Transactional
    public ResPostPaymentsDtoV1 postPayments(UUID authUserId, String accessJwt, ReqPostPaymentsDtoV1 reqDto) {
        // TODO 결제 처리 로직 추가
        Payment payment = Payment.builder()
                .orderId(reqDto.getPayment().getOrderId())
                .userId(authUserId)
                .status(Payment.Status.COMPLETED)
                .method(reqDto.getPayment().getMethod())
                .amount(reqDto.getPayment().getAmount())
                .transactionKey(UUID.randomUUID().toString())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        orderRestTemplateClientV1.postInternalOrdersComplete(
                savedPayment.getOrderId(),
                ReqPostInternalOrderCompleteDtoV1.builder()
                        .payment(
                                ReqPostInternalOrderCompleteDtoV1.PaymentDto.builder()
                                        .paymentId(savedPayment.getId())
                                        .amount(savedPayment.getAmount())
                                        .method(savedPayment.getMethod())
                                        .build()
                        )
                        .build(),
                accessJwt
        );
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
