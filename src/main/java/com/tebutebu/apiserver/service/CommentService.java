package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.dto.comment.ai.request.AiCommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.AuthorDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CommentService {

    @Transactional(readOnly = true)
    CommentResponseDTO get(Long commentId);

    @Transactional(readOnly = true)
    CursorPageResponseDTO<CommentResponseDTO> getLatestCommentsByProject(Long projectId, CursorTimePageRequestDTO dto);

    Long register(Long projectId, Long memberId, CommentCreateRequestDTO dto);

    void modify(Long commentId, Long memberId, CommentUpdateRequestDTO dto);

    void remove(Long commentId);

    Long registerAiComment(Long projectId, AiCommentCreateRequestDTO dto);

    void modifyAiComment(Long commentId, String content);

    void removeAiComment(Long commentId);

    Comment dtoToEntity(Long projectId, Long memberId, CommentCreateRequestDTO dto);

    default CommentResponseDTO entityToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .projectId(comment.getProject().getId())
                .type(comment.getType())
                .content(comment.getContent())
                .author(AuthorDTO.builder()
                        .id(comment.getMember().getId())
                        .name(comment.getMember().getName())
                        .nickname(comment.getMember().getNickname())
                        .course(comment.getMember().getCourse())
                        .profileImageUrl(comment.getMember().getProfileImageUrl())
                        .build())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }

}
