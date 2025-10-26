package com.example.shop.user.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.user.application.service.AuthServiceV1;
import com.example.shop.user.infrastructure.security.auth.CustomUserDetails;
import com.example.shop.user.presentation.dto.request.*;
import com.example.shop.user.presentation.dto.response.ResPostAuthCheckAccessTokenDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthLoginDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthRefreshDtoV1;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

    private final AuthServiceV1 authServiceV1;

    @PostMapping("/register")
    public ResponseEntity<ApiDto<Object>> register(
            @RequestBody @Valid ReqPostAuthRegisterDtoV1 reqDto
    ) {
        authServiceV1.register(reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("회원가입이 완료되었습니다.")
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiDto<ResPostAuthLoginDtoV1>> login(
            @RequestBody @Valid ReqPostAuthLoginDtoV1 reqDto
    ) {
        ResPostAuthLoginDtoV1 responseBody = authServiceV1.login(reqDto);

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthLoginDtoV1>builder()
                        .message("로그인이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiDto<ResPostAuthRefreshDtoV1>> refresh(
            @RequestBody @Valid ReqAuthPostRefreshDtoV1 reqDto
    ) {
        ResPostAuthRefreshDtoV1 responseBody = authServiceV1.refresh(reqDto);

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthRefreshDtoV1>builder()
                        .message("토큰이 갱신되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/check-access-token")
    public ResponseEntity<ApiDto<ResPostAuthCheckAccessTokenDtoV1>> checkAccessToken(
            @RequestBody @Valid ReqPostAuthCheckAccessTokenDtoV1 reqDto
    ) {
        ResPostAuthCheckAccessTokenDtoV1 responseBody = authServiceV1.checkAccessToken(reqDto);

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthCheckAccessTokenDtoV1>builder()
                        .message("액세스 토큰 검증이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }

    @PostMapping("/invalidate-before-token")
    public ResponseEntity<ApiDto<Object>> invalidateBeforeToken(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestBody @Valid ReqPostAuthInvalidateBeforeTokenDtoV1 reqDto
    ) {
        UUID authUserId = customUserDetails.getId();
        List<String> authRoleList = customUserDetails.getRoleList();
        authServiceV1.invalidateBeforeToken(authUserId, authRoleList, reqDto);
        return ResponseEntity.ok(
                ApiDto.builder()
                        .message("모든 기기에서 로그아웃 되었습니다.")
                        .build()
        );
    }

}
