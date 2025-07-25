package com.tebutebu.apiserver.service.attendance.daily;

import com.tebutebu.apiserver.domain.AttendanceDaily;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.dto.attendance.daily.response.AttendanceDailyResponseDTO;
import com.tebutebu.apiserver.dto.ai.fortune.request.AiFortuneGenerateRequestDTO;
import com.tebutebu.apiserver.dto.ai.fortune.response.DevLuckDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.repository.AttendanceDailyRepository;
import com.tebutebu.apiserver.service.ai.fortune.AiFortuneService;
import com.tebutebu.apiserver.service.attendance.weekly.AttendanceWeeklyService;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class AttendanceDailyServiceImpl implements AttendanceDailyService {

    private final AttendanceDailyRepository attendanceDailyRepository;

    private final MemberService memberService;

    private final AiFortuneService aiFortuneService;

    private final AttendanceWeeklyService attendanceWeeklyService;

    private final TeamService teamService;

    @Value("${pumati.attendance.daily.count}")
    private long attendanceDailyCount;

    @Value("${fortune.service.error-message}")
    private String fortuneServiceErrorMessage;

    @Override
    public AttendanceDailyResponseDTO register(Long memberId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        AttendanceDaily existing = attendanceDailyRepository
                .findByMemberIdAndCheckedAtBetween(memberId, start, end)
                .stream()
                .findFirst()
                .orElse(null);

        MemberResponseDTO memberDTO = memberService.get(memberId);
        if (memberDTO == null) {
            throw new BusinessException(BusinessErrorCode.MEMBER_NOT_FOUND);
        }

        if (existing != null) {
            if (existing.getDevLuckOverall().equals(fortuneServiceErrorMessage)) {
                DevLuckDTO devLuckDTO = requestDevLuck(memberDTO);
                existing.updateDevLuck(devLuckDTO.getOverall());
                AttendanceDaily updated = attendanceDailyRepository.save(existing);
                return entityToDTO(updated);
            }
            return entityToDTO(existing);
        }

        DevLuckDTO devLuckDTO = requestDevLuck(memberDTO);

        AttendanceDaily attendance = AttendanceDaily.builder()
                .member(Member.builder().id(memberId).build())
                .checkedAt(LocalDateTime.now())
                .devLuckOverall(devLuckDTO.getOverall())
                .build();

        AttendanceDaily saved = attendanceDailyRepository.save(attendance);
        attendanceWeeklyService.recordDailyAttendance(memberId);
        teamService.incrementGivedPumatiBy(memberDTO.getTeamId(), attendanceDailyCount);

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

    private DevLuckDTO requestDevLuck(MemberResponseDTO memberDTO) {
        AiFortuneGenerateRequestDTO requestDTO = AiFortuneGenerateRequestDTO.builder()
                .nickname(memberDTO.getNickname())
                .course(memberDTO.getCourse())
                .date(LocalDate.now())
                .build();

        return aiFortuneService.generateDevLuck(requestDTO);
    }

}
