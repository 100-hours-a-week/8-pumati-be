package com.tebutebu.apiserver.service;

import com.tebutebu.apiserver.dto.snapshot.response.ProjectRankingSnapshotResponseDTO;
import com.tebutebu.apiserver.dto.snapshot.response.RankingItemDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface ProjectRankingSnapshotService {

    Long register();

    default ProjectRankingSnapshotResponseDTO entityToDTO(Long snapshotId, Long projectId, Integer rank, Long givedPumatiCount) {
        return ProjectRankingSnapshotResponseDTO.builder()
                .id(snapshotId)
                .data(List.of(RankingItemDTO.builder()
                        .projectId(projectId)
                        .rank(rank)
                        .givedPumatiCount(givedPumatiCount)
                        .build()))
                .build();
    }

}
