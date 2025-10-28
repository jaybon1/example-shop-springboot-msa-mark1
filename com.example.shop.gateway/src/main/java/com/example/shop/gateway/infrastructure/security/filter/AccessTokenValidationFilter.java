package com.example.shop.gateway.infrastructure.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.gateway.infrastructure.redis.client.AuthRedisClient;
import com.example.shop.gateway.infrastructure.security.jwt.JwtProperties;
import com.example.shop.gateway.presentation.advice.GatewayError;
import com.example.shop.gateway.presentation.advice.GatewayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenValidationFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;

    private final AuthRedisClient authRedisClient;

//    private final AuthRestTemplateClientV1 authRestTemplateClientV1;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessJwt = resolveAccessToken(authorizationHeader);

        if (!StringUtils.hasText(accessJwt)) {
            return chain.filter(removeAuthorizationHeaderFromExchange(exchange));
        }

        DecodedJWT decodedAccessJwt;
        try {
            decodedAccessJwt = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret()))
                    .build()
                    .verify(accessJwt);
        } catch (JWTVerificationException exception) {
            throw new GatewayException(GatewayError.GATEWAY_TOKEN_INVALID);
        }

        String id = decodedAccessJwt.getClaim("id").asString();
        if (!StringUtils.hasText(id)) {
            throw new GatewayException(GatewayError.GATEWAY_TOKEN_INVALID);
        }

        Long jwtValidator = authRedisClient.getBy(id);
        if (jwtValidator != null && jwtValidator > decodedAccessJwt.getIssuedAtAsInstant().toEpochMilli()) {
            throw new GatewayException(GatewayError.GATEWAY_TOKEN_INVALID);
        }

        return chain.filter(exchange);

    }

    private static ServerWebExchange removeAuthorizationHeaderFromExchange(ServerWebExchange exchange) {
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();
        return mutatedExchange;
    }

    private String resolveAccessToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }

        if (authorizationHeader.regionMatches(true, 0, jwtProperties.getHeaderPrefix(), 0, jwtProperties.getHeaderPrefix().length())) {
            return authorizationHeader.substring(jwtProperties.getHeaderPrefix().length()).trim();
        }
        return authorizationHeader.trim();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
