package com.example.shop.user.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.infrastructure.config.security.jwt.JwtProperties;
import com.example.shop.user.infrastructure.config.security.jwt.JwtTokenGenerator;
import com.example.shop.user.presentation.advice.AuthError;
import com.example.shop.user.presentation.advice.AuthException;
import com.example.shop.user.presentation.dto.request.ReqAuthPostRefreshDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthAccessTokenCheckDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.request.ReqPostAuthRegisterDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthAccessTokenCheckDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProperties jwtProperties;

    @Transactional
    public void register(ReqPostAuthRegisterDtoV1 reqDto) {
        ReqPostAuthRegisterDtoV1.UserDto requestUser = reqDto.getUser();
        userRepository.findByUsername(requestUser.getUsername())
                .ifPresent(existing -> {
                    throw new AuthException(AuthError.AUTH_USER_ALREADY_EXIST);
                });

        User newUser = User.builder()
                .username(requestUser.getUsername())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .nickname(requestUser.getNickname())
                .email(requestUser.getEmail())
                .jwtValidator(0L)
                .userRoleList(List.of(
                        UserRole.builder()
                                .role(UserRole.Role.USER)
                                .build()
                ))
                .userSocialList(List.of())
                .build();
        userRepository.save(newUser);
    }

    public ResPostAuthLoginDtoV1 login(ReqPostAuthLoginDtoV1 reqDto) {
        ReqPostAuthLoginDtoV1.UserDto requestUser = reqDto.getUser();
        User user = userRepository.findByUsername(requestUser.getUsername())
                .orElseThrow(() -> new AuthException(AuthError.AUTH_USERNAME_NOT_EXIST));

        if (!passwordEncoder.matches(requestUser.getPassword(), user.getPassword())) {
            throw new AuthException(AuthError.AUTH_PASSWORD_NOT_MATCHED);
        }

        return ResPostAuthLoginDtoV1.builder()
                .accessJwt(jwtTokenGenerator.generateAccessToken(user))
                .refreshJwt(jwtTokenGenerator.generateRefreshToken(user))
                .build();
    }

    public ResPostAuthRefreshDtoV1 refresh(ReqAuthPostRefreshDtoV1 reqDto) {
        DecodedJWT decodedRefreshJwt = verifyToken(reqDto.getRefreshJwt(), jwtProperties.getRefreshSubject());
        UUID userId = parseUserId(decodedRefreshJwt).orElseThrow(() -> new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthError.AUTH_USER_CAN_NOT_FOUND));

        Instant issuedAt = decodedRefreshJwt.getIssuedAtAsInstant();
        if (issuedAt == null) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
        if (user.getJwtValidator() != null && user.getJwtValidator() > issuedAt.toEpochMilli()) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }

        return ResPostAuthRefreshDtoV1.builder()
                .accessJwt(jwtTokenGenerator.generateAccessToken(user))
                .refreshJwt(jwtTokenGenerator.generateRefreshToken(user))
                .build();
    }

    public ResPostAuthAccessTokenCheckDtoV1 checkAccessToken(ReqPostAuthAccessTokenCheckDtoV1 reqDto) {
        DecodedJWT decodedAccessJwt;
        try {
            decodedAccessJwt = verifyToken(reqDto.getAccessJwt(), jwtProperties.getAccessSubject());
        } catch (AuthException exception) {
            return ResPostAuthAccessTokenCheckDtoV1.builder()
                    .userId(null)
                    .valid(false)
                    .remainingSeconds(0L)
                    .build();
        }

        Instant expiresAt = decodedAccessJwt.getExpiresAtAsInstant();
        if (expiresAt == null) {
            return ResPostAuthAccessTokenCheckDtoV1.builder()
                    .userId(null)
                    .valid(false)
                    .remainingSeconds(0L)
                    .build();
        }

        Optional<UUID> userIdOptional = parseUserId(decodedAccessJwt);
        if (userIdOptional.isEmpty()) {
            return ResPostAuthAccessTokenCheckDtoV1.builder()
                    .userId(null)
                    .valid(false)
                    .remainingSeconds(0L)
                    .build();
        }

        boolean valid = true;
        long remainingSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        if (remainingSeconds <= 0) {
            valid = false;
            remainingSeconds = 0;
        } else {
            UUID userId = userIdOptional.get();
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user == null) {
                valid = false;
                remainingSeconds = 0;
            } else if (user.getJwtValidator() != null
                    && decodedAccessJwt.getIssuedAtAsInstant() != null
                    && user.getJwtValidator() > decodedAccessJwt.getIssuedAtAsInstant().toEpochMilli()) {
                valid = false;
                remainingSeconds = 0;
            }
        }

        return ResPostAuthAccessTokenCheckDtoV1.builder()
                .userId(valid ? userIdOptional.orElse(null) : null)
                .valid(valid)
                .remainingSeconds(remainingSeconds)
                .build();
    }

    private DecodedJWT verifyToken(String token, String subject) {
        if (!StringUtils.hasText(token)) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            if (!subject.equals(decodedJWT.getSubject())) {
                throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
            }
            return decodedJWT;
        } catch (JWTVerificationException exception) {
            throw new AuthException(AuthError.AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    private Optional<UUID> parseUserId(DecodedJWT decodedJWT) {
        String idClaim = decodedJWT.getClaim("id").asString();
        if (!StringUtils.hasText(idClaim)) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(idClaim));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
