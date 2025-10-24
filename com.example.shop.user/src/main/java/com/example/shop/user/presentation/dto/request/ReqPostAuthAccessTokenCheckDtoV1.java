package com.example.shop.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReqPostAuthAccessTokenCheckDtoV1 {

    @NotBlank(message = "accessJwt 를 입력해주세요.")
    private String accessJwt;
}
