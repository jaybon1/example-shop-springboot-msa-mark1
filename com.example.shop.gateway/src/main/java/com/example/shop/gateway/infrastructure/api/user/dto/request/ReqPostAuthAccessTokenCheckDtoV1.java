package com.example.shop.gateway.infrastructure.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReqPostAuthAccessTokenCheckDtoV1 {

    @NotBlank(message = "accessJwt 를 입력해주세요.")
    private String accessJwt;

}
