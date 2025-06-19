package com.tebutebu.apiserver.pagination.dto.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
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

    @Positive(message = ValidationMessages.CONTEXT_ID_MUST_BE_POSITIVE)
    private Long contextId;

    @PositiveOrZero(message = ValidationMessages.CURSOR_COUNT_MUST_BE_POSITIVE_OR_ZERO)
    private Integer cursorCount;

}
