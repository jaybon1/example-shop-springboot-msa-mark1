package com.example.shop.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPostAuthRefreshDtoV1 {

    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshJwt;
}
