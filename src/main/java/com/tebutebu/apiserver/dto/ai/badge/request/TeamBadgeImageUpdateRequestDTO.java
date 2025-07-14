package com.tebutebu.apiserver.dto.ai.badge.request;

import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
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

    private List<String> modificationTags;

    private ProjectSummaryDTO projectSummary;

}
