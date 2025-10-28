package com.example.shop.payment.infrastructure.redis.client;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthRedisClient {

    private final StringRedisTemplate stringRedisTemplate;

}
