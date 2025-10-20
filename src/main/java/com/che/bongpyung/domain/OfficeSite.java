package com.che.bongpyung.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "office_site", schema = "web")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficeSite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 근무지 명칭

    @Column(nullable = false)
    private Double lat;  // 위도

    @Column(nullable = false)
    private Double lng;  // 경도

    @Column(name = "radius_m", nullable = false)
    private Integer radiusM = 100; // 반경 (기본 100m)

    @Column(nullable = false)
    private Boolean active = true; // 활성화 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
