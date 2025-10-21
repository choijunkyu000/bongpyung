package com.che.bongpyung.service;

import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    /** 스프링 시큐리티 인증용 */
    public UserDetails loadUserByUserId(String username) throws UsernameNotFoundException {
        var u = userRepo.findByUserIdAndEnabledTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        var role = "ROLE_" + u.getRole().name(); // ADMIN/USER → ROLE_ADMIN / ROLE_USER
        return org.springframework.security.core.userdetails.User.withUsername(u.getUserId())
                .password(u.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .disabled(!u.getEnabled())
                .build();
    }

    /** 최초 관리자 계정이 없으면 생성 */
    public void ensureAdmin() {
        String id = System.getenv().getOrDefault("APP_ADMIN_USERNAME", "admin");
        String raw = System.getenv().getOrDefault("APP_ADMIN_PASSWORD", "admin1234");

        userRepo.findByUserIdAndEnabledTrue(id).ifPresentOrElse(
                it -> {}, // 이미 있음
                () -> {
                    var admin = User.builder()
                            .userId("ADMIN")
                            .passwordHash(passwordEncoder.encode(raw))
                            .displayName("Administrator")
                            .role(User.Role.ADMIN)
                            .enabled(true)
                            .firstLogin(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    userRepo.save(admin);
                }
        );
    }

    /** 로그인 사용자의 비밀번호 변경 */
    public void changePassword(String username, String currentRaw, String nextRaw) {
        var u = userRepo.findByUserIdAndEnabledTrue(username).orElseThrow();
        if (!passwordEncoder.matches(currentRaw, u.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        u.setPasswordHash(passwordEncoder.encode(nextRaw));
        u.setFirstLogin(false);
        u.setUpdatedAt(LocalDateTime.now());
        userRepo.save(u);
    }

    @Transactional
    public void updateLoginDeviceInfo(String userId, String userAgent, String ipAddress) {
        User user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setLastLoginDevice(userAgent);
        user.setLastLoginIp(ipAddress);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
    }
}
