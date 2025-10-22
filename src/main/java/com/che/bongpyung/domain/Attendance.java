package com.che.bongpyung.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK(web.users.id) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 근무일 (KST 기준) */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    // --- 출근 정보 ---
    // 컬럼 타입이 timestamptz가 되도록 columnDefinition을 권장(기존 DB는 수동 변경 필요)
    @Column(name = "check_in_at"/*, columnDefinition = "timestamp with time zone"*/)
    private OffsetDateTime checkInAt;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_in_inside_fence")
    private Boolean checkInInsideFence;

    // --- 퇴근 정보 ---
    @Column(name = "check_out_at"/*, columnDefinition = "timestamp with time zone"*/)
    private OffsetDateTime checkOutAt;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "check_out_inside_fence")
    private Boolean checkOutInsideFence;

    @Column(name = "note", length = 200)
    private String note;

    // 앱/DB 모두에서 안전하게 기록되도록 created/updated도 OffsetDateTime 사용
    @Column(name = "created_at"/*, columnDefinition = "timestamp with time zone"*/)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at"/*, columnDefinition = "timestamp with time zone"*/)
    private OffsetDateTime updatedAt;

    // --- 자동 타임스탬프 (KST) ---
    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(KST);
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now(KST);
    }
}
