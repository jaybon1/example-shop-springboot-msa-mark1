package com.example.shop.order.infrastructure.resttemplate.product.client;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.infrastructure.resttemplate.product.dto.request.ReqPostInternalProductsReleaseStockDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.request.ReqPostInternalProductsReturnStockDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.response.ResGetProductDtoV1;
import com.example.shop.order.infrastructure.resttemplate.product.dto.response.ResGetProductsDtoV1;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRestTemplateClientV1 {

    private static final String PRODUCT_SERVICE_BASE_URL = "http://product-service";
    private static final String GET_PRODUCTS_URL = PRODUCT_SERVICE_BASE_URL + "/v1/products";
    private static final String GET_PRODUCT_URL = PRODUCT_SERVICE_BASE_URL + "/v1/products/{productId}";
    private static final String POST_RELEASE_STOCK_URL = PRODUCT_SERVICE_BASE_URL + "/internal/v1/products/release-stock";
    private static final String POST_RETURN_STOCK_URL = PRODUCT_SERVICE_BASE_URL + "/internal/v1/products/return-stock";

    private static final ParameterizedTypeReference<ApiDto<ResGetProductsDtoV1>> RES_GET_PRODUCTS_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiDto<ResGetProductDtoV1>> RES_GET_PRODUCT_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final ParameterizedTypeReference<ApiDto<Object>> API_DTO_OBJECT_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final TypeReference<ApiDto<Object>> API_DTO_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ResGetProductsDtoV1 getProducts(Integer page, Integer size, String sort, String name) {
        String url = buildGetProductsUrl(page, size, sort, name);

        try {
            ResponseEntity<ApiDto<ResGetProductsDtoV1>> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    RES_GET_PRODUCTS_TYPE
            );
            return extractData(responseEntity);
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST, exception);
        }
    }

    public ResGetProductDtoV1 getProduct(UUID productId) {
        try {
            ResponseEntity<ApiDto<ResGetProductDtoV1>> responseEntity = restTemplate.exchange(
                    GET_PRODUCT_URL,
                    HttpMethod.GET,
                    null,
                    RES_GET_PRODUCT_TYPE,
                    productId
            );
            return extractData(responseEntity);
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST, exception);
        }
    }

    public void postInternalProductsReleaseStock(ReqPostInternalProductsReleaseStockDtoV1 reqDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReqPostInternalProductsReleaseStockDtoV1> httpEntity = new HttpEntity<>(reqDto, headers);

        try {
            restTemplate.exchange(
                    POST_RELEASE_STOCK_URL,
                    HttpMethod.POST,
                    httpEntity,
                    API_DTO_OBJECT_TYPE
            );
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST, exception);
        }
    }

    public void postInternalProductsReturnStock(ReqPostInternalProductsReturnStockDtoV1 reqDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReqPostInternalProductsReturnStockDtoV1> httpEntity = new HttpEntity<>(reqDto, headers);

        try {
            restTemplate.exchange(
                    POST_RETURN_STOCK_URL,
                    HttpMethod.POST,
                    httpEntity,
                    API_DTO_OBJECT_TYPE
            );
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST, exception);
        }
    }

    private String buildGetProductsUrl(Integer page, Integer size, String sort, String name) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(GET_PRODUCTS_URL);
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (size != null) {
            builder.queryParam("size", size);
        }
        if (StringUtils.hasText(sort)) {
            builder.queryParam("sort", sort);
        }
        if (StringUtils.hasText(name)) {
            builder.queryParam("name", name);
        }
        return builder.toUriString();
    }

    private <T> T extractData(ResponseEntity<ApiDto<T>> responseEntity) {
        ApiDto<T> body = responseEntity.getBody();
        if (body == null || body.getData() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
        return body.getData();
    }

    private OrderException mapException(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (StringUtils.hasText(responseBody)) {
            try {
                ApiDto<Object> apiDto = objectMapper.readValue(responseBody, API_DTO_TYPE_REFERENCE);
                String errorCode = apiDto.getCode();
                if ("PRODUCT_CAN_NOT_FOUND".equals(errorCode)) {
                    return new OrderException(OrderError.ORDER_PRODUCT_NOT_FOUND, exception);
                }
                if ("PRODUCT_STOCK_NOT_ENOUGH".equals(errorCode)) {
                    return new OrderException(OrderError.ORDER_PRODUCT_OUT_OF_STOCK, exception);
                }
            } catch (Exception parseException) {
                log.warn("Product service error response parsing failed: {}", responseBody, parseException);
            }
        }
        return new OrderException(OrderError.ORDER_BAD_REQUEST, exception);
    }
}
