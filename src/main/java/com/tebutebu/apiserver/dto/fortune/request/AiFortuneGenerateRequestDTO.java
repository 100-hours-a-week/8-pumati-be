package com.tebutebu.apiserver.dto.fortune.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tebutebu.apiserver.domain.Course;
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

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 가능합니다.")
    private String nickname;

    private Course course;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

}
