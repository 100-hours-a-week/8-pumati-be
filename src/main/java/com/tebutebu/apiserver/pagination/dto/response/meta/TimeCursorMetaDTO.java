package com.tebutebu.apiserver.pagination.dto.response.meta;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TimeCursorMetaDTO extends CursorMetaDTO {

    private final LocalDateTime nextCursorTime;

}
