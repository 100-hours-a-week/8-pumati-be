package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.domain.CommentType;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.dto.comment.ai.request.AiCommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.AuthorDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.dto.member.request.AiMemberSignupRequestDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorMetaDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.CommentRepository;
import com.tebutebu.apiserver.repository.paging.comment.CommentPagingRepository;
import com.tebutebu.apiserver.util.exception.CustomValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final CommentPagingRepository commentPagingRepository;

    private final MemberService memberService;

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
    public CursorPageResponseDTO<CommentResponseDTO> getLatestCommentsByProject(Long projectId, CursorPageRequestDTO dto) {
        CursorPage<Comment> page = commentPagingRepository.findByProjectLatestCursor(projectId, dto);

        List<CommentResponseDTO> responseData = page.items().stream()
                .map(this::entityToDTO)
                .toList();

        CursorMetaDTO meta = CursorMetaDTO.builder()
                .nextCursorId(page.nextCursorId())
                .nextCursorTime(page.nextCursorTime())
                .hasNext(page.hasNext())
                .build();

        return CursorPageResponseDTO.<CommentResponseDTO>builder()
                .data(responseData)
                .meta(meta)
                .build();
    }

    @Override
    public Long register(Long projectId, Long memberId, CommentCreateRequestDTO dto) {
        Comment comment = dtoToEntity(projectId, memberId, dto);
        return commentRepository.save(comment).getId();
    }

    @Override
    public void modify(Long commentId, Long memberId, CommentUpdateRequestDTO dto) {
        Comment comment = commentRepository.findById(commentId)
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
    public Long registerAiComment(Long projectId, AiCommentCreateRequestDTO dto) {
        AiMemberSignupRequestDTO aiMemberDto = AiMemberSignupRequestDTO.builder()
                .name(dto.getAuthorName())
                .nickname(dto.getAuthorNickname())
                .build();
        Long aiMemberId = memberService.registerAiMember(aiMemberDto);

        Comment comment = Comment.builder()
                .member(Member.builder().id(aiMemberId).build())
                .project(Project.builder().id(projectId).build())
                .type(CommentType.AI)
                .content(dto.getContent())
                .build();

        return commentRepository.save(comment).getId();
    }

    @Override
    public void modifyAiComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("commentNotFound"));

        if (comment.getType() != CommentType.AI) {
            throw new IllegalArgumentException("notAiComment");
        }

        comment.changeContent(content);
        commentRepository.save(comment);
    }

    @Override
    public void removeAiComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("commentNotFound"));

        if (comment.getType() != CommentType.AI) {
            throw new IllegalArgumentException("notAiComment");
        }

        commentRepository.delete(comment);
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
