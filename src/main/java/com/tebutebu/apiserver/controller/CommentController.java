package com.tebutebu.apiserver.controller;

import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.service.CommentService;
import com.tebutebu.apiserver.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    private final MemberService memberService;

    @GetMapping("/{commentId}")
    public ResponseEntity<?> get(@PathVariable("commentId") Long commentId) {
        return ResponseEntity.ok(Map.of("message", "getCommentSuccess", "data", commentService.get(commentId)));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> modify(
            @PathVariable("commentId") Long commentId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody CommentUpdateRequestDTO dto
    ) {
        Long memberId = memberService.get(authorizationHeader).getId();
        commentService.modify(commentId, memberId, dto);
        return ResponseEntity.ok(Map.of("message", "modifyCommentSuccess"));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> remove(@PathVariable("commentId") Long commentId) {
        commentService.remove(commentId);
        return ResponseEntity.ok(Map.of("message", "commentDeleted"));
    }

    @PutMapping("/ai/{commentId}")
    public ResponseEntity<?> modifyAiComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequestDTO dto
    ) {
        commentService.modifyAiComment(commentId, dto.getContent());
        return ResponseEntity.ok(Map.of("message", "modifyAiCommentSuccess"));
    }

    @DeleteMapping("/ai/{commentId}")
    public ResponseEntity<?> deleteAiComment(@PathVariable Long commentId) {
        commentService.removeAiComment(commentId);
        return ResponseEntity.ok(Map.of("message", "commentDeleted"));
    }

}
