package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductReturnStockDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostInternalProductReturnStockDtoV1;
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
@RequestMapping("/internal/v1/products")
public class InternalProductControllerV1 {

    private final ProductServiceV1 productServiceV1;

    @PostMapping("/{productId}/release-stock")
    public ResponseEntity<ApiDto<ResPostInternalProductReleaseStockDtoV1>> postInternalProductReleaseStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostInternalProductReleaseStockDtoV1 reqDto
    ) {
        ResPostInternalProductReleaseStockDtoV1 responseBody = productServiceV1.postInternalProductReleaseStock(
                productId,
                reqDto
        );

        return ResponseEntity.ok(
                ApiDto.<ResPostInternalProductReleaseStockDtoV1>builder()
                        .code("PRODUCT_STOCK_RELEASED")
                        .message("상품 재고 차감이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/{productId}/return-stock")
    public ResponseEntity<ApiDto<ResPostInternalProductReturnStockDtoV1>> postInternalProductReturnStock(
            @PathVariable("productId") UUID productId,
            @RequestBody @Valid ReqPostInternalProductReturnStockDtoV1 reqDto
    ) {
        ResPostInternalProductReturnStockDtoV1 responseBody = productServiceV1.postInternalProductReturnStock(
                productId,
                reqDto
        );

        return ResponseEntity.ok(
                ApiDto.<ResPostInternalProductReturnStockDtoV1>builder()
                        .code("PRODUCT_STOCK_RETURNED")
                        .message("상품 재고 복원이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
