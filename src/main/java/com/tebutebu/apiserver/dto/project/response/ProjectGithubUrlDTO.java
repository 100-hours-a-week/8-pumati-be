package com.tebutebu.apiserver.dto.project.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectGithubUrlDTO {

    private Long projectId;

    private String githubUrl;

}
