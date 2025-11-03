package com.che.bongpyung.persitence;

import com.che.bongpyung.domain.Attendance;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserIdAndWorkDate(Long id, LocalDate workDate);

    Collection<Attendance> findByUserIdInAndWorkDate(List<Long> ids, LocalDate target);

    Collection<Attendance> findByUserIdAndWorkDateBetween(Long userId, LocalDate startDate, LocalDate endDate);


    // ✅ 유저별 최신 1건 (work_date DESC, check_in_at DESC)
    Optional<Attendance> findFirstByUserIdOrderByWorkDateDescCheckInAtDesc(Long userId);

    @Query("""
        select a
        from Attendance a
        join fetch a.user u
        where (:name is null or :name = '' or u.displayName like concat('%', :name, '%'))
          and a.checkInAt >= :start
          and a.checkInAt < :end
        order by u.displayName asc, a.checkInAt asc
    """)
    List<Attendance> searchByUserAndPeriod(
            @Param("name") String name,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

}
