package com.che.bongpyung.service;

import com.che.bongpyung.domain.AttendanceLog;
import com.che.bongpyung.domain.User;
import com.che.bongpyung.domain.OfficeSite;
import com.che.bongpyung.persitence.AttendanceRepository;
import com.che.bongpyung.persitence.OfficeSiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final OfficeSiteRepository officeSiteRepository;

    private static final double EARTH_RADIUS_M = 6371000.0;

    /** 하버사인 거리(m) */
    private double distanceM(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng/2) * Math.sin(dLng/2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }

    private OfficeSite activeSiteOrThrow() {
        return officeSiteRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("활성화된 근무지가 없습니다."));
    }

    private LocalDateTime startOfToday() {
        return LocalDate.now().atStartOfDay();
    }

    private LocalDateTime endOfToday() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    /** 출근 체크 */
    @Transactional
    public AttendanceLog checkIn(User user, double lat, double lng, LocalDateTime now) {
        OfficeSite site = activeSiteOrThrow();

        double dist = distanceM(lat, lng, site.getLat(), site.getLng());
        if (dist > site.getRadiusM()) {
            throw new IllegalArgumentException("출근 위치가 근무지 반경을 벗어났습니다. (" + (int) dist + "m)");
        }

        var todayStart = startOfToday();
        var todayEnd   = endOfToday();

        // 이미 오늘 출근했는지
        var existed = attendanceRepository
                .findTopByUserAndCheckInAtBetweenOrderByCheckInAtDesc(user, todayStart, todayEnd);
        if (existed.isPresent()) {
            throw new IllegalStateException("이미 출근 기록이 있습니다.");
        }

        AttendanceLog log = AttendanceLog.builder()
                .user(user)
                .checkInAt(now)
                .checkInLat(lat)
                .checkInLng(lng)
                .build();

        return attendanceRepository.save(log);
    }

    /** 퇴근 체크 */
    @Transactional
    public AttendanceLog checkOut(User user, double lat, double lng, LocalDateTime now) {
        OfficeSite site = activeSiteOrThrow();

        var todayStart = startOfToday();
        var todayEnd   = endOfToday();

        // 오늘 출근했고 아직 퇴근 안한 건
        var log = attendanceRepository
                .findTopByUserAndCheckInAtBetweenAndCheckOutAtIsNullOrderByCheckInAtDesc(user, todayStart, todayEnd)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));

        double dist = distanceM(lat, lng, site.getLat(), site.getLng());
        if (dist > site.getRadiusM()) {
            throw new IllegalArgumentException("퇴근 위치가 근무지 반경을 벗어났습니다. (" + (int) dist + "m)");
        }

        log.setCheckOutAt(now);
        log.setCheckOutLat(lat);
        log.setCheckOutLng(lng);
        return attendanceRepository.save(log);
    }
}
