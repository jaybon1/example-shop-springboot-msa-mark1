package com.example.shop.gateway.infrastructure.api.user.client;

import com.example.shop.gateway.infrastructure.api.user.dto.request.ReqPostAuthAccessTokenCheckDtoV1;
import com.example.shop.gateway.infrastructure.api.user.dto.response.ResPostAuthAccessTokenCheckDtoV1;
import com.example.shop.gateway.presentation.advice.GatewayError;
import com.example.shop.gateway.presentation.advice.GatewayException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
public class AuthClient {

    private static final String ACCESS_TOKEN_CHECK_URL = "http://user-service/v1/auth/access-token-check";

    private final RestTemplate restTemplate;

    public ResPostAuthAccessTokenCheckDtoV1 checkAccessToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ReqPostAuthAccessTokenCheckDtoV1 requestBody = ReqPostAuthAccessTokenCheckDtoV1.builder()
                .accessJwt(accessToken)
                .build();

        HttpEntity<ReqPostAuthAccessTokenCheckDtoV1> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<AuthApiResponse> responseEntity = restTemplate.exchange(
                    ACCESS_TOKEN_CHECK_URL,
                    HttpMethod.POST,
                    httpEntity,
                    new ParameterizedTypeReference<AuthApiResponse>() {
                    }
            );
            AuthApiResponse body = responseEntity.getBody();
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

    @Getter
    @Setter
    @NoArgsConstructor
    private static class AuthApiResponse {
        private String code;
        private String message;
        private ResPostAuthAccessTokenCheckDtoV1 data;
    }
}
