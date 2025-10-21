// src/main/java/com/che/bongpyung/domain/Attendance.java
package com.che.bongpyung.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "attendance",
        schema = "web",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_attendance_user_date", columnNames = {"user_id", "work_date"})
        },
        indexes = {
                @Index(name = "idx_attendance_user_id", columnList = "user_id"),
                @Index(name = "idx_attendance_work_date", columnList = "work_date")
        }
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK(web.users.id) — 간단히 id만 보유 (필요 시 @ManyToOne로 교체 가능) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 근무일 (KST 기준) */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // --- 출근 정보 ---
    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_in_inside_fence")
    private Boolean checkInInsideFence;

    // --- 퇴근 정보 ---
    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "check_out_inside_fence")
    private Boolean checkOutInsideFence;

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt; // DB default now() 있지만, 앱에서도 보강

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- 자동 타임스탬프 (KST) ---
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
