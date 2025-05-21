package com.tebutebu.apiserver.dto.project.request;

import com.tebutebu.apiserver.dto.project.image.request.ProjectImageRequestDTO;
import com.tebutebu.apiserver.dto.tag.request.TagCreateRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequestDTO {

    @NotNull(message = "팀 ID는 필수 입력 값입니다.")
    private Long teamId;

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    @Size(max = 64, message = "제목은 최대 64자까지 가능합니다.")
    private String title;

    @Size(max = 150, message = "소개는 최대 150자까지 가능합니다.")
    private String introduction;

    @Size(max = 1000, message = "상세 설명은 최대 1000자까지 가능합니다.")
    private String detailedDescription;

    @Size(max = 512, message = "배포 URL은 최대 512자까지 가능합니다.")
    private String deploymentUrl;

    @Size(max = 512, message = "깃허브 URL은 최대 512자까지 가능합니다.")
    private String githubUrl;

    @NotNull(message = "태그는 최소 1개 이상 입력해야 합니다.")
    @Size(min = 1, max = 5, message = "태그는 최소 1개 이상, 최대 5개까지 가능합니다.")
    private List<@Valid TagCreateRequestDTO> tags;

    @NotNull(message = "프로젝트 이미지는 필수 입력 값입니다.")
    @Size(min = 1, message = "최소 하나 이상의 이미지를 입력해야 합니다.")
    private List<ProjectImageRequestDTO> images;

}
