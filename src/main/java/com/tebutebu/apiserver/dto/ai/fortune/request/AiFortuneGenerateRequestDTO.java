package com.tebutebu.apiserver.dto.ai.fortune.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tebutebu.apiserver.domain.enums.Course;
import com.tebutebu.apiserver.global.constant.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiFortuneGenerateRequestDTO {

    @NotBlank(message = ValidationMessages.AI_COMMENT_NICKNAME_REQUIRED)
    @Size(max = 50, message = ValidationMessages.AI_COMMENT_NICKNAME_MAX_LENGTH_EXCEEDED)
    private String nickname;

    private Course course;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
