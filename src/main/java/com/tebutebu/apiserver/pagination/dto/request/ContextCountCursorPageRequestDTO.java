package com.tebutebu.apiserver.pagination.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ContextCountCursorPageRequestDTO extends CursorPageRequestDTO {

    @Positive(message = "컨텍스트 ID는 유효한 컨텍스트 식별자여야 합니다.")
    private Long contextId;

    @PositiveOrZero(message = "개수는 0 이상이어야 합니다.")
    private Integer cursorCount;

}
