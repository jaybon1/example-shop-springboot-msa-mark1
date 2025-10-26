package com.example.shop.gateway.infrastructure.resttemplate.auth.client;

import com.example.shop.gateway.infrastructure.resttemplate.auth.dto.request.ReqPostAuthCheckAccessTokenDtoV1;
import com.example.shop.gateway.infrastructure.resttemplate.auth.dto.response.ResPostAuthCheckAccessTokenDtoV1;
import com.example.shop.gateway.presentation.advice.GatewayError;
import com.example.shop.gateway.presentation.advice.GatewayException;
import com.example.shop.gateway.presentation.dto.ApiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRestTemplateClientV1 {

    private static final String CHECK_ACCESS_TOKEN_URL = "http://user-service/v1/auth/check-access-token";

    private final RestTemplate restTemplate;

    public ResPostAuthCheckAccessTokenDtoV1 checkAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReqPostAuthCheckAccessTokenDtoV1 requestBody = ReqPostAuthCheckAccessTokenDtoV1.builder()
                .accessJwt(accessToken)
                .build();

        HttpEntity<ReqPostAuthCheckAccessTokenDtoV1> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<ApiDto<ResPostAuthCheckAccessTokenDtoV1>> responseEntity = restTemplate.exchange(
                    CHECK_ACCESS_TOKEN_URL,
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<>() {
                    }
            );
            ApiDto<ResPostAuthCheckAccessTokenDtoV1> body = responseEntity.getBody();
            if (body == null || body.getData() == null) {
                throw new GatewayException(GatewayError.GATEWAY_USER_SERVICE_UNAVAILABLE);
            }
            return body.getData();
        } catch (HttpStatusCodeException exception) {
            throw new GatewayException(GatewayError.GATEWAY_USER_SERVICE_UNAVAILABLE, exception);
        } catch (RestClientException exception) {
            throw new GatewayException(GatewayError.GATEWAY_USER_SERVICE_UNAVAILABLE, exception);
        }
    }

}
