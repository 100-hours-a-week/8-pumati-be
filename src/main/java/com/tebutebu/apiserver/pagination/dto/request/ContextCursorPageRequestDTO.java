package com.tebutebu.apiserver.pagination.dto.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.Positive;
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
public class ContextCursorPageRequestDTO extends CursorPageRequestDTO {

    @Positive(message = ValidationMessages.CONTEXT_ID_MUST_BE_POSITIVE)
    private Long contextId;

}
