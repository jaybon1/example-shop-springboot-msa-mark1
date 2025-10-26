package com.example.shop.gateway.infrastructure.filter;

import com.example.shop.gateway.infrastructure.api.user.client.AuthClient;
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
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenValidationFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthClient authClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (isAuthOrUserPath(exchange.getRequest().getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String accessToken = resolveAccessToken(authorizationHeader);

        if (!StringUtils.hasText(accessToken)) {
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                    .build();
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();
            return chain.filter(mutatedExchange);
        }

        return Mono.fromCallable(() -> authClient.checkAccessToken(accessToken))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(resDto -> {
                    if (!resDto.isValid()) {
                        return Mono.error(new GatewayException(GatewayError.GATEWAY_TOKEN_INVALID));
                    }
                    return chain.filter(exchange);
                })
                .onErrorMap(throwable -> {
                    if (throwable instanceof GatewayException) {
                        return throwable;
                    }
                    return new GatewayException(GatewayError.GATEWAY_USER_SERVICE_UNAVAILABLE, throwable);
                });
    }

    private boolean isAuthOrUserPath(String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }
        return path.matches("^/v\\d+/(auth|users)(/.*)?$");
    }

    private String resolveAccessToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }

        if (authorizationHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        }
        return authorizationHeader.trim();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
