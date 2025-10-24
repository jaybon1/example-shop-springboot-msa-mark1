package com.example.shop.user.infrastructure.config.security;

import com.example.shop.user.infrastructure.config.security.auth.CustomAccessDeniedHandler;
import com.example.shop.user.infrastructure.config.security.auth.CustomAuthenticationEntryPoint;
import com.example.shop.user.infrastructure.config.security.auth.JwtAuthorizationFilter;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Nullable
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        http.logout(logout -> logout.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.exceptionHandling(handler -> handler
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));

        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> {
            if ("dev".equalsIgnoreCase(activeProfile)) {
                authorize.requestMatchers("/h2-console/**").permitAll();
            } else {
                authorize.requestMatchers("/h2-console/**").hasRole("ADMIN");
            }

            authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
            authorize.requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/assets/**",
                    "/springdoc/**",
                    "/favicon.ico",
                    "/docs/**",
                    "/swagger-ui/**",
                    "/actuator/health",
                    "/actuator/info"
            ).permitAll();
            authorize.requestMatchers("/v1/auth/**").permitAll();
            authorize.anyRequest().authenticated();
        });

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration configuration = new CorsConfiguration();
            configuration.setAllowedHeaders(Collections.singletonList("*"));
            configuration.setAllowedMethods(Collections.singletonList("*"));
            configuration.setAllowedOriginPatterns(List.of(
                    "http://127.0.0.1:[*]",
                    "http://localhost:[*]"
            ));
            configuration.setAllowCredentials(true);
            return configuration;
        };
    }
}
