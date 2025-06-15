package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.member.request.MemberOAuthSignupRequestDTO;
import com.tebutebu.apiserver.dto.member.request.MemberUpdateRequestDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberSignupResponseDTO;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.project.ProjectService;
import com.tebutebu.apiserver.service.team.TeamService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final ProjectService projectService;

    private final TeamService teamService;

    @GetMapping("/{memberId}")
    public ResponseEntity<?> get(@PathVariable("memberId") Long memberId) {
        MemberResponseDTO dto = memberService.get(memberId);
        return ResponseEntity.ok(Map.of("message", "getMemberSuccess", "data", dto));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getByAuthHeader(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO dto = memberService.get(authorizationHeader);
        return ResponseEntity.ok(Map.of("message", "getMemberSuccess", "data", dto));
    }

    @GetMapping("/teams/projects/existence")
    public ResponseEntity<?> checkProjectExistence(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO member = memberService.get(authorizationHeader);
        Long teamId = member.getTeamId();

        if (teamId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "onlyTraineeAllowed"));
        }

        boolean exists = projectService.existsByTeamId(teamId);
        return ResponseEntity.ok(Map.of("message", "checkProjectExistenceSuccess", "data", Map.of("exists", exists)));
    }

    @PostMapping("/social")
    public ResponseEntity<?> registerOAuthUser(
            @Valid @RequestBody MemberOAuthSignupRequestDTO dto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        MemberSignupResponseDTO data = memberService.registerOAuthUser(dto, request, response);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "signupSuccess",
                        "data", data
                ));
    }

    @PutMapping("/me")
    public ResponseEntity<?> modify(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody MemberUpdateRequestDTO dto
    ) {
        memberService.modify(authorizationHeader, dto);
        return ResponseEntity.ok(Map.of("message", "modifyMemberSuccess"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> delete(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        memberService.delete(authorizationHeader, request, response);
        return ResponseEntity.ok(Map.of("message", "memberDeleted"));
    }

}
