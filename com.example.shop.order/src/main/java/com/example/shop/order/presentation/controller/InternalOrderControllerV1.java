package com.example.shop.order.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.application.service.OrderServiceV1;
import com.example.shop.order.presentation.dto.request.ReqPostInternalOrderCompleteDtoV1;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/orders")
public class InternalOrderControllerV1 {

    private final OrderServiceV1 orderServiceV1;

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiDto<Object>> postInternalOrdersComplete(
            @PathVariable("id") UUID orderId,
            @RequestBody @Valid ReqPostInternalOrderCompleteDtoV1 reqDto
    ) {
        orderServiceV1.postInternalOrdersComplete(orderId, reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(orderId + " 주문이 결제 완료되었습니다.")
                        .build()
        );
    }
}
