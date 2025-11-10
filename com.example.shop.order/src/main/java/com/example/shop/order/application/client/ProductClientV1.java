package com.example.shop.order.application.client;

import com.example.shop.order.infrastructure.resttemplate.product.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.request.ReqPostInternalProductsReturnStockDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.response.ResGetProductDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.response.ResGetProductsDtoV1;
import java.util.UUID;

public interface ProductClientV1 {

    ResGetProductsDtoV1 getProducts(Integer page, Integer size, String sort, String name);

    ResGetProductDtoV1 getProduct(UUID productId);

    void postInternalProductsReleaseStock(ReqPostInternalProductsReleaseStockDtoV1 reqDto, String accessJwt);

    void postInternalProductsReturnStock(ReqPostInternalProductsReturnStockDtoV1 reqDto, String accessJwt);
}
