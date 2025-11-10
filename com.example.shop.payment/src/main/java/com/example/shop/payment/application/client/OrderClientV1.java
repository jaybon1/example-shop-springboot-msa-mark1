package com.example.shop.payment.application.client;

import com.example.shop.payment.infrastructure.resttemplate.order.dto.request.ReqPostInternalOrderCompleteDtoV1;
import java.util.UUID;

public interface OrderClientV1 {

    void postInternalOrdersComplete(UUID orderId, ReqPostInternalOrderCompleteDtoV1 reqDto, String accessJwt);
}
