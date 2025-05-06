package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import com.tebutebu.apiserver.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
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
        return ResponseEntity.ok(Map.of("message", "getMemberSuccess", "data", dto));
    }

    @PostMapping("/social")
    public ResponseEntity<?> registerOAuthUser(
            @Valid MemberOAuthSignupRequestDTO dto,
            HttpServletResponse response
    ) {
        MemberSignupResponseDTO data = memberService.registerOAuthUser(dto, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "signupSuccess",
                        "data", data
                ));
    }

    @PutMapping("/me")
    public ResponseEntity<?> modify(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid MemberUpdateRequestDTO dto
    ) {
        memberService.modify(authorizationHeader, dto);
        return ResponseEntity.ok(Map.of("message", "modifyMemberSuccess"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> delete(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        memberService.delete(authorizationHeader);
        return ResponseEntity.ok(Map.of("message", "memberDeleted"));
    }

}
