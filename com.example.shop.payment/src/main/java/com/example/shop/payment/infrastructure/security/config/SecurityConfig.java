package com.example.shop.payment.infrastructure.security.config;

import com.example.shop.payment.infrastructure.security.auth.CustomAccessDeniedHandler;
import com.example.shop.payment.infrastructure.security.auth.CustomAuthenticationEntryPoint;

import java.util.Collections;
import java.util.List;

import com.example.shop.payment.infrastructure.security.filter.JwtAuthorizationFilter;
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
    @Value("${spring.cloud.config.profile}")
    private String activeProfile;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable());
        httpSecurity.formLogin(form -> form.disable());
        httpSecurity.httpBasic(basic -> basic.disable());
        httpSecurity.logout(logout -> logout.disable());
        httpSecurity.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        httpSecurity.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        httpSecurity.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        httpSecurity.exceptionHandling(handler -> handler
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler));

        httpSecurity.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);

        httpSecurity.authorizeHttpRequests(authorize -> {
            if ("dev".equalsIgnoreCase(activeProfile)) {
                authorize.requestMatchers("/h2/**").permitAll();
            } else {
                authorize.requestMatchers("/h2/**").hasRole("ADMIN");
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
            authorize.requestMatchers("/v1/payments/**").permitAll();
            authorize.requestMatchers("/actuator/**").hasRole("ADMIN");
            authorize.anyRequest().authenticated();
        });

        return httpSecurity.build();
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
