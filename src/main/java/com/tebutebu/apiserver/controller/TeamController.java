package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.service.MemberService;
import com.tebutebu.apiserver.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    private final MemberService memberService;

    @PostMapping("")
    public ResponseEntity<?> register(@Valid @RequestBody TeamCreateRequestDTO dto) {
        long teamId = teamService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "registerSuccess", "data", Map.of("id", teamId)));
    }

    @GetMapping("")
    public ResponseEntity<?> getAllTeams() {
        List<TeamListResponseDTO> list = teamService.getAllTeams();
        return ResponseEntity.ok(Map.of(
                "message", "getTeamsSuccess",
                "data", list
        ));
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<?> getMembersByTeam(@PathVariable Long teamId) {
        List<MemberResponseDTO> members = memberService.getMembersByTeamId(teamId);
        return ResponseEntity.ok(Map.of(
                "message", "getTeamMembersSuccess",
                "data", members
        ));
    }

    @PatchMapping("/{teamId}/gived-pumati")
    public ResponseEntity<?> increaseGivedPumati(@PathVariable Long teamId) {
        return ResponseEntity.ok(Map.of(
                "message", "increaseGivedPumatiSuccess",
                "data", Map.of("givedPumatiCount", teamService.incrementGivedPumati(teamId))
        ));
    }

    @PatchMapping("/{teamId}/received-pumati")
    public ResponseEntity<?> increaseReceivedPumati(@PathVariable Long teamId) {
        return ResponseEntity.ok(Map.of(
                "message", "increaseReceivedPumatiSuccess",
                "data", Map.of("receivedPumatiCount", teamService.incrementReceivedPumati(teamId))
        ));
    }

}
