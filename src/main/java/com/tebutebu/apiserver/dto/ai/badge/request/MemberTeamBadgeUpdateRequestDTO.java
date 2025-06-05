package com.tebutebu.apiserver.dto.ai.badge.request;

import com.tebutebu.apiserver.dto.project.request.ProjectSummaryDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberTeamBadgeUpdateRequestDTO {

    private BadgeImageModificationRequestDTO modificationTags;

    private ProjectSummaryDTO projectSummary;

}
