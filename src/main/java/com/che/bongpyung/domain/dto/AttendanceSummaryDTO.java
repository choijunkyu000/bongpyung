package com.che.bongpyung.domain.dto;

public interface AttendanceSummaryDTO {
    Long getUserId();
    String getName();
    Integer getWorkDays();
    Integer getTotalHours();
    String getTotalMinutes();
}