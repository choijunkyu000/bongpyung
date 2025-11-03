// src/main/java/com/che/bongpyung/service/AttendanceAdminService.java
package com.che.bongpyung.service;

import com.che.bongpyung.domain.Attendance;
import com.che.bongpyung.persitence.AttendanceAdminRepository;
import com.che.bongpyung.persitence.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceAdminService {

    private final AttendanceAdminRepository repo;

    private final AttendanceRepository attendanceRepository;

    public List<Map<String, Object>> getWorkSummary(String name, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Attendance> list = attendanceRepository.searchByUserAndPeriod(name, start, end);

        // 직원별 근무시간 합산
        Map<String, List<Attendance>> grouped = list.stream()
                .filter(a -> a.getUser() != null)
                .filter(a -> !"ADMIN".equalsIgnoreCase(String.valueOf(a.getUser().getRole())))
                .collect(Collectors.groupingBy(a -> a.getUser().getDisplayName()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Attendance>> e : grouped.entrySet()) {
            double totalHours = e.getValue().stream()
                    .filter(a -> a.getCheckInAt() != null && a.getCheckOutAt() != null)
                    .mapToDouble(a -> {
                        long seconds = java.time.Duration.between(
                                a.getCheckInAt().toLocalDateTime(),
                                a.getCheckOutAt().toLocalDateTime()
                        ).getSeconds();
                        return seconds / 3600.0;
                    })
                    .sum();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("displayName", e.getKey());
            map.put("workDays", e.getValue().size());
            map.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
            result.add(map);
        }

        // 이름순 정렬
        result.sort(Comparator.comparing(m -> m.get("displayName").toString()));

        return result;
    }

    public List<Map<String, Object>> getUserDetails(String name, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Attendance> list = attendanceRepository.searchByUserAndPeriod(name, start, end);

        return list.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("workDate", a.getWorkDate());
            m.put("checkInAt", a.getCheckInAt());
            m.put("checkOutAt", a.getCheckOutAt());

            if (a.getCheckInAt() != null && a.getCheckOutAt() != null) {
                long seconds = java.time.Duration.between(
                        a.getCheckInAt().toLocalDateTime(),
                        a.getCheckOutAt().toLocalDateTime()
                ).getSeconds();
                m.put("workHours", Math.round((seconds / 3600.0) * 100.0) / 100.0);
            } else {
                m.put("workHours", 0.0);
            }

            return m;
        }).collect(Collectors.toList());
    }
}
