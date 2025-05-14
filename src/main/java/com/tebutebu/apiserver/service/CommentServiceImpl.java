package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.domain.CommentType;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.AuthorDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.repository.CommentRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public CommentResponseDTO get(Long commentId) {
        Comment comment = commentRepository.findByIdWithMemberAndProject(commentId)
                .orElseThrow(() -> new NoSuchElementException("commentNotFound"));

        if (comment.getProject() == null) {
            throw new NoSuchElementException("projectNotFound");
        }
        return entityToDTO(comment);
    }

    @Override
    public Long register(Long projectId, Long memberId, CommentCreateRequestDTO dto) {
        Comment comment = dtoToEntity(projectId, memberId, dto);
        return commentRepository.save(comment).getId();
    }

    @Override
    public void modify(Long commentId, Long memberId, CommentUpdateRequestDTO dto) {
        Comment comment = commentRepository.findByIdWithMemberAndProject(commentId)
                .orElseThrow(() -> new NoSuchElementException("commentNotFound"));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new CustomValidationException("notCommentAuthor");
        }

        comment.changeContent(dto.getContent());
        commentRepository.save(comment);
    }

    @Override
    public void remove(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NoSuchElementException("commentNotFound");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public Comment dtoToEntity(Long projectId, Long memberId, CommentCreateRequestDTO dto) {
        return Comment.builder()
                .member(Member.builder().id(memberId).build())
                .project(Project.builder().id(projectId).build())
                .type(CommentType.USER)
                .content(dto.getContent())
                .build();
    }

    @Override
    public CommentResponseDTO entityToDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .id(comment.getId())
                .projectId(comment.getProject().getId())
                .type(comment.getType())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .author(AuthorDTO.builder()
                        .id(comment.getMember().getId())
                        .name(comment.getMember().getName())
                        .nickname(comment.getMember().getNickname())
                        .course(comment.getMember().getCourse())
                        .profileImageUrl(comment.getMember().getProfileImageUrl())
                        .build())
                .build();
    }

}
