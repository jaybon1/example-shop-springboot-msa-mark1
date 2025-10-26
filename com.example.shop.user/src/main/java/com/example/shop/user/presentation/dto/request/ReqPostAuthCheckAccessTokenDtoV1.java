package com.example.shop.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPostAuthCheckAccessTokenDtoV1 {

    @NotBlank(message = "accessJwt 를 입력해주세요.")
    private String accessJwt;
}
