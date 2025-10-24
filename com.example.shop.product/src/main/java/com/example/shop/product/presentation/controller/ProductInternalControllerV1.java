package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.presentation.dto.request.ReqPostProductStockAdjustDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductStockReleaseDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductStockReturnDtoV1;
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
public class ProductInternalControllerV1 {

    @PostMapping("/{productId}/stock-release")
    public ResponseEntity<ApiDto<ResPostProductStockReleaseDtoV1>> releaseProductStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostProductStockAdjustDtoV1 reqDto
    ) {
        ResPostProductStockReleaseDtoV1 responseBody = ResPostProductStockReleaseDtoV1.builder()
                .productId(productId.toString())
                .orderId(reqDto.getOrderId().toString())
                .releasedQuantity(reqDto.getQuantity())
                .currentStock(Math.max(0, 100 - reqDto.getQuantity()))
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostProductStockReleaseDtoV1>builder()
                        .code("PRODUCT_STOCK_RELEASED")
                        .message("상품 재고 차감이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{productId}/stock-return")
    public ResponseEntity<ApiDto<ResPostProductStockReturnDtoV1>> returnProductStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostProductStockAdjustDtoV1 reqDto
    ) {
        ResPostProductStockReturnDtoV1 responseBody = ResPostProductStockReturnDtoV1.builder()
                .productId(productId.toString())
                .orderId(reqDto.getOrderId().toString())
                .returnedQuantity(reqDto.getQuantity())
                .currentStock(100 + reqDto.getQuantity())
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostProductStockReturnDtoV1>builder()
                        .code("PRODUCT_STOCK_RETURNED")
                        .message("상품 재고 복원이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
