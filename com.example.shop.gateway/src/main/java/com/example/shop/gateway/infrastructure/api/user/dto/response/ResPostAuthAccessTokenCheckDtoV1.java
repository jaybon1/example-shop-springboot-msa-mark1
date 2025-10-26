package com.example.shop.gateway.infrastructure.api.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResPostAuthAccessTokenCheckDtoV1 {

    private UUID userId;
    private boolean valid;
    private long remainingSeconds;

}
