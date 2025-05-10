package com.tebutebu.apiserver.dto.project.image.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProjectImageResponseDTO {

    private Long id;

    private Long projectId;

    private String url;

    private Integer sequence;

    private LocalDateTime createdAt, modifiedAt;

}
