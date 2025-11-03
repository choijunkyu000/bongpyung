// src/main/java/com/che/bongpyung/controller/AdminAttendanceController.java
package com.che.bongpyung.controller;

import com.che.bongpyung.domain.dto.AttendanceDetailResponse;
import com.che.bongpyung.domain.dto.AttendanceSummaryDTO;
import com.che.bongpyung.service.AttendanceAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/attendance")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AttendanceAdminService service;

    @PostMapping("/summary")
    public List<Map<String, Object>> getSummary(@RequestBody Map<String, String> req) {
        String name = req.getOrDefault("name", "");
        LocalDate startDate = LocalDate.parse(req.getOrDefault("startDate",
                LocalDate.now().withDayOfMonth(1).toString()));
        LocalDate endDate = LocalDate.parse(req.getOrDefault("endDate",
                LocalDate.now().toString()));

        return service.getWorkSummary(name, startDate, endDate);
    }

    @PostMapping("/detail")
    public List<Map<String, Object>> getUserDetail(@RequestBody Map<String, String> req) {
        String name = req.get("name");
        LocalDate startDate = LocalDate.parse(req.getOrDefault("startDate",
                LocalDate.now().withDayOfMonth(1).toString()));
        LocalDate endDate = LocalDate.parse(req.getOrDefault("endDate",
                LocalDate.now().toString()));

        return service.getUserDetails(name, startDate, endDate);
    }
}
