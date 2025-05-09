package com.tebutebu.apiserver.dto.project.image.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectImageRequestDTO {

    @NotNull(message = "프로젝트 ID는 필수 입력 값입니다.")
    @Positive(message = "프로젝트 ID는 양수여야 합니다.")
    private Long projectId;

    @NotBlank(message = "프로젝트 이미지 URL은 필수 입력 값입니다.")
    @Size(max = 512, message = "프로젝트 이미지 URL은 최대 512자까지 가능합니다.")
    private String url;

    @NotNull(message = "프로젝트 이미지 순서는 필수 입력 값입니다.")
    private Integer sequence;

}
