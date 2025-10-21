package com.che.bongpyung.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", schema = "web")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String userId; // 로그인 ID

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // 암호화된 비밀번호

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName; // 화면 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER; // USER or ADMIN

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "first_login", nullable = false)
    private Boolean firstLogin = true; // 첫 로그인 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Role {
        ADMIN, USER
    }

    @Column(name = "last_login_ip")
    private String lastLoginIp;

    @Column(name = "last_login_device")
    private String lastLoginDevice;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "use_yn")
    private boolean useYn;

}
