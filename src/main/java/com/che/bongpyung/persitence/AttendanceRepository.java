package com.che.bongpyung.persitence;

import com.che.bongpyung.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Object> findByUserIdAndWorkDate(Long id, LocalDate workDate);
}
