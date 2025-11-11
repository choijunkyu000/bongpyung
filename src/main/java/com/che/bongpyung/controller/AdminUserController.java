package com.che.bongpyung.controller;

import com.che.bongpyung.domain.Attendance;
import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.AttendanceRepository;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminUserController {

    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ==== Pages ====

    @GetMapping("/users")
    public String usersPage() {
        // 템플릿: admin/user_list.html (프론트가 /admin/api/users/list 호출)
        return "admin/user_list";
    }

    @GetMapping("/user_detail/{id}")
    public String userDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("userId", id);
        return "admin/user_detail"; // -> templates/admin/user_detail.html
    }

    /** 목록 조회 + 검색 + 선택일 근태 합쳐서 내려주기 */
    @ResponseBody
    @GetMapping("/api/users/list")
    public Map<String, Object> listUsers(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date // 사용 안 해도 OK
    ) {
        // 1) 사용자 조회 + 간단 필터
        List<User> users = userRepo.findAll().stream()
                .filter(u -> userId == null || userId.isBlank() || u.getUserId().toLowerCase().contains(userId.toLowerCase()))
                .filter(u -> displayName == null || displayName.isBlank()
                        || (u.getDisplayName() != null && u.getDisplayName().contains(displayName)))
                .sorted(Comparator.comparing(User::getId))
                .toList();

        // 2) 각 사용자별 최신 출퇴근 1건 조회
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : users) {
            Optional<Attendance> recentOpt = attendanceRepo.findFirstByUserIdOrderByWorkDateDescCheckInAtDesc(u.getId());
            Attendance recent = recentOpt.orElse(null);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", u.getId());
            row.put("userId", u.getUserId());
            row.put("displayName", u.getDisplayName());
            row.put("role", u.getRole().name());
            row.put("useYn", u.isUseYn());
            row.put("createdAt", u.getCreatedAt()); // 프론트에서 yyyy-MM-dd 포맷
            row.put("updatedAt", u.getUpdatedAt());
            row.put("lastCheckIn", (recent != null) ? recent.getCheckInAt() : null);
            row.put("lastCheckOut", (recent != null) ? recent.getCheckOutAt() : null);

            rows.add(row);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("ok", true);
        res.put("rows", rows);
        return res;
    }

    /** 상세 조회 + 선택일 근태 */
    @ResponseBody
    @GetMapping("/api/users/{id}")
    public Map<String, Object> getUser(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate target = (date != null) ? date : LocalDate.now();
        User user = userRepo.findById(id).orElseThrow();

        Map<String, Object> resultUser = new LinkedHashMap<>();
        resultUser.put("id", user.getId());
        resultUser.put("userId", user.getUserId());
        resultUser.put("displayName", user.getDisplayName());
        resultUser.put("role", user.getRole().name());
        resultUser.put("useYn", user.isUseYn());

        Optional<Attendance> attOpt = attendanceRepo.findByUserIdAndWorkDate(user.getId(), target);

        Map<String, Object> resultAtt = new LinkedHashMap<>();
        if (attOpt.isPresent()) {
            Attendance a = attOpt.get();
            resultAtt.put("workDate", a.getWorkDate());
            resultAtt.put("checkInAt", a.getCheckInAt());
            resultAtt.put("checkOutAt", a.getCheckOutAt());
        } else {
            resultAtt.put("workDate", target);
            resultAtt.put("checkInAt", null);
            resultAtt.put("checkOutAt", null);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", true);
        response.put("user", resultUser);
        response.put("attendance", resultAtt);

        return response;
    }


    /**
     * ✅ 유저별 출근 요약 리스트
     */
    @GetMapping("/api/attendance/summary")
    @ResponseBody
    public Map<String, Object> listAttendanceSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        // ✅ 1. 관리자 제외, 비활성 사용자 제외
        List<User> users = userRepo.findAll().stream()
                .filter(User::isUseYn)                               // 활성 사용자만
                .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole().name())) // 관리자 제외
                .toList();

        List<Map<String, Object>> rows = new ArrayList<>();

        for (User u : users) {
            List<Attendance> attList = (List<Attendance>) attendanceRepo.findByUserIdAndWorkDateBetween(u.getId(), start, end);

            long totalDays = attList.stream().filter(a -> a.getCheckInAt() != null).count();
            long totalMinutes = attList.stream()
                    .filter(a -> a.getCheckInAt() != null && a.getCheckOutAt() != null)
                    .mapToLong(a -> ChronoUnit.MINUTES.between(a.getCheckInAt(), a.getCheckOutAt()))
                    .sum();

            long hours = totalMinutes / 60;
            long minutes = totalMinutes % 60;

            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());
            row.put("userId", u.getUserId());
            row.put("displayName", u.getDisplayName());
            row.put("role", u.getRole() != null ? u.getRole().name() : "-");
            row.put("useYn", u.isUseYn());
            row.put("totalDays", totalDays);
            row.put("totalWorkTime", String.format("%02d:%02d", hours, minutes));
            rows.add(row);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("rows", rows);
        return res;
    }

    /**
     * ✅ 특정 유저 상세 출퇴근 내역
     */
    @GetMapping("/api/attendance/summaryDetail/{userId}")
    @ResponseBody
    public Map<String, Object> userAttendanceDetail(
            @PathVariable Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        List<Attendance> list = attendanceRepo.findByUserIdAndWorkDateBetween(userId, start, end)
                .stream()
                .sorted(Comparator.comparing(Attendance::getWorkDate))
                .collect(Collectors.toList());

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Attendance a : list) {
            Map<String, Object> r = new HashMap<>();
            r.put("workDate", a.getWorkDate());
            r.put("checkInAt", a.getCheckInAt());
            r.put("checkOutAt", a.getCheckOutAt());

            String workTime = "-";
            if (a.getCheckInAt() != null && a.getCheckOutAt() != null) {
                long minutes = ChronoUnit.MINUTES.between(a.getCheckInAt(), a.getCheckOutAt());
                workTime = String.format("%02d:%02d", minutes / 60, minutes % 60);
            }
            r.put("workTime", workTime);
            rows.add(r);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("rows", rows);
        return res;
    }

}
