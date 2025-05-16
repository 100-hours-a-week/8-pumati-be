package com.tebutebu.apiserver.repository.paging.comment;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.tebutebu.apiserver.domain.Comment;
import com.tebutebu.apiserver.domain.QComment;
import com.tebutebu.apiserver.pagination.dto.request.CursorPageRequestDTO;
import com.tebutebu.apiserver.pagination.factory.CursorPageFactory;
import com.tebutebu.apiserver.pagination.factory.CursorPageSpec;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentPagingRepositoryImpl implements CommentPagingRepository {

    private final CursorPageFactory cursorPageFactory;

    private final QComment c = QComment.comment;

    @Override
    public CursorPage<Comment> findByProjectLatestCursor(Long projectId, CursorPageRequestDTO req) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(c.project.id.eq(projectId));

        OrderSpecifier<?>[] orderBy = new OrderSpecifier[]{
                c.createdAt.desc(),
                c.id.desc()
        };

        CursorPageSpec<Comment> spec = CursorPageSpec.<Comment>builder()
                .entityPath(c)
                .where(where)
                .orderBy(orderBy)
                .createdAtExpr(c.createdAt)
                .idExpr(c.id)
                .cursorId(req.getCursorId())
                .cursorTime(req.getCursorTime())
                .pageSize(req.getPageSize())
                .build();
        return cursorPageFactory.create(spec);
    }

}
