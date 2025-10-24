package com.example.shop.user.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class UserSocial {

    private final UUID id;
    private final Provider provider;
    private final String providerId;
    private final String nickname;
    private final String email;
    private final Instant createdAt;
    private final String createdBy;
    private final Instant updatedAt;
    private final String updatedBy;
    private final Instant deletedAt;
    private final String deletedBy;

    UserSocialBuilder toBuilder() {
        return UserSocial.builder()
                .id(id)
                .provider(provider)
                .providerId(providerId)
                .nickname(nickname)
                .email(email)
                .createdAt(createdAt)
                .createdBy(createdBy)
                .updatedAt(updatedAt)
                .updatedBy(updatedBy)
                .deletedAt(deletedAt)
                .deletedBy(deletedBy);
    }

    public enum Provider {
        KAKAO,
        GOOGLE,
        NAVER
    }
}
