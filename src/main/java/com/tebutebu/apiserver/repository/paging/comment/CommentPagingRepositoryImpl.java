package com.tebutebu.apiserver.repository.paging.comment;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.domain.QComment;
import com.tebutebu.apiserver.dto.comment.response.AuthorDTO;
import com.tebutebu.apiserver.dto.comment.response.CommentResponseDTO;
import com.tebutebu.apiserver.pagination.dto.request.CursorTimePageRequestDTO;
import com.tebutebu.apiserver.pagination.factory.CursorPageFactory;
import com.tebutebu.apiserver.pagination.factory.CursorPageSpec;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommentPagingRepositoryImpl implements CommentPagingRepository {

    private final CursorPageFactory cursorPageFactory;

    private final QComment qComment = QComment.comment;

    @Override
    public CursorPage<CommentResponseDTO> findByProjectLatestCursor(Long projectId, CursorTimePageRequestDTO req) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(qComment.project.id.eq(projectId));

        OrderSpecifier<?>[] orderBy = new OrderSpecifier[]{
                qComment.createdAt.desc(),
                qComment.id.desc()
        };

        CursorPageSpec<Comment> spec = CursorPageSpec.<Comment>builder()
                .entityPath(qComment)
                .where(where)
                .orderBy(orderBy)
                .createdAtExpr(qComment.createdAt)
                .idExpr(qComment.id)
                .cursorId(req.getCursorId())
                .cursorTime(req.getCursorTime())
                .pageSize(req.getPageSize())
                .build();
        CursorPage<Comment> page = cursorPageFactory.create(spec);

        List<CommentResponseDTO> commentResponseDtoList = page.items().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());

        return CursorPage.<CommentResponseDTO>builder()
                .items(commentResponseDtoList)
                .nextCursorId(page.nextCursorId())
                .nextCursorTime(page.nextCursorTime())
                .hasNext(page.hasNext())
                .build();
    }

    private CommentResponseDTO toResponseDTO(Comment comment) {
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
