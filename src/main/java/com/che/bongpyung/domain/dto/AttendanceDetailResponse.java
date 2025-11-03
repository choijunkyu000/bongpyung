package com.che.bongpyung.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class AttendanceDetailResponse {
    private String userName;
    private List<AttendanceRecordDTO> records;
}