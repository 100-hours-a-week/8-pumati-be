package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.ai.comment.request.AiCommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectCreateRequestDTO;
import com.tebutebu.apiserver.dto.project.request.ProjectUpdateRequestDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectGithubUrlDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectPageResponseDTO;
import com.tebutebu.apiserver.dto.project.response.ProjectResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.ContextCursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.CursorMetaDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import com.tebutebu.apiserver.service.comment.CommentService;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.project.snapshot.ProjectRankingSnapshotService;
import com.tebutebu.apiserver.service.project.ProjectService;
import com.tebutebu.apiserver.service.subscription.SubscriptionService;
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
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    private final ProjectRankingSnapshotService projectRankingSnapshotService;

    private final MemberService memberService;

    private final CommentService commentService;

    private final SubscriptionService subscriptionService;

    @GetMapping("/{projectId}")
    public ResponseEntity<?> get(
            @PathVariable long projectId,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        Long memberId = (authorizationHeader != null)
                ? memberService.get(authorizationHeader).getId()
                : null;

        ProjectResponseDTO dto = projectService.get(projectId, memberId);
        return ResponseEntity.ok(Map.of("message", "getProjectSuccess", "data", dto));
    }

    @GetMapping("/github-urls")
    public ResponseEntity<?> getGithubUrls() {
        List<ProjectGithubUrlDTO> data = projectService.getAllGithubUrls();
        return ResponseEntity.ok(Map.of(
                "message", "getProjectsGithubUrlsSuccess",
                "data", data
        ));
    }

    @PostMapping("/snapshot")
    public ResponseEntity<?> registerSnapshot() {
        Long snapshotId = projectRankingSnapshotService.register();

        if (snapshotId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "snapshotCreationFailed"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "snapshotCreated", "data", Map.of("id", snapshotId)));
    }

    @GetMapping(params = "sort=rank")
    public ResponseEntity<?> scrollRanking(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(name = "context-id") @NotNull Long contextId,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        Long memberId = (authorizationHeader != null) ? memberService.get(authorizationHeader).getId() : null;

        ContextCursorPageRequestDTO dto = ContextCursorPageRequestDTO.builder()
                .contextId(contextId)
                .cursorId(cursorId)
                .pageSize(pageSize)
                .memberId(memberId)
                .build();

        CursorPageResponseDTO<ProjectPageResponseDTO, CursorMetaDTO> page = projectService.getRankingPage(dto);
        return ResponseEntity.ok(Map.of(
                "message", "getRankingPageSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @GetMapping(params = "sort=latest")
    public ResponseEntity<?> scrollLatest(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "cursor-time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        if (cursorTime == null) {
            cursorTime = LocalDateTime.now();
        }

        Long memberId = (authorizationHeader != null) ? memberService.get(authorizationHeader).getId() : null;

        CursorTimePageRequestDTO dto = CursorTimePageRequestDTO.builder()
                .cursorId(cursorId)
                .cursorTime(cursorTime)
                .pageSize(pageSize)
                .memberId(memberId)
                .build();

        CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> page = projectService.getLatestPage(dto);
        return ResponseEntity.ok(Map.of(
                "message", "getLatestPageSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @GetMapping("/subscription/term/{term}")
    public ResponseEntity<?> getSubscribedProjectsByTerm(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("term") int term,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "cursor-time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        if (cursorTime == null) {
            cursorTime = LocalDateTime.now();
        }

        Long memberId = memberService.get(authorizationHeader).getId();

        CursorTimePageRequestDTO dto = CursorTimePageRequestDTO.builder()
                .cursorId(cursorId)
                .cursorTime(cursorTime)
                .pageSize(pageSize)
                .memberId(memberId)
                .build();

        CursorPageResponseDTO<ProjectPageResponseDTO, TimeCursorMetaDTO> page =
                projectService.getSubscribedPageByTerm(memberId, term, dto);

        return ResponseEntity.ok(Map.of(
                "message", "getSubscribedProjectsSuccess",
                "data", page.getData(),
                "meta", page.getMeta()
        ));
    }

    @GetMapping("/{projectId}/comments")
    public ResponseEntity<?> scrollComments(
            @PathVariable("projectId") Long projectId,
            @RequestParam(name = "cursor-id", defaultValue = "0") @PositiveOrZero Long cursorId,
            @RequestParam(name = "cursor-time", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(name = "page-size", defaultValue = "10") @Positive @Min(1) @Max(100) Integer pageSize
    ) {
        if (cursorTime == null) {
            cursorTime = LocalDateTime.now();
        }

        CursorTimePageRequestDTO dto = CursorTimePageRequestDTO.builder()
                .cursorId(cursorId)
                .cursorTime(cursorTime)
                .pageSize(pageSize)
                .build();
        CursorPageResponseDTO<CommentResponseDTO, TimeCursorMetaDTO> page = commentService.getLatestCommentsByProject(projectId, dto);
        return ResponseEntity.ok(Map.of(
                "message", "getCommentPageSuccess",
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

    @PostMapping("/{projectId}/comments")
    public ResponseEntity<?> registerComment(
            @PathVariable("projectId") Long projectId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CommentCreateRequestDTO dto
    ) {
        Long memberId = memberService.get(authorizationHeader).getId();
        Long commentId = commentService.register(projectId, memberId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "registerSuccess",
                "data", Map.of("id", commentId)
        ));
    }

    @PostMapping("/{projectId}/comments/ai")
    public ResponseEntity<?> registerAiComment(
            @PathVariable Long projectId,
            @Valid @RequestBody AiCommentCreateRequestDTO dto
    ) {
        Long commentId = commentService.registerAiComment(projectId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "registerSuccess",
                "data", Map.of("id", commentId)
        ));
    }

    @PostMapping("/{projectId}/subscription")
    public ResponseEntity<?> subscribe(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long memberId = memberService.get(authorizationHeader).getId();
        Long id = subscriptionService.subscribe(memberId, projectId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "subscribeSuccess",
                        "data", Map.of(
                                "id", id,
                                "subscribedAt", LocalDateTime.now()
                        )
                ));
    }

    @DeleteMapping("/{projectId}/subscription")
    public ResponseEntity<?> unsubscribe(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long memberId = memberService.get(authorizationHeader).getId();
        subscriptionService.unsubscribe(memberId, projectId);
        return ResponseEntity.ok(Map.of("message", "unsubscribeSuccess"));
    }

    @GetMapping("/{projectId}/subscription")
    public ResponseEntity<?> checkSubscription(
            @PathVariable Long projectId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        Long memberId = memberService.get(authorizationHeader).getId();
        boolean isSubscribed = subscriptionService.isSubscribed(memberId, projectId);
        return ResponseEntity.ok(Map.of(
                "message", "getSubscriptionStatusSuccess",
                "data", Map.of("isSubscribed", isSubscribed)
        ));
    }

}
