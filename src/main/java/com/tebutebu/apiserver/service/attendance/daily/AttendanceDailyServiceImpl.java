package com.tebutebu.apiserver.service.attendance.daily;

import com.tebutebu.apiserver.domain.AttendanceDaily;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.attendance.daily.response.AttendanceDailyResponseDTO;
import com.tebutebu.apiserver.dto.ai.fortune.request.AiFortuneGenerateRequestDTO;
import com.tebutebu.apiserver.dto.ai.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.repository.AttendanceDailyRepository;
import com.tebutebu.apiserver.service.ai.fortune.AiFortuneService;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.attendance.weekly.AttendanceWeeklyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class AttendanceDailyServiceImpl implements AttendanceDailyService {

    private final AttendanceDailyRepository attendanceDailyRepository;

    private final MemberService memberService;

    private final AiFortuneService aiFortuneService;

    private final AttendanceWeeklyService attendanceWeeklyService;

    @Override
    public AttendanceDailyResponseDTO register(Long memberId) {
        if (existsToday(memberId)) {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);

            AttendanceDaily existing = attendanceDailyRepository
                    .findByMemberIdAndCheckedAtBetween(memberId, start, end)
                    .stream()
                    .findFirst()
                    .orElseThrow();

            return entityToDTO(existing);
        }

        MemberResponseDTO memberDTO = memberService.get(memberId);
        if (memberDTO == null) {
            throw new NoSuchElementException("memberNotFound");
        }

        AiFortuneGenerateRequestDTO requestDTO = AiFortuneGenerateRequestDTO.builder()
                .nickname(memberDTO.getNickname())
                .course(memberDTO.getCourse())
                .date(LocalDate.now())
                .build();

        DevLuckDTO devLuckDTO = aiFortuneService.generateDevLuck(requestDTO);

        AttendanceDaily attendance = AttendanceDaily.builder()
                .member(Member.builder().id(memberId).build())
                .checkedAt(LocalDateTime.now())
                .devLuckOverall(devLuckDTO.getOverall())
                .build();

        AttendanceDaily saved = attendanceDailyRepository.save(attendance);
        attendanceWeeklyService.recordDailyAttendance(memberId);
        return entityToDTO(saved);
    }

    @Override
    public boolean existsToday(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        return attendanceDailyRepository.existsByMemberIdAndCheckedAtBetween(
                memberId, start, end
        );
    }

    @Override
    public AttendanceDaily dtoToEntity(AttendanceDailyResponseDTO dto) {
        return AttendanceDaily.builder()
                .id(dto.getId())
                .devLuckOverall(dto.getDevLuck().getOverall())
                .checkedAt(dto.getCheckedAt())
                .build();
    }

}
