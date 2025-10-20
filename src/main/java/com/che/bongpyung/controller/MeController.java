// src/main/java/com/che/bongpyung/controller/MeController.java
package com.che.bongpyung.controller;

import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/me")
public class MeController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @GetMapping("/password")
    public String changeForm() { return "me/password"; }

    @PostMapping("/password")
    public String change(@AuthenticationPrincipal User login,
                         @RequestParam String current,
                         @RequestParam String next,
                         Model model) {
        try {
            var u = userRepo.findByUsernameAndEnabledTrue(login.getUsername()).orElseThrow();
            if (!encoder.matches(current, u.getPasswordHash())) {
                model.addAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
                return "me/password";
            }
            u.setPasswordHash(encoder.encode(next));
            u.setFirstLogin(false);
            u.setUpdatedAt(LocalDateTime.now());
            userRepo.save(u);
            model.addAttribute("ok", true);
        } catch (Exception e) {
            model.addAttribute("error", "변경 중 오류가 발생했습니다.");
        }
        return "me/password";
    }
}
