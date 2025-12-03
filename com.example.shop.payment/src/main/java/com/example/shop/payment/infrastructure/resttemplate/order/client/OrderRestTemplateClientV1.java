package com.example.shop.payment.infrastructure.resttemplate.order.client;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.payment.application.client.OrderClientV1;
import com.example.shop.payment.infrastructure.resttemplate.order.dto.request.ReqPostInternalOrderCompleteDtoV1;
import com.example.shop.payment.presentation.advice.PaymentError;
import com.example.shop.payment.presentation.advice.PaymentException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRestTemplateClientV1 implements OrderClientV1 {

    private static final String ORDER_SERVICE_BASE_URL = "http://order-service";
    private static final String POST_INTERNAL_ORDER_COMPLETE_URL =
            ORDER_SERVICE_BASE_URL + "/internal/v1/orders/{orderId}/complete";

    private static final ParameterizedTypeReference<ApiDto<Object>> API_DTO_OBJECT_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final TypeReference<ApiDto<Object>> API_DTO_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void postInternalOrdersComplete(UUID orderId, ReqPostInternalOrderCompleteDtoV1 reqDto, String accessJwt) {
        HttpHeaders headers = createJsonHeadersWithAuthorization(accessJwt);
        HttpEntity<ReqPostInternalOrderCompleteDtoV1> httpEntity = new HttpEntity<>(reqDto, headers);

        try {
            ResponseEntity<ApiDto<Object>> responseEntity = restTemplate.exchange(
                    POST_INTERNAL_ORDER_COMPLETE_URL,
                    HttpMethod.POST,
                    httpEntity,
                    API_DTO_OBJECT_TYPE,
                    orderId
            );
            validateResponse(responseEntity);
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new PaymentException(PaymentError.PAYMENT_BAD_REQUEST);
        }
    }

    private void validateResponse(ResponseEntity<ApiDto<Object>> responseEntity) {
        ApiDto<Object> body = responseEntity.getBody();
        if (body == null) {
            throw new PaymentException(PaymentError.PAYMENT_BAD_REQUEST);
        }
    }

    private PaymentException mapException(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (StringUtils.hasText(responseBody)) {
            try {
                ApiDto<Object> apiDto = objectMapper.readValue(responseBody, API_DTO_TYPE_REFERENCE);
                String errorCode = apiDto.getCode();
                if ("ORDER_PAYMENT_AMOUNT_MISMATCH".equals(errorCode)) {
                    return new PaymentException(PaymentError.PAYMENT_INVALID_AMOUNT);
                }
                if ("ORDER_NOT_FOUND".equals(errorCode)
                        || "ORDER_ALREADY_CANCELLED".equals(errorCode)
                        || "ORDER_ALREADY_PAID".equals(errorCode)) {
                    return new PaymentException(PaymentError.PAYMENT_ORDER_BAD_REQUEST);
                }
            } catch (Exception parseException) {
                log.warn("Order service error response parsing failed: {}", responseBody, parseException);
            }
        }
        return new PaymentException(PaymentError.PAYMENT_BAD_REQUEST);
    }

    private HttpHeaders createJsonHeadersWithAuthorization(String accessJwt) {
        if (!StringUtils.hasText(accessJwt)) {
            throw new PaymentException(PaymentError.PAYMENT_FORBIDDEN);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessJwt);
        return headers;
    }
}
