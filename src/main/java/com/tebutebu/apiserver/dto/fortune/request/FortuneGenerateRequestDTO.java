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
public class FortuneGenerateRequestDTO {

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(max = 10, message = "이름은 최대 10자까지 가능합니다.")
    private String name;

    private Course course;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

}
