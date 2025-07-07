package com.tebutebu.apiserver.dto.project.response;

import com.tebutebu.apiserver.dto.tag.response.TagResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProjectPageResponseDTO {

    private Long id;

    private Long teamId;

    private Integer term;

    private Integer teamNumber;

    private String title;

    private String introduction;

    private String representativeImageUrl;

    private List<TagResponseDTO> tags;

    private Long commentCount;

    private Long givedPumatiCount;

    private Long receivedPumatiCount;

    private String badgeImageUrl;

    private boolean isSubscribed;

    private LocalDateTime createdAt, modifiedAt;

}
