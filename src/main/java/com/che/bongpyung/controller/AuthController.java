// src/main/java/com/che/bongpyung/controller/AuthController.java
package com.che.bongpyung.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication auth) {
        // 인증 안 된 경우 로그인 페이지로
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        // 관리자면 관리자 대시보드로
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return "redirect:/admin";
        }
        // 일반 유저는 기존 홈
        return "home";
    }

    // 관리자 대시보드(메뉴: 출결관리/사용자관리 진입점)
    @GetMapping("/admin")
    public String adminDashboard() {
        return "admin/dashboard"; // templates/admin/dashboard.html
    }
}
