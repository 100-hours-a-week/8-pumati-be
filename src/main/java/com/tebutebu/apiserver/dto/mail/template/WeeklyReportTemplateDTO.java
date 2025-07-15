package com.tebutebu.apiserver.dto.mail.template;

import com.tebutebu.apiserver.dto.ai.report.request.BadgeStatDTO;
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
public class WeeklyReportTemplateDTO {

    String nickname;

    Integer term;

    Integer teamNumber;

    String projectTitle;

    Long receivedPumatiCount;

    Long givedPumatiCount;

    String pumatiRank;

    List<BadgeStatDTO> badgeStats;

    String reportImageUrl;

}
