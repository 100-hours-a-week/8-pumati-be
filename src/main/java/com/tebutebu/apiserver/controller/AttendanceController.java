package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.attendance.daily.response.AttendanceDailyResponseDTO;
import com.tebutebu.apiserver.dto.attendance.weekly.response.AttendanceWeeklyResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.service.attendance.daily.AttendanceDailyService;
import com.tebutebu.apiserver.service.attendance.weekly.AttendanceWeeklyService;
import com.tebutebu.apiserver.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceDailyService attendanceDailyService;

    private final AttendanceWeeklyService attendanceWeeklyService;

    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<?> register(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO memberDTO = memberService.get(authorizationHeader);
        AttendanceDailyResponseDTO dto = attendanceDailyService.register(memberDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "attendanceSuccess",
                        "data", dto
                ));
    }

    @GetMapping("")
    public ResponseEntity<?> getWeeklyStatus(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO member = memberService.get(authorizationHeader);
        AttendanceWeeklyResponseDTO dto = attendanceWeeklyService.getWeeklyStatus(member.getId());
        return ResponseEntity.ok(Map.of(
                "message", "getAttendanceSuccess",
                "data", dto
        ));
    }

}
