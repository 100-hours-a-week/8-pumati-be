package com.tebutebu.apiserver.dto.attendance.daily.response;

import com.tebutebu.apiserver.dto.ai.fortune.response.DevLuckDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceDailyResponseDTO {

    private Long id;

    private DevLuckDTO devLuck;

    private LocalDateTime checkedAt;

}
