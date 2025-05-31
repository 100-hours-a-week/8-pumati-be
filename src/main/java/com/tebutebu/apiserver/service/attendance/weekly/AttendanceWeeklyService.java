package com.tebutebu.apiserver.service.attendance.weekly;

import com.tebutebu.apiserver.domain.AttendanceWeekly;
import com.tebutebu.apiserver.dto.attendance.weekly.response.AttendanceWeeklyResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.LinkedHashMap;
import java.util.Map;

@Transactional
public interface AttendanceWeeklyService {

    @Transactional(readOnly = true)
    AttendanceWeeklyResponseDTO getWeeklyStatus(Long memberId);

    void recordDailyAttendance(Long memberId);

    default AttendanceWeeklyResponseDTO entityToDTO(AttendanceWeekly attendanceWeekly, boolean isToday, int streak) {
        Map<String, Boolean> orderedSummary = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            orderedSummary.put(
                    dow.name().toLowerCase(),
                    attendanceWeekly.getSummary().getOrDefault(dow.name().toLowerCase(), false)
            );
        }
        return AttendanceWeeklyResponseDTO.builder()
                .today(isToday)
                .weekly(orderedSummary)
                .streak(streak)
                .lastCheckedDate(attendanceWeekly.getLastCheckedDate())
                .build();
    }

}
