package com.tebutebu.apiserver.dto.ai.report.request;

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
public class WeeklyReportImageRequestDTO {

    private Long projectId;

    private String projectTitle;

    private TeamInfoDTO team;

    private List<BadgeStatDTO> badgeStats;

    private List<DailyPumatiStatDTO> dailyPumatiStats;

}
