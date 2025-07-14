package com.tebutebu.apiserver.pagination.dto.request;

import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class CursorTimePageRequestDTO extends CursorPageRequestDTO {

    @PastOrPresent(message = ValidationMessages.CURSOR_TIME_PAST_OR_PRESENT)
    private LocalDateTime cursorTime;

    private Long memberId;

}
