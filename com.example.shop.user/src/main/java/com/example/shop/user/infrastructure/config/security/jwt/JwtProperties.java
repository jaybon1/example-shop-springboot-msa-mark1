package com.example.shop.user.infrastructure.config.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

    private final String secret;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;
    private final String accessHeaderName;
    private final String headerPrefix;
    private final String accessSubject;
    private final String refreshSubject;

    public JwtProperties(
            @Value("${shop.security.jwt.secret:sweetsalt}") String secret,
            @Value("${shop.security.jwt.access-expiration-millis:1800000}") long accessExpirationMillis,
            @Value("${shop.security.jwt.refresh-expiration-millis:15552000000}") long refreshExpirationMillis,
            @Value("${shop.security.jwt.access-header-name:Authorization}") String accessHeaderName,
            @Value("${shop.security.jwt.header-prefix:Bearer }") String headerPrefix,
            @Value("${shop.security.jwt.access-subject:accessJwt}") String accessSubject,
            @Value("${shop.security.jwt.refresh-subject:refreshJwt}") String refreshSubject
    ) {
        this.secret = secret;
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
        this.accessHeaderName = accessHeaderName;
        this.headerPrefix = headerPrefix;
        this.accessSubject = accessSubject;
        this.refreshSubject = refreshSubject;
    }
}
