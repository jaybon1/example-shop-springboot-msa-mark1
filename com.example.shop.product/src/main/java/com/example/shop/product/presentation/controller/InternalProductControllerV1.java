package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.product.presentation.dto.request.ReqPostInternalProductsReturnStockDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1/products")
public class InternalProductControllerV1 {

    private final ProductServiceV1 productServiceV1;

    @PostMapping("/release-stock")
    public ResponseEntity<ApiDto<Object>> postInternalProductsReleaseStock(
            @RequestBody @Valid ReqPostInternalProductsReleaseStockDtoV1 reqDto
    ) {
        productServiceV1.postInternalProductsReleaseStock(reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("상품 재고 차감이 완료되었습니다.")
                        .build()
        );
    }

    @PostMapping("/return-stock")
    public ResponseEntity<ApiDto<Object>> postInternalProductsReturnStock(
            @RequestBody @Valid ReqPostInternalProductsReturnStockDtoV1 reqDto
    ) {
        productServiceV1.postInternalProductsReturnStock(reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("상품 재고 복원이 완료되었습니다.")
                        .build()
        );
    }

}
