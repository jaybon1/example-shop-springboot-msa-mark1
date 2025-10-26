package com.example.shop.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostAuthRefreshDtoV1 {

    private final String accessJwt;
    private final String refreshJwt;

    public static ResPostAuthRefreshDtoV1 of(String accessJwt, String refreshJwt) {
        return ResPostAuthRefreshDtoV1.builder()
                .accessJwt(accessJwt)
                .refreshJwt(refreshJwt)
                .build();
    }

}
