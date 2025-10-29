package com.example.shop.order.infrastructure.security.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String username;
    private String nickname;
    private String email;
    private List<String> roleList;

    public static CustomUserDetails of(DecodedJWT decodedJwt) {
        List<String> roles = decodedJwt.getClaim("roleList").asList(String.class);
        return CustomUserDetails.builder()
                .id(UUID.fromString(decodedJwt.getClaim("id").asString()))
                .username(decodedJwt.getClaim("username").asString())
                .nickname(decodedJwt.getClaim("nickname").asString())
                .email(decodedJwt.getClaim("email").asString())
                .roleList(roles == null ? List.of() : List.copyOf(roles))
                .build();
    }

    public List<String> getRoleList() {
        return roleList == null ? List.of() : Collections.unmodifiableList(roleList);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoleList()
                .stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
