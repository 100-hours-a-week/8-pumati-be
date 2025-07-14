package com.tebutebu.apiserver.pagination.dto.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CursorPageRequestDTO {

    @Positive(message = ValidationMessages.CURSOR_ID_MUST_BE_POSITIVE)
    private Long cursorId;

    @NotNull(message = ValidationMessages.PAGE_SIZE_REQUIRED)
    @Min(value = 1, message = ValidationMessages.PAGE_SIZE_MIN)
    @Max(value = 100, message = ValidationMessages.PAGE_SIZE_MAX)
    private Integer pageSize;

}
