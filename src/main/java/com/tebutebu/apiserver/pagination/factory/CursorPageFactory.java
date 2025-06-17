package com.tebutebu.apiserver.pagination.factory;

import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tebutebu.apiserver.pagination.internal.CursorPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CursorPageFactory {

    private final JPAQueryFactory queryFactory;

    public <E> CursorPage<E> create(CursorPageSpec<E> spec) {
        applyTimeCursorCondition(spec);
        return buildPage(spec, "getCreatedAt", null);
    }

    public <E> CursorPage<E> createForCount(CursorPageSpec<E> spec) {
        applyCountCursorCondition(spec);
        return buildPage(spec, null, "getAcquiredCount");
    }

    private <E> CursorPage<E> buildPage(CursorPageSpec<E> spec, String timeMethodName, String countMethodName) {
        List<E> fetched = queryFactory
                .select(spec.getEntityPath())
                .from(spec.getEntityPath())
                .where(spec.getWhere())
                .orderBy(spec.getOrderBy())
                .limit(spec.getPageSize() + 1)
                .fetch();

        boolean hasNext = fetched.size() > spec.getPageSize();
        List<E> pageItems = hasNext ? fetched.subList(0, spec.getPageSize()) : fetched;

        Long nextCursorId = null;
        LocalDateTime nextCursorTime = null;
        Integer nextCursorCount = null;

        if (!pageItems.isEmpty()) {
            E last = pageItems.getLast();
            try {
                Method getId = last.getClass().getMethod("getId");
                nextCursorId = (Long) getId.invoke(last);

                if (timeMethodName != null) {
                    Method getTime = last.getClass().getMethod(timeMethodName);
                    nextCursorTime = (LocalDateTime) getTime.invoke(last);
                }

                if (countMethodName != null) {
                    Method getCount = last.getClass().getMethod(countMethodName);
                    nextCursorCount = (Integer) getCount.invoke(last);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return CursorPage.<E>builder()
                .items(pageItems)
                .nextCursorId(nextCursorId)
                .nextCursorTime(nextCursorTime)
                .nextCursorCount(nextCursorCount)
                .hasNext(hasNext)
                .build();
    }

    private <E> void applyTimeCursorCondition(CursorPageSpec<E> spec) {
        Long cursorId = spec.getCursorId();
        LocalDateTime cursorTime = spec.getCursorTime();
        DateTimeExpression<LocalDateTime> createdAtExpr = spec.getCreatedAtExpr();
        NumberExpression<Long> idExpr = spec.getIdExpr();

        if (cursorId != null && cursorTime != null) {
            spec.getWhere().and(
                    createdAtExpr.lt(cursorTime)
                            .or(createdAtExpr.eq(cursorTime).and(idExpr.lt(cursorId)))
            );
        }
    }

    private <E> void applyCountCursorCondition(CursorPageSpec<E> spec) {
        Long cursorId = spec.getCursorId();
        Integer cursorCount = spec.getCursorCount();
        NumberExpression<Integer> countExpr = spec.getCountExpr();
        NumberExpression<Long> idExpr = spec.getIdExpr();

        if (cursorId != null && cursorCount != null) {
            spec.getWhere().and(
                    countExpr.lt(cursorCount)
                            .or(countExpr.eq(cursorCount).and(idExpr.lt(cursorId)))
            );
        }
    }

}
