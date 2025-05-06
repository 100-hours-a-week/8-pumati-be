package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.team.request.TeamCreateRequestDTO;
import com.tebutebu.apiserver.dto.team.response.TeamListResponseDTO;
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

    @PostMapping("")
    public ResponseEntity<?> register(@Valid TeamCreateRequestDTO dto) {
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

}
