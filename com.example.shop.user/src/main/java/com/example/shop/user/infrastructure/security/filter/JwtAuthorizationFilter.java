package com.example.shop.user.infrastructure.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.user.infrastructure.redis.client.AuthRedisClient;
import com.example.shop.user.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    private final AuthRedisClient authRedisClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(jwtProperties.getAccessHeaderName());
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(jwtProperties.getHeaderPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessJwt = authorizationHeader.substring(jwtProperties.getHeaderPrefix().length());
        DecodedJWT decodedAccessJwt;
        try {
            decodedAccessJwt = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret()))
                    .build()
                    .verify(accessJwt);
        } catch (JWTVerificationException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        Long jwtValidator = authRedisClient.getBy(decodedAccessJwt.getClaim("id").toString());
        if (jwtValidator != null && jwtValidator > decodedAccessJwt.getIssuedAt().toInstant().getEpochSecond()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtProperties.getAccessSubject().equals(decodedAccessJwt.getSubject())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (decodedAccessJwt.getExpiresAtAsInstant().isBefore(Instant.now())) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails userDetails;
        try {
            userDetails = CustomUserDetails.of(decodedAccessJwt);
        } catch (RuntimeException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
