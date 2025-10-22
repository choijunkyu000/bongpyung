// src/main/java/com/che/bongpyung/controller/AccountController.java
package com.che.bongpyung.controller;


import com.che.bongpyung.domain.dto.ChangePasswordForm;
import com.che.bongpyung.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                                 Model model) {
        try {
            if (form.getNewPassword() == null ||
                    !form.getNewPassword().equals(form.getConfirmPassword())) {
                throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
            }
            String userId = auth.getName(); // SecurityBeans에서 userId를 username으로 사용 중
            accountService.changePassword(userId, form.getCurrentPassword(), form.getNewPassword());
            // 성공 메시지와 함께 같은 페이지로
            return "redirect:/account/password?success=비밀번호가%20변경되었습니다.";
        } catch (Exception e) {
            // 실패 메시지와 함께 같은 페이지로
            return "redirect:/account/password?error=" + urlEncode(e.getMessage());
        }
    }

    private String urlEncode(String s) {
        try { return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8); }
        catch (Exception ignored) { return "오류가%20발생했습니다."; }
    }
}
