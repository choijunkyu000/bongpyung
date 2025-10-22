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

    @GetMapping("/admin")
    public String adminHome() {
        return "admin/dashboard"; // templates/admin/dashboard.html
    }

    @GetMapping("/admin/attendance")
    public String adminAttendance() {
        return "admin/attendance"; // templates/admin/attendance.html
    }

    @GetMapping("/admin/user_list")
    public String adminuserList() {
        return "admin/user_list"; // templates/admin/users.html
    }

    @GetMapping("/admin/user_detail")
    public String adminUserDetail() {
        return "admin/user_detail"; // templates/admin/users.html
    }
}
