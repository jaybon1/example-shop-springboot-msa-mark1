package com.example.shop.order.application.client;

import java.util.UUID;

public interface PaymentClientV1 {

    void postInternalPaymentsCancel(UUID paymentId, String accessJwt);
}
