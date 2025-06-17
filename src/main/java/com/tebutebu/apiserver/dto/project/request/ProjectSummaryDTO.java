package com.tebutebu.apiserver.dto.project.request;

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
public class ProjectSummaryDTO {

    private String title;

    private String introduction;

    private String detailedDescription;

    private String deploymentUrl;

    private String githubUrl;

    private List<String> tags;

    private Long teamId;

    private Integer term;

    private Integer teamNumber;

}
