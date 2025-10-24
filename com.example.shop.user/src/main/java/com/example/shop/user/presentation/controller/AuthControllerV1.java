package com.example.shop.user.presentation.controller;

import com.example.shop.global.presentation.dto.ApiDto;
import com.example.shop.user.presentation.dto.request.ReqPostAuthAccessTokenCheckDtoV1;
import com.example.shop.user.presentation.dto.response.ResPostAuthAccessTokenCheckDtoV1;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthControllerV1 {

    @PostMapping("/access-token-check")
    public ResponseEntity<ApiDto<ResPostAuthAccessTokenCheckDtoV1>> checkAccessToken(
            @RequestBody @Valid ReqPostAuthAccessTokenCheckDtoV1 reqDto
    ) {
        long remainingSeconds = 300L;

        ResPostAuthAccessTokenCheckDtoV1 responseBody = ResPostAuthAccessTokenCheckDtoV1.builder()
                .valid(true)
                .remainingSeconds(remainingSeconds)
                .build();

        return ResponseEntity.ok(
                ApiDto.<ResPostAuthAccessTokenCheckDtoV1>builder()
                        .message("액세스 토큰 검증이 완료되었습니다.")
                        .data(responseBody)
                        .build()
        );
    }
}
