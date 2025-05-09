package com.tebutebu.apiserver.pagination.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageRequestDTO {

    @Positive(message = "컨텍스트 ID는 유효한 컨텍스트 식별자여야 합니다.")
    private Long contextId;

    @Positive(message = "리소스 ID는 양수여야 합니다.")
    private Long cursorId;

    private LocalDateTime cursorTime;

    @NotNull(message = "페이지 크기는 필수 입력 값입니다.")
    @Size(min = 1, max = 100, message = "페이지 크기는 1에서 100 사이여야 합니다.")
    private Integer pageSize;

}
