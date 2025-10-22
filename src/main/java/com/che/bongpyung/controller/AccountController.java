// src/main/java/com/che/bongpyung/controller/AccountController.java
package com.che.bongpyung.controller;


import com.che.bongpyung.domain.dto.ChangePasswordForm;
import com.che.bongpyung.service.AccountService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/password")
    public String viewPasswordPage(
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        if (success != null) model.addAttribute("success", success);
        if (error != null) model.addAttribute("error", error);
        return "account/password"; // ← templates/account/password.html
    }

    @PostMapping("/password")
    public String changePassword(@ModelAttribute ChangePasswordForm form,
                                 Authentication auth,
                                 HttpSession session) {
        try {
            if (form.getNewPassword() == null || !form.getNewPassword().equals(form.getConfirmPassword())) {
                throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
            }
            String userId = auth.getName(); // username= userId 로 사용 중
            accountService.changePassword(userId, form.getCurrentPassword(), form.getNewPassword());

            // ✅ 세션 완전 종료
            session.invalidate();
            SecurityContextHolder.clearContext();

            // ✅ 로그인 페이지로 이동 + 메시지 전달
            String msg = URLEncoder.encode("비밀번호가 변경되었습니다. 다시 로그인해주세요.", StandardCharsets.UTF_8);
            return "redirect:/login?changed=1&msg=" + msg;

        } catch (Exception e) {
            String err = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/account/password?error=" + err;
        }
    }

    private String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception ignored) { return "오류가%20발생했습니다."; }
    }
}
