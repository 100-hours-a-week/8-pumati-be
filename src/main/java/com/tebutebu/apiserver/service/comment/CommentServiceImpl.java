package com.tebutebu.apiserver.service.comment;

import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.domain.enums.CommentType;
import com.tebutebu.apiserver.domain.Member;
import com.tebutebu.apiserver.domain.Project;
import com.tebutebu.apiserver.dto.ai.comment.request.AiCommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentCreateRequestDTO;
import com.tebutebu.apiserver.dto.comment.request.CommentUpdateRequestDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.dto.member.request.AiMemberSignupRequestDTO;
import com.tebutebu.apiserver.global.errorcode.BusinessErrorCode;
import com.tebutebu.apiserver.global.exception.BusinessException;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.dto.response.CursorPageResponseDTO;
import com.tebutebu.apiserver.pagination.dto.response.meta.TimeCursorMetaDTO;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import com.tebutebu.apiserver.repository.CommentRepository;
import com.tebutebu.apiserver.repository.paging.comment.CommentPagingRepository;
import com.tebutebu.apiserver.service.member.MemberService;
import com.tebutebu.apiserver.service.project.ProjectService;
import com.tebutebu.apiserver.service.team.TeamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final CommentPagingRepository commentPagingRepository;

    private final MemberService memberService;

    private final ProjectService projectService;

    private final TeamService teamService;

    @Value("${pumati.comment.count}")
    private long commentPumatiCount;

    @Override
    public CommentResponseDTO get(Long commentId) {
        Comment comment = commentRepository.findByIdWithMemberAndProject(commentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND));

        if (comment.getProject() == null) {
            throw new BusinessException(BusinessErrorCode.PROJECT_NOT_FOUND);
        }
        return entityToDTO(comment);
    }

    @Override
    public CursorPageResponseDTO<CommentResponseDTO, TimeCursorMetaDTO> getLatestCommentsByProject(Long projectId, CursorTimePageRequestDTO dto) {
        CursorPage<CommentResponseDTO> page =
                commentPagingRepository.findByProjectLatestCursor(projectId, dto);

        TimeCursorMetaDTO meta = TimeCursorMetaDTO.builder()
                .nextCursorId(page.nextCursorId())
                .nextCursorTime(page.nextCursorTime())
                .hasNext(page.hasNext())
                .build();

        return CursorPageResponseDTO.<CommentResponseDTO, TimeCursorMetaDTO>builder()
                .data(page.items())
                .meta(meta)
                .build();
    }

    @Override
    public Long register(Long projectId, Long memberId, CommentCreateRequestDTO dto) {
        Comment comment = dtoToEntity(projectId, memberId, dto);

        Long giverTeamId = memberService.get(memberId).getTeamId();
        teamService.incrementGivedPumatiBy(giverTeamId, commentPumatiCount);

        Long receiverTeamId = projectService.get(projectId, memberId).getTeamId();
        teamService.incrementReceivedPumatiBy(receiverTeamId, commentPumatiCount);

        return commentRepository.save(comment).getId();
    }

    @Override
    public void modify(Long commentId, Long memberId, CommentUpdateRequestDTO dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(BusinessErrorCode.NOT_COMMENT_AUTHOR);
        }

        comment.changeContent(dto.getContent());
        commentRepository.save(comment);
    }

    @Override
    public void remove(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND);
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
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND));

        if (comment.getType() != CommentType.AI) {
            throw new BusinessException(BusinessErrorCode.NOT_AI_COMMENT);
        }

        comment.changeContent(content);
        commentRepository.save(comment);
    }

    @Override
    public void removeAiComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(BusinessErrorCode.COMMENT_NOT_FOUND));

        if (comment.getType() != CommentType.AI) {
            throw new BusinessException(BusinessErrorCode.NOT_AI_COMMENT);
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

}
