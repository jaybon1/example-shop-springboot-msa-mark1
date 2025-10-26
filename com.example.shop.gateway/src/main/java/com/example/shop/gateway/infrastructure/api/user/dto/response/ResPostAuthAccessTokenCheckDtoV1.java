package com.example.shop.gateway.infrastructure.api.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ResPostAuthAccessTokenCheckDtoV1 {

    private final UUID userId;
    private final boolean valid;
    private final long remainingSeconds;
}
