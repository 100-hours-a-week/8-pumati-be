package com.tebutebu.apiserver.pagination.dto.response.meta;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CountCursorMetaDTO extends CursorMetaDTO {

    private final Integer nextCount;

}
