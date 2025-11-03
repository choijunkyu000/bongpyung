package com.che.bongpyung.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * local, dev 환경에서는 Spring Security 인증을 비활성화한다.
 * 모든 요청을 permitAll()로 처리하여 Postman 및 프론트 개발 테스트를 쉽게 함.
 */
@Configuration
@Profile({"local", "dev"})
public class SecurityConfigDev {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CSRF 비활성화 (API 테스트 가능)
                .csrf(csrf -> csrf.disable())

                // ✅ 모든 요청 허용
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // ✅ 프레임옵션 해제 (H2 콘솔 사용 시 편의용)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // ✅ 세션 및 로그인 완전 비활성화
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }
}
