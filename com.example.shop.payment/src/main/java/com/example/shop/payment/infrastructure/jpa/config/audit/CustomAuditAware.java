package com.example.shop.payment.infrastructure.jpa.config.audit;

import com.example.shop.user.infrastructure.security.auth.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomAuditAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return Optional.of("system");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("anonymous");
        }

        if (!authentication.isAuthenticated()) {
            return Optional.of("custom_authentication");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.ofNullable(userDetails.getId())
                    .map(Object::toString)
                    .or(() -> Optional.of("anonymous"));
        }

        return Optional.of("etc");
    }
}
