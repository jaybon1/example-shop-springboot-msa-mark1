package com.example.shop.order.infrastructure.resttemplate.payment.client;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.order.application.client.PaymentClientV1;
import com.example.shop.order.presentation.advice.OrderError;
import com.example.shop.order.presentation.advice.OrderException;
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
public class PaymentRestTemplateClientV1 implements PaymentClientV1 {

    private static final String PAYMENT_SERVICE_BASE_URL = "http://payment-service";
    private static final String POST_INTERNAL_PAYMENT_CANCEL_URL =
            PAYMENT_SERVICE_BASE_URL + "/internal/v1/payments/{paymentId}/cancel";

    private static final ParameterizedTypeReference<ApiDto<Object>> API_DTO_OBJECT_TYPE =
            new ParameterizedTypeReference<>() {
            };
    private static final TypeReference<ApiDto<Object>> API_DTO_TYPE_REFERENCE = new TypeReference<>() {
    };

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void postInternalPaymentsCancel(UUID paymentId, String accessJwt) {
        HttpHeaders headers = createJsonHeadersWithAuthorization(accessJwt);
        HttpEntity<Void> httpEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ApiDto<Object>> responseEntity = restTemplate.exchange(
                    POST_INTERNAL_PAYMENT_CANCEL_URL,
                    HttpMethod.POST,
                    httpEntity,
                    API_DTO_OBJECT_TYPE,
                    paymentId
            );
            validateResponse(responseEntity);
        } catch (HttpStatusCodeException exception) {
            throw mapException(exception);
        } catch (RestClientException exception) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
    }

    private void validateResponse(ResponseEntity<ApiDto<Object>> responseEntity) {
        if (responseEntity.getBody() == null) {
            throw new OrderException(OrderError.ORDER_BAD_REQUEST);
        }
    }

    private OrderException mapException(HttpStatusCodeException exception) {
        String responseBody = exception.getResponseBodyAsString();
        if (StringUtils.hasText(responseBody)) {
            try {
                ApiDto<Object> apiDto = objectMapper.readValue(responseBody, API_DTO_TYPE_REFERENCE);
                String errorCode = apiDto.getCode();
                if ("PAYMENT_NOT_FOUND".equals(errorCode) || "PAYMENT_ALREADY_CANCELLED".equals(errorCode)) {
                    return new OrderException(OrderError.ORDER_BAD_REQUEST);
                }
            } catch (Exception parseException) {
                log.warn("Payment service error response parsing failed: {}", responseBody, parseException);
            }
        }
        return new OrderException(OrderError.ORDER_BAD_REQUEST);
    }

    private HttpHeaders createJsonHeadersWithAuthorization(String accessJwt) {
        if (!StringUtils.hasText(accessJwt)) {
            throw new OrderException(OrderError.ORDER_FORBIDDEN);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessJwt);
        return headers;
    }
}
