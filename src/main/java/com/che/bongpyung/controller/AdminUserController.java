package com.che.bongpyung.controller;

import com.che.bongpyung.domain.Attendance;
import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.AttendanceRepository;
import com.che.bongpyung.persitence.UserRepository;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/users/{id}")
    public String userDetailPage(@PathVariable Long id, Model model) {
        // 템플릿: admin/user_detail.html (프론트가 /admin/api/users/{id} 호출)
        model.addAttribute("userId", id);
        return "admin/user_detail";
    }

    // ==== APIs (조회만; 생성/수정/삭제/리셋은 기존 API 사용) ====

    /** 목록 조회 + 검색 + 선택일 근태 합쳐서 내려주기 */
    @ResponseBody
    @GetMapping("/api/users/list")
    public Map<String, Object> listUsers(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String displayName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // --- 기본 유저 목록 필터링 (DB 전체 조회 후 필터)
        List<User> users = userRepo.findAll().stream()
                .filter(u -> userId == null || userId.isBlank() || u.getUserId().toLowerCase().contains(userId.toLowerCase()))
                .filter(u -> displayName == null || displayName.isBlank() ||
                        (u.getDisplayName() != null && u.getDisplayName().contains(displayName)))
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());

        // --- Attendance 조건 조회 (date 조건이 있을 때만)
        Map<Long, Attendance> attMap = new HashMap<>();
        if (targetDate != null) {
            List<Long> ids = users.stream().map(User::getId).toList();
            List<Attendance> atts = (List<Attendance>) attendanceRepo.findByUserIdInAndWorkDate(ids, targetDate);
            for (Attendance a : atts) {
                attMap.put(a.getUserId(), a);
            }
        }

        // --- 응답 변환
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : users) {
            Attendance a = attMap.get(u.getId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("userId", u.getUserId());
            map.put("displayName", u.getDisplayName());
            map.put("role", u.getRole().name());
            map.put("useYn", u.isUseYn());
            map.put("lastCheckIn", a != null && a.getCheckInAt() != null ? a.getCheckInAt().format(df) : null);
            map.put("lastCheckOut", a != null && a.getCheckOutAt() != null ? a.getCheckOutAt().format(df) : null);
            map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
            map.put("updatedAt", u.getUpdatedAt() != null ? u.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);
            rows.add(map);
        }

        return Map.of("ok", true, "rows", rows);
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

        List<User> users = userRepo.findAll();
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
    @GetMapping("/api/attendance/users/{userId}")
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
