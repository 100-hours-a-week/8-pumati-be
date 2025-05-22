package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.AttendanceWeekly;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.attendance.weekly.response.AttendanceWeeklyResponseDTO;
import com.tebutebu.apiserver.repository.AttendanceWeeklyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class AttendanceWeeklyServiceImpl implements AttendanceWeeklyService {

    private final AttendanceWeeklyRepository attendanceWeeklyRepository;

    @Override
    public AttendanceWeeklyResponseDTO getWeeklyStatus(Long memberId) {
        AttendanceWeekly weekly = attendanceWeeklyRepository.findByMemberId(memberId)
                .orElseGet(() -> createEmptyWeekly(memberId));

        LocalDate today = LocalDate.now();
        resetWeeklySummaryIfNewWeek(weekly, today);

        boolean isToday = weekly.getSummary().getOrDefault(
                today.getDayOfWeek().name().toLowerCase(), false
        );

        int streak = computeDisplayStreak(weekly, isToday, today);
        return entityToDTO(weekly, isToday, streak);
    }

    @Override
    public void recordDailyAttendance(Long memberId) {
        LocalDate today = LocalDate.now();

        AttendanceWeekly weekly = attendanceWeeklyRepository.findByMemberId(memberId)
                .orElseGet(() -> createEmptyWeekly(memberId));

        resetWeeklySummaryIfNewWeek(weekly, today);

        LocalDate lastDate = weekly.getLastCheckedDate();
        if (lastDate != null && lastDate.isEqual(today.minusDays(1))) {
            weekly.incrementStreak();
        } else {
            weekly.resetStreak();
            weekly.incrementStreak();
        }

        Map<String, Boolean> summary = weekly.getSummary();
        summary.put(today.getDayOfWeek().name().toLowerCase(), true);
        weekly.updateSummary(summary);
        weekly.updateLastCheckedDate(today);

        attendanceWeeklyRepository.save(weekly);
    }

    private void resetWeeklySummaryIfNewWeek(AttendanceWeekly weekly, LocalDate today) {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate lastDate = weekly.getLastCheckedDate();
        if (lastDate == null || lastDate.isBefore(monday)) {
            Map<String, Boolean> newSummary = new LinkedHashMap<>();
            for (DayOfWeek dow : DayOfWeek.values()) {
                newSummary.put(dow.name().toLowerCase(), false);
            }
            weekly.updateSummary(newSummary);
        }
    }

    private AttendanceWeekly createEmptyWeekly(Long memberId) {
        Map<String, Boolean> summary = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            summary.put(dow.name().toLowerCase(), false);
        }
        return AttendanceWeekly.builder()
                .member(Member.builder().id(memberId).build())
                .lastCheckedDate(null)
                .streak(0)
                .summary(summary)
                .build();
    }

    private int computeDisplayStreak(AttendanceWeekly weekly, boolean isToday, LocalDate today) {
        LocalDate lastDate = weekly.getLastCheckedDate();
        if (lastDate != null && (lastDate.isEqual(today) || lastDate.isEqual(today.minusDays(1)))) {
            return weekly.getStreak();
        }
        return isToday ? 1 : 0;
    }

}
