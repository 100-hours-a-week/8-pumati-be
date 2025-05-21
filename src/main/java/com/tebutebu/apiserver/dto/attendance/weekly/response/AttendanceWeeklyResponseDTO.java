package com.tebutebu.apiserver.dto.attendance.weekly.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceWeeklyResponseDTO {

    private boolean today;

    private Map<String, Boolean> weekly;

    private int streak;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastCheckedDate;

}
