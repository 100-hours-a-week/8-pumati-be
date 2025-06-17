package com.tebutebu.apiserver.dto.ai.badge.request;

import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamBadgeImageUpdateRequestDTO {

    private List<@NotBlank(message = "각 태그는 공백이 아닌 문자열이어야 합니다.") String> modificationTags;

    private ProjectSummaryDTO projectSummary;

}
