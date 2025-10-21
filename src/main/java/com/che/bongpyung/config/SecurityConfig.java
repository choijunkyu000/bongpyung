// src/main/java/com/che/bongpyung/config/SecurityConfig.java
package com.che.bongpyung.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
// ↑ org.springframework.security.web.util.matcher.RequestMatcher 구현체

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // GET /logout 허용용 매처 (정규식은 ^…$ 로 고정 매칭)
        var logoutGetMatcher = new RegexRequestMatcher("^/logout$", "GET");

        http
                .sessionManagement(sm -> sm
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                        .expiredSessionStrategy(new CustomSessionExpiredStrategy())
                )
                .sessionManagement(sm -> sm.invalidSessionUrl("/login?invalid"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/", "/home", "/inout/**").authenticated()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler) // 쓰는 중이면 유지
                        //.defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // API만 CSRF 예외
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/admin/api/**"))

                // ★ GET /logout 허용 (주소창/링크로 접근 가능)
                .logout(l -> l
                        .logoutRequestMatcher(logoutGetMatcher)   // GET /logout 허용
                        // .logoutUrl("/logout")                   // (선택) POST도 함께 허용하고 싶으면 추가
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .authenticationProvider(authenticationProvider);

        return http.build();
    }
}
