package com.example.shop.order.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.presentation.dto.request.ReqPostOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrdersDtoV1;
import com.example.shop.order.presentation.dto.response.ResGetOrderDtoV1;
import com.example.shop.order.presentation.dto.response.ResPostOrdersDtoV1;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/orders")
public class OrderControllerV1 {

    @GetMapping
    public ResponseEntity<ApiDto<ResGetOrdersDtoV1>> getOrders() {
        List<ResGetOrdersDtoV1.OrderDto> orderDtoList = List.of(
                ResGetOrdersDtoV1.OrderDto.builder()
                        .id(UUID.fromString("aaaaaaaa-1111-2222-3333-bbbbbbbbbbbb").toString())
                        .status("CREATED")
                        .totalAmount(45_000L)
                        .createdAt(Instant.parse("2024-01-01T10:15:30Z"))
                        .build(),
                ResGetOrdersDtoV1.OrderDto.builder()
                        .id(UUID.fromString("cccccccc-1111-2222-3333-dddddddddddd").toString())
                        .status("PAID")
                        .totalAmount(90_000L)
                        .createdAt(Instant.parse("2024-02-01T11:22:33Z"))
                        .build()
        );

        ResGetOrdersDtoV1 responseBody = ResGetOrdersDtoV1.builder()
                .orders(orderDtoList)
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetOrdersDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetOrderDtoV1>> getOrder(@PathVariable("id") UUID orderId) {
        ResGetOrderDtoV1 responseBody = ResGetOrderDtoV1.builder()
                .order(
                        ResGetOrderDtoV1.OrderDto.builder()
                                .id(orderId.toString())
                                .status("PAID")
                                .totalAmount(120_000L)
                                .createdAt(Instant.parse("2024-03-01T09:00:00Z"))
                                .orderItemList(List.of(
                                        ResGetOrderDtoV1.OrderItemDto.builder()
                                                .id(UUID.fromString("10101010-aaaa-bbbb-cccc-111111111111").toString())
                                                .productId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa").toString())
                                                .productName("샘플 상품 A")
                                                .unitPrice(40_000L)
                                                .quantity(2L)
                                                .lineTotal(80_000L)
                                                .build(),
                                        ResGetOrderDtoV1.OrderItemDto.builder()
                                                .id(UUID.fromString("20202020-aaaa-bbbb-cccc-222222222222").toString())
                                                .productId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb").toString())
                                                .productName("샘플 상품 B")
                                                .unitPrice(20_000L)
                                                .quantity(2L)
                                                .lineTotal(40_000L)
                                                .build()
                                ))
                                .payment(
                                        ResGetOrderDtoV1.PaymentDto.builder()
                                                .id(UUID.fromString("99999999-9999-9999-9999-999999999999").toString())
                                                .status("COMPLETED")
                                                .method("CARD")
                                                .amount(120_000L)
                                                .build()
                                )
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetOrderDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostOrdersDtoV1>> createOrder(
            @RequestBody @Valid ReqPostOrdersDtoV1 reqDto
    ) {
        ResPostOrdersDtoV1 responseBody = ResPostOrdersDtoV1.builder()
                .order(
                        ResPostOrdersDtoV1.OrderDto.builder()
                                .id(UUID.randomUUID().toString())
                                .status("CREATED")
                                .totalAmount(55_000L)
                                .createdAt(Instant.now())
                                .orderItemList(List.of(
                                        ResPostOrdersDtoV1.OrderItemDto.builder()
                                                .productId(reqDto.getOrder().getOrderItemList().get(0).getProductId().toString())
                                                .productName("요청 상품")
                                                .unitPrice(55_000L)
                                                .quantity(reqDto.getOrder().getOrderItemList().get(0).getQuantity())
                                                .lineTotal(55_000L)
                                                .build()
                                ))
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostOrdersDtoV1>builder()
                        .message("주문이 생성되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiDto<Object>> cancelOrder(@PathVariable("id") UUID orderId) {
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message(orderId + " 주문이 취소되었습니다.")
                        .build()
        );
    }
}
