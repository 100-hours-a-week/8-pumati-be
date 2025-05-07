package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.service.ProjectService;
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
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> get(@PathVariable long projectId) {
        ProjectResponseDTO dto = projectService.get(projectId);
        return ResponseEntity.ok(Map.of("message", "getProjectSuccess", "data", dto));
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
