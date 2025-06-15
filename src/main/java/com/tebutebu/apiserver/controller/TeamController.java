package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.ai.badge.request.BadgeImageModificationRequestDTO;
import com.tebutebu.apiserver.dto.ai.badge.response.TeamBadgeStatPageResponseDTO;
import com.tebutebu.apiserver.dto.member.response.MemberResponseDTO;
import com.tebutebu.apiserver.dto.team.request.BadgeImageUpdateRequestDTO;
import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCountCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CountCursorMetaDTO;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.team.TeamService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    private final MemberService memberService;

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(Map.of(
                "message", "getTeamSuccess",
                "data", teamService.get(teamId)
        ));
    }

    @PostMapping("")
    public ResponseEntity<?> register(@Valid @RequestBody TeamCreateRequestDTO dto) {
        Long teamId = teamService.register(dto);
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
    public ResponseEntity<?> getTeamMembers(@PathVariable Long teamId) {
        List<MemberResponseDTO> members = memberService.getMembersByTeamId(teamId);
        return ResponseEntity.ok(Map.of(
                "message", "getTeamMembersSuccess",
                "data", members
        ));
    }

    @PostMapping("/{teamId}/badge-image")
    public ResponseEntity<?> requestUpdateBadgeImage(
            @PathVariable Long teamId,
            @Valid @RequestBody BadgeImageModificationRequestDTO badgeImageModificationRequestDTO
    ) {
        teamService.requestUpdateBadgeImage(teamId, badgeImageModificationRequestDTO);
        return ResponseEntity.ok(Map.of("message", "requestSuccess"));
    }

    @PatchMapping("/{teamId}/badge-image-url")
    public ResponseEntity<?> modifyBadgeImage(
            @PathVariable Long teamId,
            @Valid @RequestBody BadgeImageUpdateRequestDTO dto
    ) {
        teamService.updateBadgeImageUrl(teamId, dto.getBadgeImageUrl());
        return ResponseEntity.ok(Map.of("message", "updateBadgeImageSuccess"));
    }

    @PatchMapping("/{teamId}/gived-pumati")
    public ResponseEntity<?> increaseGivedPumati(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO memberDTO = memberService.get(authorizationHeader);
        if (!memberDTO.getTeamId().equals(teamId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalidRequest"));
        }

        Long result = teamService.incrementGivedPumati(teamId);
        return ResponseEntity.ok(Map.of(
                "message", "increaseGivedPumatiSuccess",
                "data", Map.of("givedPumatiCount", result)
        ));
    }

    @PatchMapping("/{teamId}/received-pumati")
    public ResponseEntity<?> increaseReceivedPumati(
            @PathVariable Long teamId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        MemberResponseDTO memberDTO = memberService.get(authorizationHeader);
        if (memberDTO.getTeamId().equals(teamId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalidRequest"));
        }

        Long result = teamService.incrementReceivedPumati(teamId);
        return ResponseEntity.ok(Map.of(
                "message", "increaseReceivedPumatiSuccess",
                "data", Map.of("receivedPumatiCount", result)
        ));
    }

    @GetMapping("/received-badges")
    public ResponseEntity<?> getReceivedBadgesPage(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "cursor-count", required = false) @PositiveOrZero Integer cursorCount,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        Long receiverTeamId = memberService.get(authorizationHeader).getTeamId();
        ContextCountCursorPageRequestDTO dto = ContextCountCursorPageRequestDTO.builder()
                .contextId(receiverTeamId)
                .cursorId(cursorId)
                .cursorCount(cursorCount != null ? cursorCount : Integer.MAX_VALUE)
                .pageSize(pageSize)
                .build();
        dto.setCursorCount(Objects.requireNonNullElse(cursorCount, Integer.MAX_VALUE));

        CursorPageResponseDTO<TeamBadgeStatPageResponseDTO, CountCursorMetaDTO> page = teamService.getReceivedBadgesPage(dto);
        return ResponseEntity.ok(Map.of(
                "message", "getReceivedBadgesSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @PatchMapping("/{receiverTeamId}/badge")
    public ResponseEntity<?> acquireBadge(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long receiverTeamId
    ) {
        MemberResponseDTO memberDTO = memberService.get(authorizationHeader);
        Long giverTeamId = memberDTO.getTeamId();

        if (giverTeamId.equals(receiverTeamId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "invalidRequest"));
        }

        teamService.increaseOrCreateBadge(giverTeamId, receiverTeamId);
        return ResponseEntity.ok(Map.of("message", "acquireBadgeSuccess"));
    }

    @PatchMapping("/{teamId}/ai-badge-status")
    public ResponseEntity<?> resetBadgeProgress(@PathVariable Long teamId) {
        teamService.resetAiBadgeProgress(teamId);
        return ResponseEntity.ok(Map.of("message", "updateAiBadgeStatusSuccess"));
    }

}

