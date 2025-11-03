package com.che.bongpyung.domain.dto;

import java.time.LocalDateTime;

public interface AttendanceRecordDTO {
    String getUserName();
    LocalDateTime getCheckInAt();
    LocalDateTime getCheckOutAt();
    Integer getWorkHours();
    String getWorkMinutes();
}