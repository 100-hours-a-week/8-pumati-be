package com.tebutebu.apiserver.pagination.dto.response.meta;

import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
public class CursorMetaDTO {

    protected final Long nextCursorId;

    protected final boolean hasNext;

}
