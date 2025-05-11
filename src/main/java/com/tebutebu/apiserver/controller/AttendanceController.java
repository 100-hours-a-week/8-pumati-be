package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.attendance.daily.response.AttendanceDailyResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.service.AttendanceDailyService;
import com.tebutebu.apiserver.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/attendances")
public class AttendanceController {

    private final AttendanceDailyService attendanceDailyService;

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

}
