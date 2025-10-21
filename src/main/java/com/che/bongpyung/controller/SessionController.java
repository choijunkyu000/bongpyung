// src/main/java/com/che/bongpyung/controller/SessionController.java
package com.che.bongpyung.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class SessionController {

    @GetMapping("/api/session/status")
    public Map<String, Object> checkSession(HttpSession session) {
        boolean active = session != null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null;
        return Map.of("active", active);
    }
}
