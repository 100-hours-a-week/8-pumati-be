package com.tebutebu.apiserver.pagination.factory;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberExpression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CursorPageSpec<E> {

    private EntityPathBase<E> entityPath;

    private BooleanBuilder where;

    private OrderSpecifier<?>[] orderBy;

    private DateTimeExpression<LocalDateTime> createdAtExpr;

    private NumberExpression<Long> idExpr;

    private NumberExpression<Integer> countExpr;

    private Long cursorId;

    private LocalDateTime cursorTime;

    private Integer cursorCount;

    private int pageSize;

}
