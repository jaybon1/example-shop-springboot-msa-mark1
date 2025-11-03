package com.example.shop.order.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.application.service.OrderServiceV1;
import com.example.shop.order.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/v1/orders")
public class OrderControllerV1 {

    private final OrderServiceV1 orderServiceV1;

    @GetMapping
    public ResponseEntity<ApiDto<ResGetOrdersDtoV1>> getOrders(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PageableDefault Pageable pageable
    ) {
        ResGetOrdersDtoV1 responseBody = orderServiceV1.getOrders(customUserDetails.getId(), customUserDetails.getRoleList(), pageable);
        return ResponseEntity.ok(
                ApiDto.<ResGetOrdersDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetOrderDtoV1>> getOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID orderId
    ) {
        ResGetOrderDtoV1 responseBody = orderServiceV1.getOrder(customUserDetails.getId(), customUserDetails.getRoleList(), orderId);
        return ResponseEntity.ok(
                ApiDto.<ResGetOrderDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostOrdersDtoV1>> postOrders(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPostOrdersDtoV1 reqDto
    ) {
        ResPostOrdersDtoV1 responseBody = orderServiceV1.postOrders(
                customUserDetails.getId(),
                customUserDetails.getAccessJwt(),
                reqDto
        );
        return ResponseEntity.ok(
                ApiDto.<ResPostOrdersDtoV1>builder()
                        .message("주문이 생성되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiDto<Object>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @PathVariable("id") UUID orderId
    ) {
        orderServiceV1.cancelOrder(
                customUserDetails.getId(),
                customUserDetails.getRoleList(),
                customUserDetails.getAccessJwt(),
                orderId
        );
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(orderId + " 주문이 취소되었습니다.")
                        .build()
        );
    }
}
