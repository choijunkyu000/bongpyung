// src/main/java/com/che/bongpyung/controller/AdminUserApi.java
package com.che.bongpyung.controller;

import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserApi {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // 목록
    @GetMapping
    public Map<String, Object> list(@RequestParam(defaultValue = "false") boolean includeInactive) {
        List<User> users = includeInactive
                ? userRepo.findAllByOrderByDisplayNameAscUserIdAsc()
                : userRepo.findAllByUseYnTrueOrderByDisplayNameAscUserIdAsc();
        return Map.of("ok", true, "rows", users.stream().map(u -> Map.of(
                "id", u.getId(),
                "userId", u.getUserId(),
                "displayName", u.getDisplayName(),
                "role", u.getRole().name(),
                "useYn", true,
                "enabled", u.getEnabled()
        )).toList());
    }

    // 등록
    @PostMapping
    public Map<String, Object> create(@RequestBody CreateUserReq req) {
        if (userRepo.existsByUserId(req.userId())) {
            return Map.of("ok", false, "message", "이미 존재하는 아이디입니다.");
        }
        var now = LocalDateTime.now();
        // 임시 비밀번호 정책: firstLogin=true로 강제 변경 흐름 유도
        String tempRaw = (req.tempPassword() != null && !req.tempPassword().isBlank())
                ? req.tempPassword() : "asdf1234";

        var u = User.builder()
                .userId(req.userId())
                .displayName(req.displayName())
                .passwordHash(passwordEncoder.encode(tempRaw))
                .role(User.Role.valueOf(req.role())) // "ADMIN" 또는 "USER"
                .enabled(true)
                .useYn(true)
                .firstLogin(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userRepo.save(u);
        return Map.of("ok", true);
    }

    public record CreateUserReq(String userId, String displayName, String role, String tempPassword) {}

    // 수정
    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody UpdateUserReq req) {
        var u = userRepo.findById(id).orElseThrow();
        if (req.displayName() != null) u.setDisplayName(req.displayName());
        if (req.role() != null) u.setRole(User.Role.valueOf(req.role()));
        if (req.useYn() != null) {
            u.setUseYn(req.useYn());
            if (!req.useYn()) u.setEnabled(false); // 정책: useYn=false면 enabled도 false
        }
        u.setUpdatedAt(LocalDateTime.now());
        userRepo.save(u);
        return Map.of("ok", true);
    }

    public record UpdateUserReq(String displayName, String role, Boolean useYn) {}

    // 삭제(소프트): useYn=false (+ enabled=false)
    @DeleteMapping("/{id}")
    public Map<String, Object> softDelete(@PathVariable Long id) {
        var u = userRepo.findById(id).orElseThrow();
        u.setUseYn(false);
        u.setEnabled(false);
        u.setUpdatedAt(LocalDateTime.now());
        userRepo.save(u);
        return Map.of("ok", true);
    }

    // (선택) 비밀번호 리셋
    @PostMapping("/{id}/reset-password")
    public Map<String, Object> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        var u = userRepo.findById(id).orElseThrow();
        String raw = body.getOrDefault("password", "asdf1234");
        u.setPasswordHash(passwordEncoder.encode(raw));
        u.setFirstLogin(true);
        u.setUpdatedAt(LocalDateTime.now());
        userRepo.save(u);
        return Map.of("ok", true);
    }
}
