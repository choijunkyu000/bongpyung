// src/main/java/com/che/bongpyung/config/SecurityConfigLocal.java
package com.che.bongpyung.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("local")
public class SecurityConfigLocal {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(h -> h.frameOptions(f -> f.disable())) // H2 등 테스트시 편의
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // ★ 전부 허용
        return http.build();
    }
}
