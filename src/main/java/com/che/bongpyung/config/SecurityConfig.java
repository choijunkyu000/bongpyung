// src/main/java/com/che/bongpyung/config/SecurityConfig.java
package com.che.bongpyung.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?invalid")
                )
                .authorizeHttpRequests(auth -> auth
                        // 공개 리소스
                        .requestMatchers(
                                "/login", "/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                        ).permitAll()
                        // 뷰 페이지
                        .requestMatchers("/", "/home", "/inout/**").authenticated()
                        // 그 외도 인증만 필요(denyAll 대신)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // API는 POST라 CSRF 예외가 필요함
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                )
                .authenticationProvider(authenticationProvider);

        return http.build();
    }
}
