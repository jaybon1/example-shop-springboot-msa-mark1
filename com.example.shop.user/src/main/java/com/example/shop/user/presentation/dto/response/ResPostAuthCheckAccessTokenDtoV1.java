package com.example.shop.user.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResPostAuthCheckAccessTokenDtoV1 {

    private final String userId;
    private final boolean valid;
    private final long remainingSeconds;

}
