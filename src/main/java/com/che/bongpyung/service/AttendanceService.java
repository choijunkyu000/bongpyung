package com.che.bongpyung.service;

import com.che.bongpyung.domain.Attendance;
import com.che.bongpyung.domain.OfficeSite;
import com.che.bongpyung.domain.User;
import com.che.bongpyung.persitence.AttendanceRepository;
import com.che.bongpyung.persitence.OfficeSiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository; // web.attendance
    private final OfficeSiteRepository officeSiteRepository; // 활성 근무지

    private static final double EARTH_RADIUS_M = 6371000.0;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // ===== 시간 유틸(KST) =====
    private LocalDate todayKst() { return LocalDate.now(KST); }
    private OffsetDateTime nowKst() { return OffsetDateTime.now(KST); }

    // ===== 지오펜스/거리 =====
    private OfficeSite activeSiteOrThrow() {
        return officeSiteRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("활성화된 근무지가 없습니다."));
    }

    /** 하버사인 거리(m) */
    private double distanceM(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng/2) * Math.sin(dLng/2);
        return 2 * EARTH_RADIUS_M * Math.asin(Math.sqrt(a));
    }

    private void assertInsideFence(double lat, double lng, OfficeSite site, String when) {
        double dist = distanceM(lat, lng, site.getLat(), site.getLng());
        if (dist > site.getRadiusM()) {
            throw new IllegalArgumentException(when + " 위치가 근무지 반경을 벗어났습니다. (" + (int) dist + "m)");
        }
    }

    // ===== 핵심 로직 =====

    /** 출근: 오늘 행을 INSERT (이미 있으면 예외) */
    @Transactional
    public Attendance checkIn(User user, double lat, double lng) {
        OfficeSite site = activeSiteOrThrow();
        assertInsideFence(lat, lng, site, "출근");

        LocalDate workDate = todayKst();
        attendanceRepository.findByUserIdAndWorkDate(user.getId(), workDate)
                .ifPresent(a -> { throw new IllegalStateException("이미 출근 기록이 있습니다."); });

        OffsetDateTime now = nowKst();
        Attendance a = new Attendance();
        a.setUserId(user.getId());
        a.setWorkDate(workDate);
        a.setCheckInAt(now);
        a.setCheckInLat(lat);
        a.setCheckInLng(lng);
        a.setCheckInInsideFence(true);
        a.setCreatedAt(now);
        a.setUpdatedAt(now);

        try {
            return attendanceRepository.save(a);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("이미 출근 처리되었습니다. 잠시 후 새로고침 해주세요.", e);
        }
    }

    /** 퇴근: 오늘 행 UPDATE (없으면 예외, 이미 퇴근했으면 예외) */
    @Transactional
    public Attendance checkOut(User user, double lat, double lng) {
        OfficeSite site = activeSiteOrThrow();
        assertInsideFence(lat, lng, site, "퇴근");

        LocalDate workDate = todayKst();
        Attendance a = attendanceRepository
                .findByUserIdAndWorkDate(user.getId(), workDate)
                .orElseThrow(() -> new IllegalStateException("출근 기록이 없습니다."));

        if (a.getCheckOutAt() != null) {
            throw new IllegalStateException("이미 퇴근 처리되었습니다.");
        }

        OffsetDateTime now = nowKst();
        a.setCheckOutAt(now);
        a.setCheckOutLat(lat);
        a.setCheckOutLng(lng);
        a.setCheckOutInsideFence(true);
        a.setUpdatedAt(now);

        return attendanceRepository.save(a);
    }

    // ===== 조회 헬퍼(선택) =====
    @Transactional(readOnly = true)
    public Status todayStatus(User user) {
        LocalDate workDate = todayKst();
        Attendance a = attendanceRepository.findByUserIdAndWorkDate(user.getId(), workDate).orElse(null);
        if (a == null) return Status.NOT_CHECKED_IN;
        if (a.getCheckOutAt() == null) return Status.WORKING;
        return Status.CHECKED_OUT;
    }

    public enum Status { NOT_CHECKED_IN, WORKING, CHECKED_OUT }
}
