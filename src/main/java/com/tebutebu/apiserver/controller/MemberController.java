package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    public ResponseEntity<?> get(@PathVariable("memberId") Long memberId) {
        MemberResponseDTO dto = memberService.get(memberId);
        return ResponseEntity.ok(Map.of("message", "success", "data", dto));
    }

    @PostMapping("/social")
    public ResponseEntity<?> registerOAuthUser(@Valid MemberOAuthSignupRequestDTO dto) {
        long memberId = memberService.registerOAuthUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "registerSuccess", "data", Map.of("id", memberId)));
    }

}
