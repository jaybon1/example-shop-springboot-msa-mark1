package com.example.shop.product.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shop.product.presentation.dto.response.ResGetProductDtoV1;
import com.example.shop.product.presentation.dto.response.ResPostProductsDtoV1;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products")
public class ProductControllerV1 {

    @GetMapping
    public ResponseEntity<ApiDto<ResGetProductsDtoV1>> getProducts() {
        List<ResGetProductsDtoV1.ProductDto> productDtoList = List.of(
                ResGetProductsDtoV1.ProductDto.builder()
                        .id(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa").toString())
                        .name("샘플 상품 A")
                        .price(10_000L)
                        .stock(5L)
                        .build(),
                ResGetProductsDtoV1.ProductDto.builder()
                        .id(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb").toString())
                        .name("샘플 상품 B")
                        .price(25_000L)
                        .stock(2L)
                        .build()
        );

        ResGetProductsDtoV1 responseBody = ResGetProductsDtoV1.builder()
                .products(productDtoList)
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetProductsDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDto<ResGetProductDtoV1>> getProduct(@PathVariable("id") UUID productId) {
        ResGetProductDtoV1 responseBody = ResGetProductDtoV1.builder()
                .product(
                        ResGetProductDtoV1.ProductDto.builder()
                                .id(productId.toString())
                                .name("단일 상품")
                                .price(15_000L)
                                .stock(7L)
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResGetProductDtoV1>builder()
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiDto<ResPostProductsDtoV1>> createProduct(
            @RequestBody @Valid ReqPostProductsDtoV1 reqDto
    ) {
        ResPostProductsDtoV1 responseBody = ResPostProductsDtoV1.builder()
                .product(
                        ResPostProductsDtoV1.ProductDto.builder()
                                .id(UUID.randomUUID().toString())
                                .build()
                )
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostProductsDtoV1>builder()
                        .message("상품 등록이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
