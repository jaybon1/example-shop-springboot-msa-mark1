package com.example.shop.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class ReqAuthPostRefreshDtoV1 {

    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshJwt;
}
