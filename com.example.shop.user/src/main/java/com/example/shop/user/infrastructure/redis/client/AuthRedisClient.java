package com.example.shop.user.infrastructure.redis.client;

import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthRedisClient {

    private static final String USER_DENY_PREFIX = "auth:deny:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public void denyBy(String userId, Long jwtValidator) {
        String key = USER_DENY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, jwtValidator.toString());
        stringRedisTemplate.expire(key, Duration.ofMillis(jwtProperties.getAccessExpirationMillis()));
    }

    public Long getBy(String userId) {
        String key = USER_DENY_PREFIX + userId;
        String stringValue = stringRedisTemplate.opsForValue().get(key);
        return stringValue != null ? Long.valueOf(stringValue) : null;
    }

    public void cancelDenyBy(String userId) {
        String key = USER_DENY_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }

}
