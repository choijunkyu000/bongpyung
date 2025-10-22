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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminUserController {

    private final UserRepository userRepo;
    private final AttendanceRepository attendanceRepo;

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
        LocalDate target = (date != null) ? date : LocalDate.now();

        List<User> users = userRepo.findAll().stream()
                .filter(u -> userId == null || userId.isBlank() ||
                        (u.getUserId() != null && u.getUserId().toLowerCase().contains(userId.toLowerCase())))
                .filter(u -> displayName == null || displayName.isBlank() ||
                        (u.getDisplayName() != null && u.getDisplayName().contains(displayName)))
                .sorted(Comparator.comparing(User::getId))
                .collect(Collectors.toList());

        List<Long> ids = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Long, Attendance> attMap = attendanceRepo.findByUserIdInAndWorkDate(ids, target).stream()
                .collect(Collectors.toMap(Attendance::getUserId, a -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : users) {
            Attendance a = attMap.get(u.getId());

            Map<String, Object> row = new HashMap<>();
            row.put("id", u.getId());                                // null 허용됨
            row.put("userId", u.getUserId());                        // null 허용됨
            row.put("displayName", u.getDisplayName());              // null 허용됨
            row.put("role", (u.getRole() != null) ? u.getRole().name() : null);
            row.put("useYn", Boolean.valueOf(u.isUseYn()));          // boolean → Boolean 박싱
            row.put("lastCheckIn", (a != null) ? a.getCheckInAt() : null);
            row.put("lastCheckOut", (a != null) ? a.getCheckOutAt() : null);

            rows.add(row);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ok", Boolean.TRUE);
        response.put("rows", rows);
        return response;
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

}
