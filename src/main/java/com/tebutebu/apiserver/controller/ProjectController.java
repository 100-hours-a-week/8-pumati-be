package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.service.ProjectRankingSnapshotService;
import com.tebutebu.apiserver.service.ProjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> get(@PathVariable long projectId) {
        ProjectResponseDTO dto = projectService.get(projectId);
        return ResponseEntity.ok(Map.of("message", "getProjectSuccess", "data", dto));
    }

    @PostMapping("/snapshot")
    public ResponseEntity<?> registerSnapshot() {
        Long snapshotId = projectRankingSnapshotService.register();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "snapshotCreated", "data", Map.of("id", snapshotId)));
    }

    @GetMapping(params = "sort=rank")
    public ResponseEntity<?> scrollRanking(
            @RequestParam(name = "context-id") @NotNull Long contextId,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        CursorPageRequestDTO dto = CursorPageRequestDTO.builder()
                .contextId(contextId)
                .cursorId(cursorId)
                .cursorTime(null)
                .pageSize(pageSize)
                .build();
        CursorPageResponseDTO<ProjectResponseDTO> page = projectService.getRankingPage(dto);
        return ResponseEntity.ok(Map.of(
                "message", "getRankingPageSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @GetMapping(params = "sort=latest")
    public ResponseEntity<?> scrollLatest(
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "cursor-time", defaultValue = "#{T(java.time.LocalDateTime).now().format(T(java.time.format.DateTimeFormatter).ISO_DATE_TIME)}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        CursorPageRequestDTO dto = CursorPageRequestDTO.builder()
                .cursorId(cursorId)
                .cursorTime(cursorTime)
                .pageSize(pageSize)
                .build();
        CursorPageResponseDTO<ProjectResponseDTO> page = projectService.getLatestPage(dto);
        return ResponseEntity.ok(Map.of(
                "message", "getLatestPageSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @PostMapping("")
    public ResponseEntity<?> register(@Valid @RequestBody ProjectCreateRequestDTO dto) {
        long projectId = projectService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "projectCreated",
                        "data", Map.of("id", projectId)
                ));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<?> modify(
            @PathVariable long projectId,
            @Valid @RequestBody ProjectUpdateRequestDTO dto
    ) {
        projectService.modify(projectId, dto);
        return ResponseEntity.ok(Map.of("message", "updateProjectSuccess"));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<?> delete(@PathVariable long projectId) {
        projectService.delete(projectId);
        return ResponseEntity.ok(Map.of("message", "projectDeleted"));
    }

}
