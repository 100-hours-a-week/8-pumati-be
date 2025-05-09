package com.tebutebu.apiserver.dto.snapshot.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProjectRankingSnapshotResponseDTO {

    private Long id;

    private List<RankingItemDTO> data;

}
