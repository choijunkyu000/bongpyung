// src/main/java/com/che/bongpyung/config/SecurityConfig.java
package com.che.bongpyung.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

@Configuration
@RequiredArgsConstructor
@Profile("!local")  // ★ local 아닐 때만 이 설정 사용 (dev/prod)
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var logoutGetMatcher = new RegexRequestMatcher("^/logout$", "GET");

        http
                .sessionManagement(sm -> sm
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired")
                        .expiredSessionStrategy(new CustomSessionExpiredStrategy()))
                .sessionManagement(sm -> sm.invalidSessionUrl("/login?invalid"))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/", "/home", "/inout/**").authenticated()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("userId")
                        .passwordParameter("password")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .permitAll())

                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/admin/api/**"))
                .logout(l -> l
                        .logoutRequestMatcher(logoutGetMatcher)
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .authenticationProvider(authenticationProvider);

        return http.build();
    }
}
