package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductStockReleaseDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductStockReturnDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductStockReleaseDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductStockReturnDtoV1;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/products")
public class InternalProductControllerV1 {

    @PostMapping("/{productId}/stock-release")
    public ResponseEntity<ApiDto<ResPostInternalProductStockReleaseDtoV1>> releaseProductStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostInternalProductStockReleaseDtoV1 reqDto
    ) {
        ResPostInternalProductStockReleaseDtoV1 responseBody = ResPostInternalProductStockReleaseDtoV1.builder()
                .productId(productId.toString())
                .orderId(reqDto.getOrderId().toString())
                .releasedQuantity(reqDto.getQuantity())
                .currentStock(Math.max(0, 100 - reqDto.getQuantity()))
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostInternalProductStockReleaseDtoV1>builder()
                        .code("PRODUCT_STOCK_RELEASED")
                        .message("상품 재고 차감이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{productId}/stock-return")
    public ResponseEntity<ApiDto<ResPostInternalProductStockReturnDtoV1>> returnProductStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostInternalProductStockReturnDtoV1 reqDto
    ) {
        ResPostInternalProductStockReturnDtoV1 responseBody = ResPostInternalProductStockReturnDtoV1.builder()
                .productId(productId.toString())
                .orderId(reqDto.getOrderId().toString())
                .returnedQuantity(reqDto.getQuantity())
                .currentStock(100 + reqDto.getQuantity())
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostInternalProductStockReturnDtoV1>builder()
                        .code("PRODUCT_STOCK_RETURNED")
                        .message("상품 재고 복원이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
