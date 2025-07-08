package com.tebutebu.apiserver.dto.project.snapshot.response;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRankingSnapshotResponseDTO {

    private Long id;

    private List<RankingItemDTO> data;

}
