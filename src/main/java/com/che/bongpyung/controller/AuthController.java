// src/main/java/com/che/bongpyung/controller/AuthController.java
package com.che.bongpyung.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ✅ 충돌 원인 제거: "/", "/home" 매핑 삭제
    // @GetMapping({"/", "/home"})
    // public String home(Authentication auth) { ... }

    @GetMapping("/admin/dashboard")
    public String adminHome() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/attendance")
    public String adminAttendance() {
        return "admin/attendance";
    }

    @GetMapping("/admin/user_list")
    public String adminuserList() {
        return "admin/user_list";
    }

    @GetMapping("/admin/user_detail")
    public String adminUserDetail() {
        return "admin/user_detail";
    }

    @GetMapping("/admin/attendance_list")
    public String adminUserAttendanceList() {
        return "admin/user_attendance_list";
    }

    @GetMapping("/admin/attendance_detail")
    public String adminUserAttendanceDetail() {
        return "admin/user_attendance_detail";
    }

    @GetMapping("/admin/users/new")
    public String viewCreateUserPage() {
        return "admin/user";
    }
}
