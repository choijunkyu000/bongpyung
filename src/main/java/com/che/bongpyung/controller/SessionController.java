// src/main/java/com/che/bongpyung/controller/SessionController.java
package com.che.bongpyung.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    @GetMapping("/status")
    public Map<String, Object> status(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        HttpSession s = request.getSession(false);

        Integer maxInactiveSec = (s != null) ? s.getMaxInactiveInterval() : null;
        Long createdAt = (s != null) ? s.getCreationTime() : null;
        Long lastAccessedAt = (s != null) ? s.getLastAccessedTime() : null;

        Long nowMs = Instant.now().toEpochMilli();
        Long remainingSec = (s != null && maxInactiveSec != null && lastAccessedAt != null)
                ? Math.max(0, (lastAccessedAt + (maxInactiveSec * 1000L) - nowMs) / 1000L)
                : null;

        return Map.of(
                "ok", true,
                "active", authenticated,                 // 로그인/세션 유효 여부
                "username", authenticated ? auth.getName() : null,
                "sessionId", (s != null) ? s.getId() : null,
                "createdAt", createdAt,                  // epoch ms
                "lastAccessedAt", lastAccessedAt,        // epoch ms
                "maxInactiveIntervalSec", maxInactiveSec,
                "remainingSec", remainingSec
        );
    }
}
