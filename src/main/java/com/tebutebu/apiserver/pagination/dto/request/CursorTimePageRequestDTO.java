package com.tebutebu.apiserver.pagination.dto.request;

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

    @PastOrPresent(message = "커서 시간은 현재보다 이후일 수 없습니다.")
    private LocalDateTime cursorTime;

}
