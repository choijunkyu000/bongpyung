package com.che.bongpyung.controller;

import com.che.bongpyung.domain.OfficeSite;
import com.che.bongpyung.domain.User; // ✅ 우리의 도메인 User
import com.che.bongpyung.persitence.OfficeSiteRepository;
import com.che.bongpyung.persitence.UserRepository;
import com.che.bongpyung.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final OfficeSiteRepository siteRepo;
    private final UserRepository userRepo;
    private final AttendanceService attendanceService;

    // 테스트용 메인 (모바일 최적화)
    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        OfficeSite site = siteRepo.findFirstByActiveTrue().orElse(null);
        model.addAttribute("site", site); // 없을 수도 있으니 null 허용
        // 필요 시 로그인 사용자 정보도 내려주려면 아래 주석 해제
        // if (auth != null) model.addAttribute("username", auth.getName());
        return "home";
    }

    // 활성 사이트 좌표 조회 (JS에서 사용)
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

    // 출근
    @PostMapping("/api/attendance/check-in")
    @ResponseBody
    public Map<String, Object> checkIn(@RequestParam double lat,
                                       @RequestParam double lng,
                                       Authentication auth) {
        try {
            if (auth == null) {
                return Map.of("ok", false, "message", "인증 정보가 없습니다.");
            }
            User me = userRepo.findByUsernameAndEnabledTrue(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
            var r = attendanceService.checkIn(me, lat, lng, LocalDateTime.now());
            return Map.of("ok", true, "checkInAt", r.getCheckInAt());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Map.of("ok", false, "message", e.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "message", "서버 오류가 발생했습니다.");
        }
    }

    // 퇴근
    @PostMapping("/api/attendance/check-out")
    @ResponseBody
    public Map<String, Object> checkOut(@RequestParam double lat,
                                        @RequestParam double lng,
                                        Authentication auth) {
        try {
            if (auth == null) {
                return Map.of("ok", false, "message", "인증 정보가 없습니다.");
            }
            User me = userRepo.findByUsernameAndEnabledTrue(auth.getName())
                    .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다."));
            var r = attendanceService.checkOut(me, lat, lng, LocalDateTime.now());
            return Map.of("ok", true, "checkOutAt", r.getCheckOutAt());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Map.of("ok", false, "message", e.getMessage());
        } catch (Exception e) {
            return Map.of("ok", false, "message", "서버 오류가 발생했습니다.");
        }
    }
}
