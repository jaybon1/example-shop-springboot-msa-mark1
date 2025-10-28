package com.example.shop.payment.infrastructure.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.payment.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.payment.infrastructure.security.jwt.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(jwtProperties.getAccessHeaderName());
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith(jwtProperties.getHeaderPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessJwt = authorizationHeader.substring(jwtProperties.getHeaderPrefix().length());
        DecodedJWT decodedAccessJwt = JWT.decode(accessJwt);

        CustomUserDetails userDetails;
        try {
            userDetails = CustomUserDetails.from(decodedAccessJwt);
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
