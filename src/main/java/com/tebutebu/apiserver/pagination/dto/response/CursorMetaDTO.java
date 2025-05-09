package com.tebutebu.apiserver.pagination.dto.response;

import lombok.Getter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CursorMetaDTO {

    private Long nextCursorId;

    private LocalDateTime nextCursorTime;

    private boolean hasNext;

}
