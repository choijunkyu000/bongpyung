// src/main/java/com/che/bongpyung/config/CustomSessionExpiredStrategy.java
package com.che.bongpyung.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import java.io.IOException;

public class CustomSessionExpiredStrategy implements SessionInformationExpiredStrategy {
    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException, ServletException {
        HttpServletResponse response = event.getResponse();
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("""
                <script>
                  alert('다른 기기에서 로그인되어 세션이 종료되었습니다.');
                  window.location.href='/login?expired';
                </script>
                """);
        response.getWriter().flush();
    }
}
