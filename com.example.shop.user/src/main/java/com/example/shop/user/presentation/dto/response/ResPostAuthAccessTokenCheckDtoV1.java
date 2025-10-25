package com.example.shop.user.presentation.dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResPostAuthAccessTokenCheckDtoV1 {

    private final UUID userId;
    private final boolean valid;
    private final long remainingSeconds;
}
