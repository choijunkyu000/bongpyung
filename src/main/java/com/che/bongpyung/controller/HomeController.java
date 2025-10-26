// src/main/java/com/che/bongpyung/controller/HomeController.java
package com.che.bongpyung.controller;

import com.che.bongpyung.domain.OfficeSite;
import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.OfficeSiteRepository;
import com.che.bongpyung.persitence.UserRepository;
import com.che.bongpyung.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final OfficeSiteRepository siteRepo;
    private final UserRepository userRepo;
    private final AttendanceService attendanceService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        // ✅ 비로그인 → 로그인 페이지로
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        // ✅ 관리자면 관리자 대시보드로
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) {
            return "redirect:/auth/admin/dashboard";
        }

        OfficeSite site = siteRepo.findFirstByActiveTrue().orElse(null);
        model.addAttribute("site", site);
        return "home";
    }

    @GetMapping("/api/site/active")
    @ResponseBody
    public Map<String, Object> activeSite() {
        return siteRepo.findFirstByActiveTrue()
                .<Map<String,Object>>map(s -> Map.of(
                        "ok", true,
                        "lat", s.getLat(),
                        "lng", s.getLng(),
                        "radiusM", s.getRadiusM()))
                .orElseGet(() -> Map.of(
                        "ok", false,
                        "message", "활성화된 근무지가 없습니다."));
    }

    @PostMapping("/api/attendance/check-in")
    @ResponseBody
    public Map<String, Object> checkIn(@RequestParam double lat,
                                       @RequestParam double lng,
                                       Authentication auth) {
        try {
            if (auth == null) {
                return Map.of("ok", false, "message", "인증 정보가 없습니다.");
            }
            User me = userRepo.findByUserIdAndEnabledTrue(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
            var r = attendanceService.checkIn(me, lat, lng);
            return Map.of("ok", true, "checkInAt", r.getCheckInAt());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Map.of("ok", false, "message", e.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "message", "서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/api/attendance/check-out")
    @ResponseBody
    public Map<String, Object> checkOut(@RequestParam double lat,
                                        @RequestParam double lng,
                                        Authentication auth) {
        try {
            if (auth == null) {
                return Map.of("ok", false, "message", "인증 정보가 없습니다.");
            }
            User me = userRepo.findByUserIdAndEnabledTrue(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
            var r = attendanceService.checkOut(me, lat, lng);
            return Map.of("ok", true, "checkOutAt", r.getCheckOutAt());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Map.of("ok", false, "message", e.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "message", "서버 오류가 발생했습니다.");
        }
    }
}
