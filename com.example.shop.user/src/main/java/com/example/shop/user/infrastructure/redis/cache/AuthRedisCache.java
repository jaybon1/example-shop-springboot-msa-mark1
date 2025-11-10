package com.example.shop.user.infrastructure.redis.cache;

import com.example.shop.user.application.cache.AuthCache;
import com.example.shop.user.infrastructure.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthRedisCache implements AuthCache {

    private static final String AUTH_DENY_PREFIX = "auth:deny:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    @Override
    public void denyBy(String userId, Long jwtValidator) {
        String key = AUTH_DENY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, jwtValidator.toString());
        stringRedisTemplate.expire(key, Duration.ofMillis(jwtProperties.getAccessExpirationMillis()));
    }

    @Override
    public Long getBy(String userId) {
        String key = AUTH_DENY_PREFIX + userId;
        String stringValue = stringRedisTemplate.opsForValue().get(key);
        return stringValue != null ? Long.valueOf(stringValue) : null;
    }

    @Override
    public void cancelDenyBy(String userId) {
        String key = AUTH_DENY_PREFIX + userId;
        stringRedisTemplate.delete(key);
    }

}
