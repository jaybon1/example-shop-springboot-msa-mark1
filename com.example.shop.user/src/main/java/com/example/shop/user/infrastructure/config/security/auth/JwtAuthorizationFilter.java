package com.example.shop.user.infrastructure.config.security.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.user.infrastructure.config.security.jwt.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    public JwtAuthorizationFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(jwtProperties.getAccessHeaderName());
        if (!StringUtils.hasText(header) || !header.startsWith(jwtProperties.getHeaderPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(jwtProperties.getHeaderPrefix().length());
        DecodedJWT decodedJwt;
        try {
            decodedJwt = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtProperties.getAccessSubject().equals(decodedJwt.getSubject())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (decodedJwt.getExpiresAtAsInstant().isBefore(Instant.now())) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails userDetails;
        try {
            userDetails = CustomUserDetails.from(decodedJwt);
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
