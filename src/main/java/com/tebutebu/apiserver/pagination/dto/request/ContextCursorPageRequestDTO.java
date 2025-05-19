package com.tebutebu.apiserver.pagination.dto.request;

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

    @Positive(message = "컨텍스트 ID는 유효한 컨텍스트 식별자여야 합니다.")
    private Long contextId;

}
