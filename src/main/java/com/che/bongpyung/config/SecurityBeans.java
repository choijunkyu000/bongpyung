// src/main/java/com/che/bongpyung/config/SecurityBeans.java
package com.che.bongpyung.config;

import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class SecurityBeans {

    private final UserRepository userRepo; // ✅ UserService 말고 Repository만 의존

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ UserDetailsService를 Repository로 직접 구현(어댑터 삭제)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepo.findByUserIdAndEnabledTrue(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUserId())
                        .password(u.getPasswordHash())
                        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))
                        .disabled(!Boolean.TRUE.equals(u.getEnabled()))
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("user not found: " + username));
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            BCryptPasswordEncoder encoder,
            UserDetailsService uds
    ) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
