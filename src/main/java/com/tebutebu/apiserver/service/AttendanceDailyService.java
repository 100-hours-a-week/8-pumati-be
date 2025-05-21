package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.AttendanceDaily;
import com.tebutebu.apiserver.dto.attendance.daily.response.AttendanceDailyResponseDTO;
import com.tebutebu.apiserver.dto.fortune.response.DevLuckDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface AttendanceDailyService {

    AttendanceDailyResponseDTO register(Long memberId);

    boolean existsToday(Long memberId);

    AttendanceDaily dtoToEntity(AttendanceDailyResponseDTO dto);

    default AttendanceDailyResponseDTO entityToDTO(AttendanceDaily attendanceDaily) {
        return AttendanceDailyResponseDTO.builder()
                .id(attendanceDaily.getId())
                .devLuck(DevLuckDTO.builder()
                        .overall(attendanceDaily.getDevLuckOverall())
                        .build())
                .checkedAt(attendanceDaily.getCheckedAt())
                .build();
    }

}
