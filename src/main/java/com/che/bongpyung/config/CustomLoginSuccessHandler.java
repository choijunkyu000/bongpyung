package com.che.bongpyung.config;

import com.che.bongpyung.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public CustomLoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        // ✅ 로그인 기기 정보 저장
        userService.updateLoginDeviceInfo(username, userAgent, ipAddress);

        response.sendRedirect("/inout"); // 로그인 성공 시 이동
    }
}
