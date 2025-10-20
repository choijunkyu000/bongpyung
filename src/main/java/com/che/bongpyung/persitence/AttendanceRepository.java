package com.che.bongpyung.persitence;

import com.che.bongpyung.domain.AttendanceLog;
import com.che.bongpyung.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceLog, Long> {

    // 오늘 이미 출근했는지 확인
    Optional<AttendanceLog> findTopByUserAndCheckInAtBetweenOrderByCheckInAtDesc(
            User user, LocalDateTime start, LocalDateTime end
    );

    // 오늘 출근은 했고 아직 퇴근 전인 한 건 찾기
    Optional<AttendanceLog> findTopByUserAndCheckInAtBetweenAndCheckOutAtIsNullOrderByCheckInAtDesc(
            User user, LocalDateTime start, LocalDateTime end
    );
}
