package com.che.bongpyung.config;

import com.che.bongpyung.persitence.UserRepository;
import com.che.bongpyung.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;

    public CustomLoginSuccessHandler(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String userId = authentication.getName();
        var user = userRepo.findByUserIdAndEnabledTrue(userId).orElse(null);
        if (user != null && Boolean.TRUE.equals(user.getFirstLogin())) {
            response.sendRedirect("/account/password");
            return;
        }

        // ✅ 권한별 대시보드 이동
        if (user.getRole() == com.che.bongpyung.domain.User.Role.ADMIN) {
            response.sendRedirect("/admin/dashboard");
        } else {
            response.sendRedirect("/home");
        }
    }
}
