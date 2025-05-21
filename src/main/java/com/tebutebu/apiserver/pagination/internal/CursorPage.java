package com.tebutebu.apiserver.pagination.internal;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CursorPage<E>(
        List<E> items,
        Long nextCursorId,
        LocalDateTime nextCursorTime,
        boolean hasNext
) {}
