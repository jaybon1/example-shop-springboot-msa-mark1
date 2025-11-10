package com.example.shop.gateway.application.cache;

public interface AuthCache {

    void denyBy(String userId, Long jwtValidator);

    Long getBy(String userId);

    void cancelDenyBy(String userId);
}
