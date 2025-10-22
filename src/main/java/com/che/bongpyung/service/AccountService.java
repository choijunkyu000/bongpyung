// src/main/java/com/che/bongpyung/service/AccountService.java
package com.che.bongpyung.service;

import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // 간단 강도 규칙: 8자 이상, 영문 + 숫자 포함
    private static final Pattern SIMPLE_STRENGTH =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,64}$");

    public void changePassword(String userId, String currentRaw, String newRaw) {
        User user = userRepo.findByUserIdAndEnabledTrue(userId)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));

        if (currentRaw == null || !passwordEncoder.matches(currentRaw, user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        if (newRaw == null || !SIMPLE_STRENGTH.matcher(newRaw).matches()) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이며 영문과 숫자를 포함해야 합니다.");
        }
        if (passwordEncoder.matches(newRaw, user.getPasswordHash())) {
            throw new IllegalArgumentException("이전과 다른 비밀번호를 입력하세요.");
        }

        user.setPasswordHash(passwordEncoder.encode(newRaw));
        user.setFirstLogin(false); // 초기화/최초 로그인 강제 변경 플래그 해제
        user.setUpdatedAt(LocalDateTime.now());
        userRepo.save(user);
    }
}
