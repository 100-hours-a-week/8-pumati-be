package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface CommentService {

    @Transactional(readOnly = true)
    CommentResponseDTO get(Long commentId);

    Long register(Long projectId, Long memberId, CommentCreateRequestDTO dto);

    void modify(Long commentId, Long memberId, CommentUpdateRequestDTO dto);

    void remove(Long commentId);

    Comment dtoToEntity(Long projectId, Long memberId, CommentCreateRequestDTO dto);

    default CommentResponseDTO entityToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }

}
