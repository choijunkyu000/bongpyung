// src/main/java/com/che/bongpyung/config/DataInitializer.java
package com.che.bongpyung.config;

import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminId  = System.getenv().getOrDefault("APP_ADMIN_USERNAME", "admin");
        String adminRaw = System.getenv().getOrDefault("APP_ADMIN_PASSWORD", "admin1234");

        userRepo.findByUserIdAndEnabledTrue(adminId).ifPresentOrElse(u -> {
            // bcrypt 아니면 교정
            if (!looksLikeBcrypt(u.getPasswordHash())) {
                u.setPasswordHash(passwordEncoder.encode(adminRaw));
                u.setUpdatedAt(LocalDateTime.now());
                userRepo.save(u);
                log.info("🔐 admin 비밀번호를 기본값으로 재설정했습니다.");
            } else {
                log.info("✅ 관리자 계정 이미 존재함: " + adminId);
            }
        }, () -> {
            var admin = User.builder()
                    .userId(adminId)
                    .passwordHash(passwordEncoder.encode(adminRaw))
                    .displayName("Administrator")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .firstLogin(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepo.save(admin);
            log.info("✅ 관리자 계정 생성: " + adminId);
        });
    }

    private boolean looksLikeBcrypt(String s) {
        return s != null && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }
}
