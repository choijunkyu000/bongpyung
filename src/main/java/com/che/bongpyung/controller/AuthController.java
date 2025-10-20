// src/main/java/com/che/bongpyung/controller/AuthController.java
package com.che.bongpyung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping({"/", "/home"})
    public String home() {
        return "home"; // ← templates/home.html
    }
}
