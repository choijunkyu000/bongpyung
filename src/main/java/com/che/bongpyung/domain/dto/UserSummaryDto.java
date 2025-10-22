package com.che.bongpyung.domain.dto;

import java.time.LocalDateTime;

public record UserSummaryDto(
        Long id,
        String userId,
        String displayName,
        String role,
        boolean useYn,
        LocalDateTime lastCheckIn,
        LocalDateTime lastCheckOut
) {}
