package com.example.shop.user.infrastructure.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.infrastructure.security.auth.CustomUserDetails;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenGenerator {

    private final JwtProperties jwtProperties;

    public JwtTokenGenerator(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return createAccessToken(
                user.getId().toString(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getUserRoleList().stream()
                        .map(role -> role.getRole().name())
                        .toList()
        );
    }

    public String generateRefreshToken(User user) {
        Objects.requireNonNull(user, "user must not be null");
        return createRefreshToken(user.getId().toString());
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        Objects.requireNonNull(userDetails, "userDetails must not be null");
        return createAccessToken(
                Objects.requireNonNull(userDetails.getId(), "userDetails.id must not be null").toString(),
                userDetails.getUsername(),
                userDetails.getNickname(),
                userDetails.getEmail(),
                userDetails.getRoleList()
        );
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        Objects.requireNonNull(userDetails, "userDetails must not be null");
        return createRefreshToken(
                Objects.requireNonNull(userDetails.getId(), "userDetails.id must not be null").toString()
        );
    }

    private String createAccessToken(
            String userId,
            String username,
            String nickname,
            String email,
            List<String> roleList
    ) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(jwtProperties.getAccessSubject())
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(jwtProperties.getAccessExpirationMillis()))
                .withClaim("id", userId)
                .withClaim("username", username)
                .withClaim("nickname", nickname)
                .withClaim("email", email)
                .withClaim("roleList", roleList)
                .sign(Algorithm.HMAC512(jwtProperties.getSecret()));
    }

    private String createRefreshToken(String userId) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(jwtProperties.getRefreshSubject())
                .withIssuedAt(now)
                .withExpiresAt(now.plusMillis(jwtProperties.getRefreshExpirationMillis()))
                .withClaim("id", userId)
                .sign(Algorithm.HMAC512(jwtProperties.getSecret()));
    }
}
