package com.example.shop.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostAuthLoginDtoV1 {

    private final String accessJwt;
    private final String refreshJwt;

    public static ResPostAuthLoginDtoV1 of(String accessJwt, String refreshJwt) {
        return ResPostAuthLoginDtoV1.builder()
                .accessJwt(accessJwt)
                .refreshJwt(refreshJwt)
                .build();
    }

}
