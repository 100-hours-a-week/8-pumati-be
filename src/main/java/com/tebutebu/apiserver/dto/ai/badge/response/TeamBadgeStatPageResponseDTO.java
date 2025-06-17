package com.tebutebu.apiserver.dto.ai.badge.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TeamBadgeStatPageResponseDTO {

    private Long id;

    private Long projectId;

    private Long giverTeamId;

    private Integer giverTeamTerm;

    private Integer giverTeamNumber;

    private String badgeImageUrl;

    private Integer acquiredCount;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
