package com.example.shop.user.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shop.user.domain.model.User;
import com.example.shop.user.domain.model.UserRole;
import com.example.shop.user.domain.repository.UserRepository;
import com.example.shop.user.application.cache.AuthCache;
import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import com.example.shop.user.infrastructure.security.jwt.JwtTokenGenerator;
import com.example.shop.user.presentation.advice.AuthError;
import com.example.shop.user.presentation.advice.AuthException;
import com.example.shop.user.presentation.advice.UserError;
import com.example.shop.user.presentation.advice.UserException;
import com.example.shop.user.presentation.dto.request.*;
import com.example.shop.user.presentation.dto.response.ResPostAuthCheckAccessTokenDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceV1 {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final JwtProperties jwtProperties;

    private final AuthCache authCache;


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

        return ResPostAuthLoginDtoV1.of(
                jwtTokenGenerator.generateAccessToken(user),
                jwtTokenGenerator.generateRefreshToken(user)
        );
    }

    public ResPostAuthRefreshDtoV1 refresh(ReqPostAuthRefreshDtoV1 reqDto) {
        DecodedJWT decodedRefreshJwt = verifyToken(reqDto.getRefreshJwt(), jwtProperties.getRefreshSubject());
        UUID userId = parseUserId(decodedRefreshJwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthError.AUTH_USER_CAN_NOT_FOUND));

        Instant issuedAt = decodedRefreshJwt.getIssuedAtAsInstant();
        if (issuedAt == null) {
            throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
        }
        if (user.getJwtValidator() != null && user.getJwtValidator() > issuedAt.toEpochMilli()) {
            throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
        }

        return ResPostAuthRefreshDtoV1.of(
                jwtTokenGenerator.generateAccessToken(user),
                jwtTokenGenerator.generateRefreshToken(user)
        );
    }

    public ResPostAuthCheckAccessTokenDtoV1 checkAccessToken(ReqPostAuthCheckAccessTokenDtoV1 reqDto) {
        DecodedJWT decodedAccessJwt;
        try {
            decodedAccessJwt = verifyToken(reqDto.getAccessJwt(), jwtProperties.getAccessSubject());
        } catch (AuthException exception) {
            return ResPostAuthCheckAccessTokenDtoV1.builder()
                    .userId(null)
                    .valid(false)
                    .remainingSeconds(0L)
                    .build();
        }

        UUID userId = parseUserId(decodedAccessJwt);
        Long jwtValidator = authCache.getBy(decodedAccessJwt.getClaim("id").toString());
        if (jwtValidator != null && jwtValidator > decodedAccessJwt.getIssuedAt().toInstant().getEpochSecond()) {
            return ResPostAuthCheckAccessTokenDtoV1.builder()
                    .userId(userId.toString())
                    .valid(false)
                    .remainingSeconds(0L)
                    .build();
        }

        boolean valid = true;
        long remainingSeconds = Duration.between(Instant.now(), decodedAccessJwt.getExpiresAtAsInstant()).getSeconds();
        if (remainingSeconds <= 0) {
            valid = false;
            remainingSeconds = 0;
        } else {
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user == null) {
                valid = false;
                remainingSeconds = 0;
            } else if (user.getJwtValidator() != null
                    && decodedAccessJwt.getIssuedAtAsInstant() != null
                    && user.getJwtValidator() > decodedAccessJwt.getIssuedAtAsInstant().getEpochSecond()) {
                valid = false;
                remainingSeconds = 0;
            }
        }

        return ResPostAuthCheckAccessTokenDtoV1.builder()
                .userId(userId.toString())
                .valid(valid)
                .remainingSeconds(remainingSeconds)
                .build();
    }

    public void invalidateBeforeToken(UUID authUserId, List<String> authRoleList, ReqPostAuthInvalidateBeforeTokenDtoV1 reqDto) {
        getAuthUserOrThrow(authUserId);
        User targetUser = getUserOrThrow(reqDto.getUser().getId());
        validateAccess(authUserId, authRoleList, targetUser);
        long nowEpochSecond = Instant.now().getEpochSecond();
        User updatedUser = targetUser.updateJwtValidator(nowEpochSecond);
        authCache.denyBy(String.valueOf(updatedUser.getId()), nowEpochSecond);
        userRepository.save(updatedUser);
    }

    private void validateAccess(UUID authUserId, List<String> authUserRoleList, User targetUser) {
        boolean targetIsAdmin = targetUser.getUserRoleList().stream()
                .map(UserRole::getRole)
                .anyMatch(role -> role == UserRole.Role.ADMIN);
        if (targetIsAdmin && !isAdmin(authUserRoleList)) {
            throw new UserException(UserError.USER_BAD_REQUEST);
        }

        if ((authUserId != null && authUserId.equals(targetUser.getId()))
                || isAdmin(authUserRoleList)
                || isManager(authUserRoleList)) {
            return;
        }
        throw new UserException(UserError.USER_BAD_REQUEST);
    }

    private User getAuthUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserError.USER_BAD_REQUEST));
    }

    private User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserError.USER_CAN_NOT_FOUND));
    }

    private boolean isAdmin(List<String> authUserRoleList) {
        return !CollectionUtils.isEmpty(authUserRoleList)
                && authUserRoleList.contains(UserRole.Role.ADMIN.toString());
    }

    private boolean isManager(List<String> authUserRoleList) {
        return !CollectionUtils.isEmpty(authUserRoleList)
                && authUserRoleList.contains(UserRole.Role.MANAGER.toString());
    }

    private DecodedJWT verifyToken(String token, String subject) {
        if (!StringUtils.hasText(token)) {
            throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
        }
        try {
            DecodedJWT decodedJWT = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            if (!subject.equals(decodedJWT.getSubject())) {
                throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
            }
            return decodedJWT;
        } catch (JWTVerificationException exception) {
            throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
        }
    }

    private UUID parseUserId(DecodedJWT decodedJWT) {
        String idClaim = decodedJWT.getClaim("id").asString();
        if (idClaim == null) {
            throw new AuthException(AuthError.AUTH_TOKEN_INVALID);
        }
        return UUID.fromString(idClaim);
    }
}
