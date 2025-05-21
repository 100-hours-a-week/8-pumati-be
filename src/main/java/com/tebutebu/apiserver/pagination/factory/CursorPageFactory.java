package com.tebutebu.apiserver.pagination.factory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CursorPageFactory {

    private final JPAQueryFactory queryFactory;

    public <E> CursorPage<E> create(CursorPageSpec<E> spec) {
        EntityPathBase<E> entityPath = spec.getEntityPath();
        BooleanBuilder where = spec.getWhere();
        OrderSpecifier<?>[] orderBy = spec.getOrderBy();
        DateTimeExpression<LocalDateTime> createdAtExpr = spec.getCreatedAtExpr();
        NumberExpression<Long> idExpr = spec.getIdExpr();
        Long cursorID = spec.getCursorId();
        LocalDateTime afterTime = spec.getCursorTime();
        int limit = spec.getPageSize();

        if (cursorID != null && afterTime != null) {
            where.and(
                    createdAtExpr.lt(afterTime)
                            .or(
                                    createdAtExpr.eq(afterTime)
                                            .and(idExpr.lt(cursorID)))
            );
        }

        List<E> fetched = queryFactory
                .select(entityPath)
                .from(entityPath)
                .where(where)
                .orderBy(orderBy)
                .limit(limit + 1)
                .fetch();

        boolean hasNext = fetched.size() > limit;
        List<E> pageItems = hasNext ? fetched.subList(0, limit) : fetched;

        if (pageItems.isEmpty()) {
            return CursorPage.<E>builder()
                    .items(pageItems)
                    .nextCursorId(null)
                    .nextCursorTime(null)
                    .hasNext(false)
                    .build();
        }

        E last = pageItems.get(pageItems.size() - 1);
        Long nextCursorId;
        LocalDateTime nextCursorTime;
        try {
            nextCursorId = (Long) last.getClass().getMethod("getId").invoke(last);
            nextCursorTime = (LocalDateTime) last.getClass().getMethod("getCreatedAt").invoke(last);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        return CursorPage.<E>builder()
                .items(pageItems)
                .nextCursorId(nextCursorId)
                .nextCursorTime(nextCursorTime)
                .hasNext(hasNext)
                .build();
    }
}
