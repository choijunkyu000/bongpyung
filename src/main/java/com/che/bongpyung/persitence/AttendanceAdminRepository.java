// src/main/java/com/che/bongpyung/persitence/AttendanceAdminRepository.java
package com.che.bongpyung.persitence;

import com.che.bongpyung.domain.Attendance;
import com.che.bongpyung.domain.dto.AttendanceRecordDTO;
import com.che.bongpyung.domain.dto.AttendanceSummaryDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceAdminRepository extends JpaRepository<com.che.bongpyung.domain.Attendance, Long> {

    @Query("""
    select a
    from Attendance a
    join fetch a.user u
    where (:name is null or :name = '' or u.displayName like concat('%', :name, '%'))
      and a.checkInAt >= :start
      and a.checkInAt < :end
      and u.role != 'ADMIN'
    order by u.displayName asc, a.checkInAt asc
""")
    List<Attendance> searchByUserAndPeriod(
            @Param("name") String name,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


    @Query(value = """
        SELECT 
            u.display_name AS userName,
            a.check_in_at AS checkInAt,
            a.check_out_at AS checkOutAt,
            ROUND(EXTRACT(EPOCH FROM (a.check_out_at - a.check_in_at)) / 3600) AS workHours,
            LPAD(FLOOR(MOD(EXTRACT(EPOCH FROM (a.check_out_at - a.check_in_at)) / 60, 60))::text, 2, '0') AS workMinutes
        FROM web.attendance a
        JOIN web.users u ON a.user_id = u.id
        WHERE a.user_id = :userId
          AND a.check_in_at >= CAST(:startDate AS date)
          AND a.check_in_at < CAST(:endDate AS date) + INTERVAL '1 day'
          AND a.check_out_at IS NOT NULL
        ORDER BY a.check_in_at
        """, nativeQuery = true)
    List<AttendanceRecordDTO> findDetail(@Param("userId") Long userId,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
}
