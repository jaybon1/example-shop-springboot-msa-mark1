package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.application.service.ProductServiceV1;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products")
public class ProductControllerV1 {

    private final ProductServiceV1 productServiceV1;

    @GetMapping
    public ResponseEntity<ApiDto<ResGetProductsDtoV1>> getProducts(
            @PageableDefault Pageable pageable,
            @RequestParam(value = "name", required = false) String name
    ) {
        ResGetProductsDtoV1 responseBody = productServiceV1.getProducts(pageable, name);
        return ResponseEntity.ok(
                ApiDto.<ResGetProductsDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetProductDtoV1>> getProduct(@PathVariable("id") UUID productId) {
        ResGetProductDtoV1 responseBody = productServiceV1.getProduct(productId);
        return ResponseEntity.ok(
                ApiDto.<ResGetProductDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostProductsDtoV1>> postProducts(
            @RequestBody @Valid ReqPostProductsDtoV1 reqDto
    ) {
        ResPostProductsDtoV1 responseBody = productServiceV1.postProducts(reqDto);
        return ResponseEntity.ok(
                ApiDto.<ResPostProductsDtoV1>builder()
                        .message("상품 등록이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
